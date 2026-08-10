package de.shadow.endlesselite.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
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
