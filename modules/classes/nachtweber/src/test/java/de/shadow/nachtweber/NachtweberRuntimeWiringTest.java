package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.shadow.endlesselite.core.OwnedRegistryLifecycle;
import org.junit.jupiter.api.Test;

final class NachtweberRuntimeWiringTest {
  @Test void storeStateUsesIdentityAndReleasesOnlyTheExactStore() {
    NachtweberStoreStateRegistry<EqualStore, StringBuilder> registry =
        new NachtweberStoreStateRegistry<>();
    EqualStore first = new EqualStore("world");
    EqualStore equalButDistinct = new EqualStore("world");
    List<String> cleaned = new ArrayList<>();

    registry.getOrCreate(first, () -> new StringBuilder("first"));
    registry.getOrCreate(equalButDistinct, () -> new StringBuilder("second"));

    assertEquals(2, registry.size());
    assertTrue(registry.release(first, value -> cleaned.add(value.toString())));
    assertEquals(List.of("first"), cleaned);
    assertEquals(1, registry.size());
    assertEquals("second", registry.getOrCreate(equalButDistinct, StringBuilder::new).toString());
    assertFalse(registry.release(first, value -> fail("released state must not be cleaned twice")));
  }

  @Test void ownerEntityBindingsUseStoreIdentityAndCleanExactPartitions() {
    OwnerEntityBindingRegistry<EqualStore, String> bindings = new OwnerEntityBindingRegistry<>();
    EqualStore first = new EqualStore("world");
    EqualStore equalButDistinct = new EqualStore("world");
    java.util.UUID owner = java.util.UUID.fromString(
        "91000000-0000-0000-0000-000000000001");

    assertTrue(bindings.bind(first, owner, "first-ref"));
    assertTrue(bindings.bind(equalButDistinct, owner, "second-ref"));
    assertEquals("first-ref", bindings.lookup(first, owner));
    assertEquals("second-ref", bindings.lookup(equalButDistinct, owner));
    assertTrue(bindings.removeOwner(first, owner));
    assertNull(bindings.lookup(first, owner));
    assertEquals("second-ref", bindings.lookup(equalButDistinct, owner));
    assertTrue(bindings.release(equalButDistinct));
    assertEquals(0, bindings.storeCount());
  }

  @Test void shutdownClearCleansEveryRemainingStoreExactlyOnce() {
    NachtweberStoreStateRegistry<Object, StringBuilder> registry =
        new NachtweberStoreStateRegistry<>();
    Object first = new Object();
    Object second = new Object();
    List<String> cleaned = new ArrayList<>();
    registry.getOrCreate(first, () -> new StringBuilder("first"));
    registry.getOrCreate(second, () -> new StringBuilder("second"));

    registry.clear(value -> cleaned.add(value.toString()));
    registry.clear(value -> fail("cleared state must not be cleaned twice"));

    assertEquals(0, registry.size());
    assertEquals(List.of("first", "second"), cleaned.stream().sorted().toList());
  }

  @Test void maintenanceLookupDoesNotCreateStateForAnEmptyStore() {
    NachtweberStoreStateRegistry<Object, StringBuilder> registry =
        new NachtweberStoreStateRegistry<>();
    Object store = new Object();
    List<String> seen = new ArrayList<>();

    assertFalse(registry.ifPresent(store, value -> seen.add(value.toString())));
    assertEquals(0, registry.size());

    registry.getOrCreate(store, () -> new StringBuilder("existing"));
    assertTrue(registry.ifPresent(store, value -> seen.add(value.toString())));
    assertEquals(List.of("existing"), seen);
  }

  @Test void runtimeCoordinatorMaintainsReleasesAndClosesFailClosed() {
    NachtweberRuntimeCoordinator<EqualStore> coordinator = new NachtweberRuntimeCoordinator<>();
    EqualStore first = new EqualStore("world");
    EqualStore equalButDistinct = new EqualStore("world");
    RecordingRuntime firstRuntime = new RecordingRuntime();
    RecordingRuntime secondRuntime = new RecordingRuntime();

    coordinator.bind(first, () -> firstRuntime);
    coordinator.bind(equalButDistinct, () -> secondRuntime);
    assertTrue(coordinator.maintain(first, 100L));
    assertEquals(List.of(100L), firstRuntime.maintenanceTimes);
    assertTrue(secondRuntime.maintenanceTimes.isEmpty());

    assertTrue(coordinator.release(first));
    assertEquals(1, firstRuntime.closeCalls);
    assertEquals(0, secondRuntime.closeCalls);
    assertFalse(coordinator.maintain(first, 200L));

    coordinator.shutdown();
    coordinator.shutdown();
    assertEquals(1, secondRuntime.closeCalls);
    assertThrows(IllegalStateException.class,
        () -> coordinator.bind(new EqualStore("late"), RecordingRuntime::new));
  }

