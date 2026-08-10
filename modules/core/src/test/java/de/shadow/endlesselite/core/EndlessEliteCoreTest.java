package de.shadow.endlesselite.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EndlessEliteCoreTest {
  @Test
  void catalogContainsEveryIntegratedModuleExactlyOnce() {
    ModuleCatalog catalog = ModuleCatalog.integrated();
    assertEquals(List.of("endless-elite", "endless-book", "hymann", "nachtweber", "seuchenweber", "rift-mage-dungeon", "portal-spawn", "mjolnir-safety-patch"),
        catalog.modules().stream().map(ModuleDescriptor::id).toList());
    assertEquals(8, catalog.modules().stream().map(ModuleDescriptor::id).distinct().count());
  }

  @Test
  void gameplayConcernRejectsCompetingOwners() {
    GameplayOwnership ownership = new GameplayOwnership();
    ownership.claim(GameplayConcern.MOB_SCALING, "endless-elite-mobs");
    IllegalStateException error = assertThrows(IllegalStateException.class,
        () -> ownership.claim(GameplayConcern.MOB_SCALING, "mmo-skill-tree"));
    assertTrue(error.getMessage().contains("MOB_SCALING"));
  }

  @Test
  void coordinatorRollsBackStartedModulesInReverseOrder() {
    List<String> calls = new ArrayList<>();
    ManagedModule first = module("first", calls, false);
    ManagedModule second = module("second", calls, true);
    ModuleCoordinator coordinator = new ModuleCoordinator(List.of(first, second));

    assertThrows(IllegalStateException.class, coordinator::startAll);
    assertEquals(List.of("start:first", "start:second", "stop:first"), calls);
  }

  @Test
  void stopAllContinuesAfterErrorAndClearsStartedState() {
    List<String> calls = new ArrayList<>();
    ManagedModule first = new ManagedModule() {
      @Override public String id() { return "first"; }
      @Override public void start() { calls.add("start:first"); }
      @Override public void stop() { calls.add("stop:first"); }
    };
    ManagedModule second = new ManagedModule() {
      @Override public String id() { return "second"; }
      @Override public void start() { calls.add("start:second"); }
      @Override public void stop() {
        calls.add("stop:second");
        throw new AssertionError("stop failed");
      }
    };
    ModuleCoordinator coordinator = new ModuleCoordinator(List.of(first, second));

    coordinator.startAll();
    assertThrows(AssertionError.class, coordinator::stopAll);
    assertEquals(List.of("start:first", "start:second", "stop:second", "stop:first"), calls);

    coordinator.startAll();
    assertEquals(List.of("start:first", "start:second", "stop:second", "stop:first",
        "start:first", "start:second"), calls);
  }

  @Test
  void rollbackPreservesStartFailureWhenCleanupThrowsError() {
    List<String> calls = new ArrayList<>();
    IllegalStateException startFailure = new IllegalStateException("start failed");
    ManagedModule first = new ManagedModule() {
      @Override public String id() { return "first"; }
      @Override public void start() { calls.add("start:first"); }
      @Override public void stop() {
        calls.add("stop:first");
        throw new AssertionError("cleanup failed");
      }
    };
    ManagedModule second = new ManagedModule() {
      @Override public String id() { return "second"; }
      @Override public void start() { calls.add("start:second"); }
      @Override public void stop() { calls.add("stop:second"); }
    };
    ManagedModule third = new ManagedModule() {
      @Override public String id() { return "third"; }
      @Override public void start() {
        calls.add("start:third");
        throw startFailure;
      }
      @Override public void stop() { calls.add("stop:third"); }
    };

    IllegalStateException thrown = assertThrows(IllegalStateException.class,
        () -> new ModuleCoordinator(List.of(first, second, third)).startAll());
    assertSame(startFailure, thrown);
    assertEquals(List.of("start:first", "start:second", "start:third", "stop:second", "stop:first"), calls);
    assertEquals(1, thrown.getSuppressed().length);
    assertTrue(thrown.getSuppressed()[0] instanceof AssertionError);
  }

  @Test
  void stopAllPreservesFirstFailureAndSuppressesLaterFailures() {
    List<String> calls = new ArrayList<>();
    IllegalStateException firstModuleFailure = new IllegalStateException("first stop failed");
    AssertionError secondModuleFailure = new AssertionError("second stop failed");
    ManagedModule first = module("first", calls, false, firstModuleFailure);
    ManagedModule second = module("second", calls, false, secondModuleFailure);
    ModuleCoordinator coordinator = new ModuleCoordinator(List.of(first, second));

    coordinator.startAll();
    AssertionError thrown = assertThrows(AssertionError.class, coordinator::stopAll);

    assertSame(secondModuleFailure, thrown);
    assertEquals(1, thrown.getSuppressed().length);
    assertSame(firstModuleFailure, thrown.getSuppressed()[0]);
    assertEquals(List.of("start:first", "start:second", "stop:second", "stop:first"), calls);
  }

  @Test
  void stopAllIgnoresSelfSuppressionAndContinuesCleanup() {
    List<String> calls = new ArrayList<>();
    AssertionError shared = new AssertionError("shared stop failure");
    ManagedModule tail = module("tail", calls, false);
    ManagedModule first = module("first", calls, false, shared);
    ManagedModule second = module("second", calls, false, shared);
    ModuleCoordinator coordinator = new ModuleCoordinator(List.of(tail, first, second));
    coordinator.startAll();

    AssertionError thrown = assertThrows(AssertionError.class, coordinator::stopAll);

    assertSame(shared, thrown);
    assertEquals(0, thrown.getSuppressed().length);
    assertEquals(List.of(
        "start:tail", "start:first", "start:second",
        "stop:second", "stop:first", "stop:tail"), calls);
  }

  @Test
  void ownedRegistryLifecycleRollsBackCollisionsAndStopsExactInstancesInReverseOrder() {
    Map<String, Object> registryValues = new LinkedHashMap<>();
    List<String> unregisterCalls = new ArrayList<>();
    Object foreign = new Object();
    registryValues.put("collision", foreign);
    var registry = new OwnedRegistryLifecycle.Registry<Object>() {
      @Override public boolean registerIfAbsent(String id, Object value) {
        return registryValues.putIfAbsent(id, value) == null;
      }
      @Override public boolean unregister(String id, Object expected) {
        unregisterCalls.add(id);
        return registryValues.remove(id, expected);
      }
    };
    Map<String, Object> requested = new LinkedHashMap<>();
    requested.put("first", new Object());
    requested.put("collision", new Object());
    requested.put("last", new Object());

    var rejected = new OwnedRegistryLifecycle<>(registry, requested);
    assertFalse(rejected.start());
    assertEquals(Map.of("collision", foreign), registryValues);
    assertEquals(List.of("collision", "first"), unregisterCalls);

    unregisterCalls.clear();
    registryValues.clear();
    var accepted = new OwnedRegistryLifecycle<>(registry, requested);
    assertTrue(accepted.start());
    assertEquals(3, accepted.ownedCount());
    accepted.shutdown();
    assertEquals(List.of("last", "collision", "first"), unregisterCalls);
    assertTrue(registryValues.isEmpty());
  }

  @Test
  void ownedRegistryLifecycleContinuesCleanupAndAvoidsSelfSuppression() {
    Error shared = new AssertionError("shared cleanup failure");
    List<String> attempted = new ArrayList<>();
    OwnedRegistryLifecycle.Registry<Object> registry = new OwnedRegistryLifecycle.Registry<>() {
      @Override public boolean registerIfAbsent(String id, Object value) { return true; }
      @Override public boolean unregister(String id, Object expectedValue) {
        attempted.add(id);
        if (!id.equals("first")) throw shared;
        return true;
      }
    };
    Map<String, Object> requested = new LinkedHashMap<>();
    requested.put("first", new Object());
    requested.put("second", new Object());
    requested.put("third", new Object());
    OwnedRegistryLifecycle<Object> lifecycle = new OwnedRegistryLifecycle<>(registry, requested);

    assertTrue(lifecycle.start());
    Error thrown = assertThrows(Error.class, lifecycle::shutdown);
    assertSame(shared, thrown);
    assertEquals(List.of("third", "second", "first"), attempted);
    assertEquals(0, thrown.getSuppressed().length);
    assertEquals(0, lifecycle.ownedCount());
  }

  @Test
  void registrationFailureAfterMutationIsCompensatedAndRemainsPrimary() {
    Error registrationFailure = new AssertionError("registration");
    Error cleanupFailure = new AssertionError("cleanup");
    Map<String, Object> values = new LinkedHashMap<>();
    List<String> unregisterOrder = new ArrayList<>();
    OwnedRegistryLifecycle.Registry<Object> registry = new OwnedRegistryLifecycle.Registry<>() {
      @Override public boolean registerIfAbsent(String id, Object value) {
        if (values.putIfAbsent(id, value) != null) return false;
        if (id.equals("failure")) throw registrationFailure;
        return true;
      }
      @Override public boolean unregister(String id, Object expectedValue) {
        unregisterOrder.add(id);
        boolean removed = values.remove(id, expectedValue);
        if (id.equals("failure")) throw cleanupFailure;
        return removed;
      }
    };
    Map<String, Object> requested = new LinkedHashMap<>();
    requested.put("owned", new Object());
    requested.put("failure", new Object());
    OwnedRegistryLifecycle<Object> lifecycle = new OwnedRegistryLifecycle<>(registry, requested);

    assertSame(registrationFailure, assertThrows(Error.class, lifecycle::start));
    assertEquals(List.of("failure", "owned"), unregisterOrder);
    assertTrue(values.isEmpty());
    assertEquals(0, lifecycle.ownedCount());
    assertEquals(1, registrationFailure.getSuppressed().length);
    assertSame(cleanupFailure, registrationFailure.getSuppressed()[0]);
  }

  @Test
  void collisionRollbackAttemptsEachOwnedEntryOnlyOnceWhenCleanupFails() {
    Error cleanupFailure = new AssertionError("collision cleanup");
    int[] unregisterCount = {0};
    OwnedRegistryLifecycle.Registry<Object> registry = new OwnedRegistryLifecycle.Registry<>() {
      @Override public boolean registerIfAbsent(String id, Object value) {
        return !id.equals("collision");
      }
      @Override public boolean unregister(String id, Object expectedValue) {
        unregisterCount[0]++;
        throw cleanupFailure;
      }
    };
    Map<String, Object> requested = new LinkedHashMap<>();
    requested.put("owned", new Object());
    requested.put("collision", new Object());
    OwnedRegistryLifecycle<Object> lifecycle = new OwnedRegistryLifecycle<>(registry, requested);

    assertSame(cleanupFailure, assertThrows(Error.class, lifecycle::start));
    assertEquals(2, unregisterCount[0]);
    assertEquals(0, lifecycle.ownedCount());
  }

  @Test
  void bestEffortCleanupRunsEveryActionAndPreservesFirstFailure() {
    Error shared = new AssertionError("first");
    RuntimeException later = new IllegalStateException("later");
    List<String> calls = new ArrayList<>();

    Error thrown = assertThrows(Error.class, () -> BestEffortCleanup.run(
        () -> { calls.add("first"); throw shared; },
        () -> { calls.add("same"); throw shared; },
        () -> { calls.add("later"); throw later; },
        () -> calls.add("tail")));

    assertSame(shared, thrown);
    assertEquals(List.of("first", "same", "later", "tail"), calls);
    assertEquals(1, thrown.getSuppressed().length);
    assertSame(later, thrown.getSuppressed()[0]);
  }

  @Test
  void ownedEffectAbiRequiresBothAdditiveMethodsBeforePluginMutation() {
    assertTrue(MmoOwnedEffectAbi.hasRequiredMethods(CompleteRegistry.class, Object.class));
    assertFalse(MmoOwnedEffectAbi.hasRequiredMethods(LegacyRegistry.class, Object.class));
    assertThrows(IllegalStateException.class,
        () -> MmoOwnedEffectAbi.requireMethods(LegacyRegistry.class, Object.class));
  }

  static final class CompleteRegistry {
    public boolean registerIfAbsent(String id, Object effect) { return true; }
    public boolean unregister(String id, Object effect) { return true; }
  }

  static final class LegacyRegistry {
    public void register(String id, Object effect) { }
  }

  private static ManagedModule module(String id, List<String> calls, boolean fail, Throwable stopFailure) {
    return new ManagedModule() {
      @Override public String id() { return id; }
      @Override public void start() {
        calls.add("start:" + id);
        if (fail) throw new IllegalStateException("boom");
      }
      @Override public void stop() {
        calls.add("stop:" + id);
        if (stopFailure instanceof RuntimeException runtime) throw runtime;
        if (stopFailure instanceof Error error) throw error;
      }
    };
  }

  private static ManagedModule module(String id, List<String> calls, boolean fail) {
    return new ManagedModule() {
      @Override public String id() { return id; }
      @Override public void start() {
        calls.add("start:" + id);
        if (fail) throw new IllegalStateException("boom");
      }
      @Override public void stop() { calls.add("stop:" + id); }
    };
  }
}
