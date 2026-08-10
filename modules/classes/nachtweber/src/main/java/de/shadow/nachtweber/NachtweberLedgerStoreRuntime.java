package de.shadow.nachtweber;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.function.LongConsumer;

final class NachtweberLedgerStoreRuntime
    implements NachtweberStoreRuntime, NachtweberGameplayRuntime {
  private static final int DEFAULT_ENTANGLEMENT_STACK_CAP = 5;
  private static final int DEFAULT_BOUND_THRESHOLD = 3;
  private static final int DEFAULT_VENOM_STACK_CAP = 4;
  private static final long DEFAULT_VENOM_TICK_INTERVAL_MS = 1_000L;

  private final EntanglementLedger entanglement;
  private final VenomLedger venom;
  private final BlackThreadService blackThread;
  private final HuntingCocoonService huntingCocoon;
  private final PassiveVenomService passiveVenom;
  private final VenomTickService venomTick;
  private final ShadowSwingReconciliationController shadowSwingReconciliation;
  private final LongConsumer firstMaintenanceObserver;
  private boolean firstMaintenanceReported;
  private boolean closed;

  static NachtweberLedgerStoreRuntime defaults(LongConsumer firstMaintenanceObserver) {
    return defaults(firstMaintenanceObserver, owner -> Double.NaN);
  }

  static NachtweberLedgerStoreRuntime defaults(
      LongConsumer firstMaintenanceObserver, VenomPowerProvider powerProvider) {
    return new NachtweberLedgerStoreRuntime(
        DEFAULT_ENTANGLEMENT_STACK_CAP,
        DEFAULT_BOUND_THRESHOLD,
        DEFAULT_VENOM_STACK_CAP,
        DEFAULT_VENOM_TICK_INTERVAL_MS,
        firstMaintenanceObserver,
        powerProvider);
  }

  NachtweberLedgerStoreRuntime(
      int entanglementStackCap,
      int boundThreshold,
      int venomStackCap,
      long venomTickIntervalMs,
      LongConsumer firstMaintenanceObserver) {
    this(
        entanglementStackCap,
        boundThreshold,
        venomStackCap,
        venomTickIntervalMs,
        firstMaintenanceObserver,
        owner -> Double.NaN);
  }

  private NachtweberLedgerStoreRuntime(
      int entanglementStackCap,
      int boundThreshold,
      int venomStackCap,
      long venomTickIntervalMs,
      LongConsumer firstMaintenanceObserver,
      VenomPowerProvider powerProvider) {
    this.entanglement = new EntanglementLedger(entanglementStackCap, boundThreshold);
    this.venom = new VenomLedger(venomStackCap, venomTickIntervalMs);
    this.blackThread = new BlackThreadService(
        entanglement,
        new BlackThreadRules(12.0, 2, 8_000L, 3_000L, 1_200L),
        ControlProfile.defaults());
    this.huntingCocoon = new HuntingCocoonService(
        entanglement,
        venom,
        new HuntingCocoonRules(8.0, 3, 2, 8_000L, 7_000L));
    this.passiveVenom = new PassiveVenomService(
        entanglement,
        venom,
        new PassiveVenomRules(1, 1, 6_000L, 1_000L));
    this.venomTick = new VenomTickService(
        venom,
        new VenomTickRules(1.5, 100.0, 100.0),
        Objects.requireNonNull(powerProvider, "powerProvider"));
    this.shadowSwingReconciliation = new ShadowSwingReconciliationController(
        new ShadowSwingReconciliationRules(
            1_500L, 18.0, 22.0, 1.25, 14.0, 3.0, 250L));
    this.firstMaintenanceObserver =
        Objects.requireNonNull(firstMaintenanceObserver, "firstMaintenanceObserver");
  }

  @Override
  public synchronized Optional<BlackThreadService.CastOutcome> castBlackThread(
      BlackThreadService.CastRequest request) {
    return closed ? Optional.empty() : Optional.of(blackThread.cast(request));
  }

  @Override
  public synchronized Optional<HuntingCocoonService.CastOutcome> castHuntingCocoon(
      HuntingCocoonService.CastRequest request) {
    return closed ? Optional.empty() : Optional.of(huntingCocoon.cast(request));
  }

  @Override
  public synchronized Optional<PassiveVenomOutcome> applyPassiveVenom(
      PassiveVenomRequest request) {
    return closed ? Optional.empty() : Optional.of(passiveVenom.onDirectHit(request));
  }

  @Override
  public synchronized Optional<VenomTickService.TickOutcome> tickVenom(
      int target, UUID owner, long nowMs, VenomDamagePort port) {
    return closed ? Optional.empty() : Optional.of(venomTick.tick(target, owner, nowMs, port));
  }

  @Override
  public synchronized List<UUID> activeVenomOwners(int target, long nowMs) {
    return closed || target < 0 || nowMs < 0L ? List.of() : venom.activeOwners(target, nowMs);
  }

  @Override
  public synchronized Optional<ShadowSwingReconciliationController.StartOutcome>
      startShadowSwing(ShadowSwingReconciliationController.StartRequest request) {
    return closed ? Optional.empty() : Optional.of(shadowSwingReconciliation.start(request));
  }

  @Override
  public synchronized Optional<ShadowSwingReconciliationController.StepOutcome>
      stepShadowSwing(ShadowSwingReconciliationController.StepRequest request) {
    return closed ? Optional.empty() : Optional.of(shadowSwingReconciliation.step(request));
  }

  @Override
  public synchronized boolean cancelShadowSwing(UUID owner) {
    return !closed && shadowSwingReconciliation.cancel(owner);
  }

  @Override
  public synchronized boolean cleanupOwner(UUID owner) {
    if (closed || owner == null) return false;
    entanglement.cleanseOwner(owner);
    venom.cleanseOwner(owner);
    blackThread.cleanseOwner(owner);
    huntingCocoon.cleanseOwner(owner);
    passiveVenom.cleanseOwner(owner);
    shadowSwingReconciliation.cleanseOwner(owner);
    return true;
  }

  synchronized int applyEntanglement(
      int target, UUID owner, int stacks, long durationMs, long nowMs) {
    if (closed) return 0;
    return entanglement.apply(target, owner, stacks, durationMs, nowMs);
  }

  synchronized int applyVenom(
      int target, UUID owner, int stacks, long durationMs, long nowMs) {
    if (closed) return 0;
    return venom.apply(target, owner, stacks, durationMs, nowMs, () -> false);
  }

  synchronized int entanglementStacks(int target, UUID owner, long nowMs) {
    if (closed) return 0;
    return entanglement.stacks(target, owner, nowMs);
  }

  synchronized boolean hasVenom(int target, UUID owner, long nowMs) {
    return !closed && venom.hasState(target, owner, nowMs);
  }

  @Override
  public void maintain(long nowMs) {
    boolean report;
    synchronized (this) {
      if (closed || nowMs < 0L) return;
      entanglement.purgeExpired(nowMs);
      venom.purgeExpired(nowMs);
      report = !firstMaintenanceReported;
      firstMaintenanceReported = true;
    }
    if (report) {
      try {
        firstMaintenanceObserver.accept(nowMs);
      } catch (RuntimeException ignored) {
        // Maintenance remains fail-closed if diagnostics are unavailable.
      }
    }
  }

  @Override
  public synchronized void close() {
    if (closed) return;
    closed = true;
    entanglement.clear();
    venom.clear();
    shadowSwingReconciliation.close();
  }
}