  @Test void maintenanceSystemUsesTheVerifiedStoreLevelTickSignature() throws Exception {
    assertTrue(TickingSystem.class.isAssignableFrom(NachtweberMaintenanceSystem.class));
    assertNotNull(NachtweberMaintenanceSystem.class.getDeclaredMethod(
        "tick", float.class, int.class, Store.class));
    ParameterizedType base = (ParameterizedType) NachtweberMaintenanceSystem.class.getGenericSuperclass();
    assertEquals(EntityStore.class, base.getActualTypeArguments()[0]);
  }

  @Test void runtimeWiringRegistersMaintenanceAndWorldRemovalExactlyOnce() {
    RecordingRegistrationPort port = new RecordingRegistrationPort();
    NachtweberRuntimeWiring wiring = new NachtweberRuntimeWiring(() -> 100L);

    wiring.start(port);
    wiring.start(port);

    assertEquals(1, port.maintenanceSystems.size());
    assertEquals(1, port.movementSystems.size());
    assertEquals(1, port.venomDamageSystems.size());
    assertEquals(1, port.worldStartHandlers.size());
    assertDoesNotThrow(() -> port.worldStartHandlers.get(0).accept(null));
    assertEquals(1, port.worldRemovalHandlers.size());
    assertEquals(1, port.playerDisconnectHandlers.size());
    assertDoesNotThrow(() -> port.playerDisconnectHandlers.get(0).accept(
        null, java.util.UUID.fromString("c0000000-0000-0000-0000-000000000001")));
    assertTrue(wiring.isStarted());
    wiring.shutdown();
    wiring.shutdown();
    assertFalse(wiring.isStarted());
  }

  @Test void pluginEntrypointOwnsTheRuntimeWiringAndRegistrationPort() throws Exception {
    assertEquals(NachtweberRuntimeWiring.class,
        NachtweberPlugin.class.getDeclaredField("runtimeWiring").getType());
    assertEquals(OwnedRegistryLifecycle.class,
        NachtweberPlugin.class.getDeclaredField("effectLifecycle").getType());
    Class<?> runtimePort = Class.forName("de.shadow.nachtweber.NachtweberPlugin$RuntimePort");
    assertTrue(NachtweberLifecycle.RuntimePort.class.isAssignableFrom(runtimePort));
    assertTrue(NachtweberRuntimeWiring.RegistrationPort.class.isAssignableFrom(runtimePort));
  }

  @Test void runtimeWiringCreatesExactlyThreeStoreBoundEffectsWithoutDirectVelocityMutation()
      throws Exception {
    RecordingRegistrationPort port = new RecordingRegistrationPort();
    NachtweberRuntimeWiring wiring = new NachtweberRuntimeWiring(() -> 100L);
    wiring.start(port);

    var effects = wiring.createStoreBoundEffects(VenomImmunityResolver.none());
    assertEquals(3, effects.size());
    assertInstanceOf(StoreBoundBlackThreadAbility.class,
        effects.get(BlackThreadAbilityContracts.EFFECT_ID));
    assertInstanceOf(StoreBoundShadowSwingAbility.class,
        effects.get(ShadowSwingAbilityContracts.EFFECT_ID));
    assertInstanceOf(StoreBoundHuntingCocoonAbility.class,
        effects.get(HuntingCocoonAbilityContracts.EFFECT_ID));

    try (var stream = StoreBoundShadowSwingAbility.class.getResourceAsStream(
        "StoreBoundShadowSwingAbility.class")) {
      assertNotNull(stream);
      String constants = new String(stream.readAllBytes(), StandardCharsets.ISO_8859_1);
      assertFalse(constants.contains("addVelocity"));
      assertTrue(constants.contains("startShadowSwing"));
    }
    wiring.shutdown();
  }

  @Test void runtimeWiringExposesOnlyStoreScopedGameplayDispatch() throws Exception {
    assertNotNull(NachtweberRuntimeWiring.class.getDeclaredMethod(
        "withGameplay", Store.class, Consumer.class));
  }

  @Test void runtimeWiringDoesNotProbeDamageCauseForANullWorldStore() {
    int[] reads = {0};
    List<DamageCauseReadinessProbe.Result> observed = new ArrayList<>();
    DamageCauseReadinessProbe probe = new DamageCauseReadinessProbe(() -> {
      reads[0]++;
      return new DamageCauseReadinessProbe.Result(
          DamageCauseReadinessProbe.Status.READY, "Physical", 3);
    });
    RecordingRegistrationPort port = new RecordingRegistrationPort();
    NachtweberRuntimeWiring wiring = new NachtweberRuntimeWiring(
        () -> 100L, ignored -> { }, ignored -> 0.0, probe, observed::add);

    wiring.start(port);
    port.worldStartHandlers.get(0).accept(null);

    assertEquals(0, reads[0]);
    assertTrue(observed.isEmpty());
    wiring.shutdown();
  }

