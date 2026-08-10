package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NekrotoxinDamageSystemTest {
  @Test void scalesOneServerTickLinearlyWithOwnerLocalStacks() {
    assertEquals(6.0f, NekrotoxinDamageSystem.damageForTick(6.0f, 1));
    assertEquals(18.0f, NekrotoxinDamageSystem.damageForTick(6.0f, 3));
  }

  @Test void appliesNecrotoxicMasteryOnlyForTheOwningPlayerUnlock() {
    assertEquals(18.0f, NekrotoxinDamageSystem.damageForTick(6.0f, 3, false, 1.25));
    assertEquals(22.5f, NekrotoxinDamageSystem.damageForTick(6.0f, 3, true, 1.25));
  }

  @Test void boundsAstralEchoPropagationToUnlockedNonEchoTicksAndConfiguredCap() {
    assertEquals(true, NekrotoxinDamageSystem.shouldApplyAstralEcho(true, true, 1, 0));
    assertEquals(false, NekrotoxinDamageSystem.shouldApplyAstralEcho(false, true, 1, 0));
    assertEquals(false, NekrotoxinDamageSystem.shouldApplyAstralEcho(true, false, 1, 0));
    assertEquals(false, NekrotoxinDamageSystem.shouldApplyAstralEcho(true, true, 1, 1));
    assertEquals(false, NekrotoxinDamageSystem.shouldApplyAstralEcho(true, true, 32, 16));
  }

  @Test void throttlesSoulDiagnosisAndRequiresOwnerUnlockAndStrongMark() {
    assertEquals(true, NekrotoxinDamageSystem.shouldReportSoulDiagnosis(true, 3, 3, 5_000L, 4_999L));
    assertEquals(false, NekrotoxinDamageSystem.shouldReportSoulDiagnosis(false, 5, 3, 5_000L, 0L));
    assertEquals(false, NekrotoxinDamageSystem.shouldReportSoulDiagnosis(true, 2, 3, 5_000L, 0L));
    assertEquals(false, NekrotoxinDamageSystem.shouldReportSoulDiagnosis(true, 3, 3, 5_000L, 5_001L));
  }

  @Test void cooldownRefundNeverCreatesNegativeRemainingTime() {
    assertEquals(3_800L, NekrotoxinDamageSystem.remainingCooldownAfterRefund(10_000L, 5_000L, 1_200L));
    assertEquals(0L, NekrotoxinDamageSystem.remainingCooldownAfterRefund(6_000L, 5_000L, 1_200L));
    assertEquals(0L, NekrotoxinDamageSystem.remainingCooldownAfterRefund(4_000L, 5_000L, 1_200L));
  }

  @Test void neverEmitsNegativeOrNanDamage() {
    assertEquals(0.0f, NekrotoxinDamageSystem.damageForTick(-4.0f, 3));
    assertEquals(0.0f, NekrotoxinDamageSystem.damageForTick(Float.NaN, 3));
    assertEquals(0.0f, NekrotoxinDamageSystem.damageForTick(6.0f, 0));
  }
}
