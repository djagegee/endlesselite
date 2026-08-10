package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AstralEchoBudgetTest {
  @Test void honorsConfiguredLimitAndGlobalHardCap() {
    assertEquals(1, AstralEchoBudget.boundedTargetLimit(1));
    assertEquals(8, AstralEchoBudget.boundedTargetLimit(8));
    assertEquals(16, AstralEchoBudget.boundedTargetLimit(32));
  }

  @Test void invalidLimitsFailClosed() {
    assertEquals(0, AstralEchoBudget.boundedTargetLimit(0));
    assertEquals(0, AstralEchoBudget.boundedTargetLimit(-1));
  }
}
