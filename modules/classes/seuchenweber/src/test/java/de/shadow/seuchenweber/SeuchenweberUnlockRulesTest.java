package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeuchenweberUnlockRulesTest {
  @Test void requiresCosmicRuinSpellbookAndPrestigeThirty() {
    assertFalse(SeuchenweberUnlockRules.isEligible(29, true));
    assertFalse(SeuchenweberUnlockRules.isEligible(30, false));
    assertTrue(SeuchenweberUnlockRules.isEligible(30, true));
    assertTrue(SeuchenweberUnlockRules.isEligible(100, true));
  }

  @Test void recognizesOnlyTheConfiguredUnlockWeapon() {
    assertTrue(SeuchenweberUnlockRules.isUnlockItem("ArcanePower_CosmicRuin_Spellbook"));
    assertFalse(SeuchenweberUnlockRules.isUnlockItem("Weapon_Spellbook_Grimoire_Brown"));
    assertFalse(SeuchenweberUnlockRules.isUnlockItem(null));
  }
}
