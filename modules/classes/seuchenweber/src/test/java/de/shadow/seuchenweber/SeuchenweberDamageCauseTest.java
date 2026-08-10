package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SeuchenweberDamageCauseTest {
  @Test void usesTheVerifiedBundledPoisonDamageCauseId() {
    assertEquals("Poison", SeuchenweberDamageCause.ID);
  }
}
