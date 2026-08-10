package de.shadow.endlesselite.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EndlessEliteCoreTest {
  @Test
  void catalogContainsEveryIntegratedModuleExactlyOnce() {
    ModuleCatalog catalog = ModuleCatalog.integrated();
    assertEquals(List.of("endless-elite", "endless-book", "hymann", "nachtweber", "seuchenweber", "rift-mage-dungeon"),
        catalog.modules().stream().map(ModuleDescriptor::id).toList());
    assertEquals(6, catalog.modules().stream().map(ModuleDescriptor::id).distinct().count());
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
