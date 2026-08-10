package de.shadow.nachtweber;

import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import java.util.List;
import java.util.Map;

final class NachtweberClassDefinition {
  static final String ID = "elite_nightweaver";
  static final String PERMISSION = "nachtweber.use";

  private NachtweberClassDefinition() { }

  static CharacterClassDefinition create() {
    return new CharacterClassDefinition(
        ID,
        "Nachtweber",
        "A dark hunter who binds prey with black thread and wears it down with venom.",
        List.of("Assassin"),
        "Physical",
        "melee",
        "COMBAT",
        true,
        null,
        Map.of(),
        List.of(),
        List.of(),
        null,
        PERMISSION);
  }
}
