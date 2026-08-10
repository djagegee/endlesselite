package de.shadow.endlesselite.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ModuleCatalog(List<ModuleDescriptor> modules) {
  public ModuleCatalog {
    modules = List.copyOf(modules);
    Set<String> ids = new HashSet<>();
    for (ModuleDescriptor module : modules) {
      if (!ids.add(module.id())) throw new IllegalArgumentException("Duplicate module id: " + module.id());
    }
  }

  public static ModuleCatalog integrated() {
    return new ModuleCatalog(List.of(
        new ModuleDescriptor("endless-elite", "Endless Elite Core", "0.1.0", Set.of("lifecycle", "ownership", "localization"), true),
        new ModuleDescriptor("endless-book", "EndlessBook", "2.0.0", Set.of("ui", "quests", "player-menu"), true),
        new ModuleDescriptor("hymann", "Hymann", "0.1.9", Set.of("class", "progression", "combat"), true),
        new ModuleDescriptor("nachtweber", "Nachtweber", "0.1.0", Set.of("class", "progression", "combat", "movement"), true),
        new ModuleDescriptor("seuchenweber", "Seuchenweber", "0.1.0", Set.of("class", "progression", "combat", "config"), true),
        new ModuleDescriptor("rift-mage-dungeon", "Rift Mage Dungeon", "0.1.1", Set.of("dungeon", "assets", "waves"), false),
        new ModuleDescriptor("portal-spawn", "Portal Spawn Snapshot", "unversioned", Set.of("prefabs", "recovery", "assets"), false),
        new ModuleDescriptor("mjolnir-safety-patch", "Mjolnir Safety Patch", "1.0.0", Set.of("patchly", "safety", "assets"), false)));
  }
}
