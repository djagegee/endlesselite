package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RiftPulseBudgetTest {
  @Test void clampsConfiguredPulseTargetsToTheGlobalSafetyLimit() {
    assertEquals(8, RiftPulseBudget.boundedTargetLimit(8));
    assertEquals(16, RiftPulseBudget.boundedTargetLimit(99));
  }

  @Test void rejectsNonPositivePulseTargetLimits() {
    assertEquals(0, RiftPulseBudget.boundedTargetLimit(0));
    assertEquals(0, RiftPulseBudget.boundedTargetLimit(-1));
  }
}
