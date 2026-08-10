package de.shadow.nachtweber;

import java.util.Set;

final class NachtweberAbilityCatalog {
  static final String CLASS_ID = "elite_nightweaver";
  private static final Set<String> ACTIVES = Set.of("BLACK_THREAD", "SHADOW_SWING", "HUNTING_COCOON");
  private static final Set<String> PASSIVES = Set.of("DANGER_SENSE", "WALL_HUNTER", "TOXIC_GLANDS", "HUNTING_INSTINCT");
  private NachtweberAbilityCatalog() { }
  static Set<String> activeIds() { return ACTIVES; }
  static Set<String> passiveIds() { return PASSIVES; }
}
