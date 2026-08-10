package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DamageBudgetTest {
  @Test void combatProfileRequiresAtLeastSeventyPercentOfDamageFromNecrotoxinTicks() {
    assertTrue(DamageBudget.isDotDominant(30.0, 70.0, 0.70));
    assertTrue(DamageBudget.isDotDominant(20.0, 80.0, 0.70));
    assertFalse(DamageBudget.isDotDominant(31.0, 69.0, 0.70));
  }

  @Test void emptyDamageIsNeverAcceptedAsACombatProfile() {
    assertFalse(DamageBudget.isDotDominant(0.0, 0.0, 0.70));
  }
}
