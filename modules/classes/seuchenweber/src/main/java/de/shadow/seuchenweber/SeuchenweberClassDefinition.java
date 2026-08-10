package de.shadow.seuchenweber;

import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import java.util.List;
import java.util.Map;

final class SeuchenweberClassDefinition {
  static final String ID = "elite_plagueweaver";

  private SeuchenweberClassDefinition() { }

  static CharacterClassDefinition create() {
    return new CharacterClassDefinition(
        ID,
        "Seuchenweber",
        "A plague-weaving control mage who binds Nekrotoxin to its owner and targets.",
        List.of("Mage"),
        "Magic",
        "range",
        "COMBAT",
        true,
        SeuchenweberUnlockRules.UNLOCK_ITEM_ID,
        Map.of(
            "spellbook", 1.12,
            "staff", 1.08,
            "wand", 1.05),
        List.of(),
        List.of(),
        null,
        "seuchenweber.use");
  }
}
