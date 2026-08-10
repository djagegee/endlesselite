package de.shadow.nachtweber;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

final class NachtweberRuntimeWiring {
  interface RegistrationPort {
    void registerMaintenance(NachtweberMaintenanceSystem system);
    void registerShadowSwingMovement(ShadowSwingMovementSystem system);
    void registerVenomDamage(VenomDamageSystem system);
    void registerWorldStart(Consumer<Store<EntityStore>> handler);
    void registerWorldRemoval(Consumer<Store<EntityStore>> handler);
    void registerPlayerDisconnect(BiConsumer<Store<EntityStore>, UUID> handler);
  }

  private final NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator =
      new NachtweberRuntimeCoordinator<>();
  private final OwnerEntityBindingRegistry<Store<EntityStore>, Ref<EntityStore>> ownerBindings =
      new OwnerEntityBindingRegistry<>();
  private final LongSupplier clock;
  private final LongConsumer firstMaintenanceObserver;
  private final VenomPowerProvider powerProvider;
  private final DamageCauseReadinessProbe damageCauseReadinessProbe;
  private final Consumer<DamageCauseReadinessProbe.Result> damageCauseReadinessObserver;
  private boolean startAttempted;
  private boolean started;

  NachtweberRuntimeWiring(LongSupplier clock) {
    this(clock, ignored -> { }, owner -> Double.NaN,
        new DamageCauseReadinessProbe(), ignored -> { });
  }

  NachtweberRuntimeWiring(LongSupplier clock, LongConsumer firstMaintenanceObserver) {
    this(clock, firstMaintenanceObserver, owner -> Double.NaN,
        new DamageCauseReadinessProbe(), ignored -> { });
  }

  NachtweberRuntimeWiring(
      LongSupplier clock,
      LongConsumer firstMaintenanceObserver,
      VenomPowerProvider powerProvider) {
    this(clock, firstMaintenanceObserver, powerProvider,
        new DamageCauseReadinessProbe(), ignored -> { });
  }

  NachtweberRuntimeWiring(
      LongSupplier clock,
      LongConsumer firstMaintenanceObserver,
      VenomPowerProvider powerProvider,
      DamageCauseReadinessProbe damageCauseReadinessProbe,
      Consumer<DamageCauseReadinessProbe.Result> damageCauseReadinessObserver) {
    this.clock = Objects.requireNonNull(clock, "clock");
    this.firstMaintenanceObserver =
        Objects.requireNonNull(firstMaintenanceObserver, "firstMaintenanceObserver");
    this.powerProvider = Objects.requireNonNull(powerProvider, "powerProvider");
    this.damageCauseReadinessProbe =
        Objects.requireNonNull(damageCauseReadinessProbe, "damageCauseReadinessProbe");
    this.damageCauseReadinessObserver =
        Objects.requireNonNull(damageCauseReadinessObserver, "damageCauseReadinessObserver");
  }

  synchronized void start(RegistrationPort port) {
    Objects.requireNonNull(port, "port");
    if (started) return;
    if (startAttempted) throw new IllegalStateException("Nachtweber runtime wiring already attempted");
    startAttempted = true;
    try {
      port.registerMaintenance(new NachtweberMaintenanceSystem(coordinator, clock));
      port.registerShadowSwingMovement(new ShadowSwingMovementSystem(coordinator, clock));
      port.registerVenomDamage(new VenomDamageSystem(coordinator, ownerBindings, clock));
      port.registerWorldStart(store -> {
        if (store != null) {
          damageCauseReadinessProbe.reportReadyOnce(damageCauseReadinessObserver);
          coordinator.bind(
              store, () -> NachtweberLedgerStoreRuntime.defaults(
                  firstMaintenanceObserver, powerProvider));
        }
      });
      port.registerWorldRemoval(store -> {
        ownerBindings.release(store);
        coordinator.release(store);
      });
      port.registerPlayerDisconnect((store, owner) -> {
        if (store != null && owner != null) {
          coordinator.withGameplay(store, gameplay -> gameplay.cleanupOwner(owner));
          ownerBindings.removeOwner(store, owner);
        }
      });
      started = true;
    } catch (RuntimeException | Error failure) {
      ownerBindings.clear();
      coordinator.shutdown();
      throw failure;
    }
  }

  synchronized boolean withGameplay(
      Store<EntityStore> store, Consumer<NachtweberGameplayRuntime> action) {
    Objects.requireNonNull(action, "action");
    return started && store != null && coordinator.withGameplay(store, action);
  }

  synchronized java.util.Map<String, com.ziggfreed.mmoskilltree.ability.AbilityEffect>
      createStoreBoundEffects(VenomImmunityResolver immunity) {
    if (!started) throw new IllegalStateException("Nachtweber runtime wiring is not started");
    return NachtweberAbilityRegistry.createStoreBoundEffects(
        coordinator, Objects.requireNonNull(immunity, "immunity"), ownerBindings::bind);
  }

  synchronized void shutdown() {
    ownerBindings.clear();
    coordinator.shutdown();
    started = false;
  }

  synchronized boolean isStarted() { return started; }
}