  @Test void runtimeWiringAcceptsTheFailClosedVenomPowerProvider() throws Exception {
    assertNotNull(NachtweberRuntimeWiring.class.getDeclaredConstructor(
        java.util.function.LongSupplier.class,
        java.util.function.LongConsumer.class,
        VenomPowerProvider.class));
  }

  @Test void coordinatorExposesGameplayOnlyForTheExactBoundStore() {
    NachtweberRuntimeCoordinator<EqualStore> coordinator = new NachtweberRuntimeCoordinator<>();
    EqualStore boundStore = new EqualStore("world");
    EqualStore equalButDistinct = new EqualStore("world");
    java.util.UUID owner = java.util.UUID.fromString("90000000-0000-0000-0000-000000000003");
    NachtweberLedgerStoreRuntime runtime =
        NachtweberLedgerStoreRuntime.defaults(ignored -> { }, ignored -> 0.0);
    List<Integer> stacks = new ArrayList<>();
    coordinator.bind(boundStore, () -> runtime);

    assertTrue(coordinator.withGameplay(boundStore, gameplay -> stacks.add(
        gameplay.castBlackThread(new BlackThreadService.CastRequest(
            owner, 7, 2.0, ControlProfile.TargetKind.NORMAL, 0L, true))
            .orElseThrow().stacks())));
    assertFalse(coordinator.withGameplay(equalButDistinct, gameplay -> fail("wrong store")));
    assertEquals(List.of(2), stacks);

    assertTrue(coordinator.release(boundStore));
    assertFalse(coordinator.withGameplay(boundStore, gameplay -> fail("released store")));
  }

  @Test void boundGameplayFacadeKeepsShadowSwingReconciliationStoreScopedAndFailClosed() {
    java.util.UUID owner =
        java.util.UUID.fromString("90000000-0000-0000-0000-000000000004");
    NachtweberLedgerStoreRuntime first =
        NachtweberLedgerStoreRuntime.defaults(ignored -> { }, ignored -> 0.0);
    NachtweberLedgerStoreRuntime second =
        NachtweberLedgerStoreRuntime.defaults(ignored -> { }, ignored -> 0.0);

    assertEquals(ShadowSwingReconciliationController.StartStatus.STARTED,
        first.startShadowSwing(new ShadowSwingReconciliationController.StartRequest(
            owner, true, 0, 0, 0, 10, 0, 0, 0)).orElseThrow().status());
    assertEquals(ShadowSwingReconciliationController.StepStatus.NO_SESSION,
        second.stepShadowSwing(new ShadowSwingReconciliationController.StepRequest(
            owner, 1, 0, 0, 0, 0, 0, 100)).orElseThrow().status());
    assertEquals(ShadowSwingReconciliationController.StepStatus.CORRECTION,
        first.stepShadowSwing(new ShadowSwingReconciliationController.StepRequest(
            owner, 1, 0, 0, 0, 0, 0, 100)).orElseThrow().status());

    first.close();
    assertTrue(first.startShadowSwing(new ShadowSwingReconciliationController.StartRequest(
        owner, true, 0, 0, 0, 10, 0, 0, 500)).isEmpty());
    assertTrue(first.stepShadowSwing(new ShadowSwingReconciliationController.StepRequest(
        owner, 1, 0, 0, 0, 0, 0, 500)).isEmpty());
    assertFalse(first.cancelShadowSwing(owner));
  }

