package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import org.junit.jupiter.api.Test;

class SeuchenweberClassDefinitionTest {
  @Test void exposesARegisteredEndlessLevelingClassDefinition() {
    CharacterClassDefinition definition = SeuchenweberClassDefinition.create();

    assertEquals("elite_plagueweaver", definition.getId());
    assertEquals("Seuchenweber", definition.getDisplayName());
    assertEquals("Magic", definition.getDamageType());
    assertEquals("range", definition.getRangeType());
    assertTrue(definition.isEnabled());
    assertTrue(definition.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase("Mage")));
    assertEquals("ArcanePower_CosmicRuin_Spellbook", definition.getIconItemId());
    assertTrue(definition.getWeaponMultipliers().containsKey("spellbook"));
  }
}