  @Test void boundGameplayFacadeSharesStateWithinOneStoreAndNeverAcrossStores() {
    List<String> emitted = new ArrayList<>();
    java.util.UUID owner = java.util.UUID.fromString("90000000-0000-0000-0000-000000000002");
    NachtweberLedgerStoreRuntime first = NachtweberLedgerStoreRuntime.defaults(ignored -> { }, ignored -> 0.0);
    NachtweberLedgerStoreRuntime second = NachtweberLedgerStoreRuntime.defaults(ignored -> { }, ignored -> 0.0);

    var firstThread = first.castBlackThread(new BlackThreadService.CastRequest(
        owner, 42, 4.0, ControlProfile.TargetKind.NORMAL, 0L, true));
    var secondThread = first.castBlackThread(new BlackThreadService.CastRequest(
        owner, 42, 4.0, ControlProfile.TargetKind.NORMAL, 3_000L, true));
    assertEquals(2, firstThread.orElseThrow().stacks());
    assertTrue(secondThread.orElseThrow().bound());

    var isolatedCocoon = second.castHuntingCocoon(new HuntingCocoonService.CastRequest(
        owner, 42, 4.0, 3_001L, true, VenomDamageCause.DIRECT_HIT, () -> false));
    assertEquals(HuntingCocoonService.CastStatus.REJECTED_INSUFFICIENT_ENTANGLEMENT,
        isolatedCocoon.orElseThrow().status());

    var cocoon = first.castHuntingCocoon(new HuntingCocoonService.CastRequest(
        owner, 42, 4.0, 3_001L, true, VenomDamageCause.DIRECT_HIT, () -> false));
    assertEquals(4, cocoon.orElseThrow().consumedEntanglementStacks());
    var passive = first.applyPassiveVenom(new PassiveVenomRequest(
        owner, 42, true, true, true, VenomDamageCause.DIRECT_HIT, 3_002L, () -> false));
    assertEquals(1, passive.orElseThrow().appliedStacks());

    var tick = first.tickVenom(42, owner, 4_002L, (source, target, damage, cause) -> {
      emitted.add(source + ":" + target + ":" + damage + ":" + cause);
      return true;
    });
    assertEquals(VenomTickService.TickStatus.EMITTED, tick.orElseThrow().status());
    assertEquals(3, tick.orElseThrow().stacks());
    assertEquals(List.of(owner + ":42:4.5:VENOM_TICK"), emitted);
    assertEquals(VenomTickService.TickStatus.NOT_DUE,
        second.tickVenom(42, owner, 4_002L, (source, target, damage, cause) -> true)
            .orElseThrow().status());

    first.close();
    assertTrue(first.castBlackThread(new BlackThreadService.CastRequest(
        owner, 42, 4.0, ControlProfile.TargetKind.NORMAL, 6_000L, true)).isEmpty());
    assertTrue(first.applyPassiveVenom(new PassiveVenomRequest(
        owner, 42, true, true, true, VenomDamageCause.DIRECT_HIT, 6_000L, () -> false)).isEmpty());
  }

  @Test void boundLedgerRuntimeExpiresStateReportsFirstTickOnceAndClosesFailClosed() {
    List<Long> firstTicks = new ArrayList<>();
    NachtweberLedgerStoreRuntime runtime =
        new NachtweberLedgerStoreRuntime(5, 3, 4, 1_000L, firstTicks::add);
    java.util.UUID owner = java.util.UUID.fromString("90000000-0000-0000-0000-000000000001");

    assertEquals(2, runtime.applyEntanglement(42, owner, 2, 1_000L, 0L));
    assertEquals(2, runtime.applyVenom(42, owner, 2, 1_000L, 0L));
    runtime.maintain(500L);
    runtime.maintain(1_001L);

    assertEquals(List.of(500L), firstTicks);
    assertEquals(0, runtime.entanglementStacks(42, owner, 1_001L));
    assertFalse(runtime.hasVenom(42, owner, 1_001L));

    runtime.close();
    runtime.close();
    assertEquals(0, runtime.applyEntanglement(42, owner, 1, 1_000L, 2_000L));
    assertEquals(0, runtime.applyVenom(42, owner, 1, 1_000L, 2_000L));
  }

  private static final class RecordingRegistrationPort
      implements NachtweberRuntimeWiring.RegistrationPort {
    private final List<NachtweberMaintenanceSystem> maintenanceSystems = new ArrayList<>();
    private final List<ShadowSwingMovementSystem> movementSystems = new ArrayList<>();
    private final List<VenomDamageSystem> venomDamageSystems = new ArrayList<>();
    private final List<Consumer<Store<EntityStore>>> worldStartHandlers = new ArrayList<>();
    private final List<Consumer<Store<EntityStore>>> worldRemovalHandlers = new ArrayList<>();
    private final List<BiConsumer<Store<EntityStore>, java.util.UUID>> playerDisconnectHandlers =
        new ArrayList<>();
    @Override public void registerMaintenance(NachtweberMaintenanceSystem system) {
      maintenanceSystems.add(system);
    }
    @Override public void registerShadowSwingMovement(ShadowSwingMovementSystem system) {
      movementSystems.add(system);
    }
    @Override public void registerVenomDamage(VenomDamageSystem system) {
      venomDamageSystems.add(system);
    }
    @Override public void registerWorldStart(Consumer<Store<EntityStore>> handler) {
      worldStartHandlers.add(handler);
    }
    @Override public void registerWorldRemoval(Consumer<Store<EntityStore>> handler) {
      worldRemovalHandlers.add(handler);
    }
    @Override public void registerPlayerDisconnect(
        BiConsumer<Store<EntityStore>, java.util.UUID> handler) {
      playerDisconnectHandlers.add(handler);
    }
  }

  private static final class RecordingRuntime implements NachtweberStoreRuntime {
    private final List<Long> maintenanceTimes = new ArrayList<>();
    private int closeCalls;
    @Override public void maintain(long nowMs) { maintenanceTimes.add(nowMs); }
    @Override public void close() { closeCalls++; }
  }

  private record EqualStore(String name) { }
}
