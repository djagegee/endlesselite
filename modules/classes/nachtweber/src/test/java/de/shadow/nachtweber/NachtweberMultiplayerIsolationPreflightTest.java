package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class NachtweberMultiplayerIsolationPreflightTest {
  private static final UUID OWNER_A =
      UUID.fromString("a0000000-0000-0000-0000-000000000001");
  private static final UUID OWNER_B =
      UUID.fromString("b0000000-0000-0000-0000-000000000002");

  @Test
  void cleanupOwnerRemovesOnlyThatOwnersStateFromTheExactStore() {
    NachtweberLedgerStoreRuntime first =
        NachtweberLedgerStoreRuntime.defaults(ignored -> { }, ignored -> 0.0);
    NachtweberLedgerStoreRuntime second =
        NachtweberLedgerStoreRuntime.defaults(ignored -> { }, ignored -> 0.0);

    seedOwner(first, OWNER_A);
    seedOwner(first, OWNER_B);
    seedOwner(second, OWNER_A);

    assertTrue(first.cleanupOwner(OWNER_A));

    assertEquals(0, first.entanglementStacks(42, OWNER_A, 1L));
    assertFalse(first.hasVenom(42, OWNER_A, 1L));
    assertEquals(0, first.entanglementStacks(43, OWNER_A, 1L));
    assertFalse(first.hasVenom(44, OWNER_A, 1L));
    assertEquals(0, first.entanglementStacks(45, OWNER_A, 1L));

    assertEquals(0, first.entanglementStacks(42, OWNER_B, 1L));
    assertTrue(first.hasVenom(42, OWNER_B, 1L));
    assertEquals(2, first.entanglementStacks(43, OWNER_B, 1L));
    assertTrue(first.hasVenom(44, OWNER_B, 1L));

    assertEquals(BlackThreadService.CastStatus.APPLIED,
        first.castBlackThread(thread(OWNER_A, 43, 1L)).orElseThrow().status());
    assertEquals(BlackThreadService.CastStatus.REJECTED_COOLDOWN,
        first.castBlackThread(thread(OWNER_B, 43, 1L)).orElseThrow().status());

    assertEquals(PassiveVenomStatus.APPLIED,
        first.applyPassiveVenom(passive(OWNER_A, 44, 1L)).orElseThrow().status());
    assertEquals(PassiveVenomStatus.COOLDOWN,
        first.applyPassiveVenom(passive(OWNER_B, 44, 1L)).orElseThrow().status());

    assertEquals(HuntingCocoonService.CastStatus.REJECTED_INSUFFICIENT_ENTANGLEMENT,
        first.castHuntingCocoon(cocoon(OWNER_A, 42, 1L)).orElseThrow().status());
    assertEquals(HuntingCocoonService.CastStatus.REJECTED_COOLDOWN,
        first.castHuntingCocoon(cocoon(OWNER_B, 42, 1L)).orElseThrow().status());

    assertEquals(ShadowSwingReconciliationController.StepStatus.NO_SESSION,
        first.stepShadowSwing(step(OWNER_A, 100L)).orElseThrow().status());
    assertEquals(ShadowSwingReconciliationController.StepStatus.CORRECTION,
        first.stepShadowSwing(step(OWNER_B, 100L)).orElseThrow().status());

    assertEquals(0, second.entanglementStacks(42, OWNER_A, 1L));
    assertTrue(second.hasVenom(42, OWNER_A, 1L));
    assertEquals(2, second.entanglementStacks(43, OWNER_A, 1L));
    assertTrue(second.hasVenom(44, OWNER_A, 1L));
    assertEquals(ShadowSwingReconciliationController.StepStatus.CORRECTION,
        second.stepShadowSwing(step(OWNER_A, 100L)).orElseThrow().status());

    first.close();
    assertFalse(first.cleanupOwner(OWNER_A));
  }

  private static void seedOwner(NachtweberLedgerStoreRuntime runtime, UUID owner) {
    assertEquals(3, runtime.applyEntanglement(42, owner, 3, 8_000L, 0L));
    assertEquals(1, runtime.applyVenom(42, owner, 1, 8_000L, 0L));
    assertEquals(2, runtime.castBlackThread(thread(owner, 43, 0L)).orElseThrow().stacks());
    assertEquals(PassiveVenomStatus.APPLIED,
        runtime.applyPassiveVenom(passive(owner, 44, 0L)).orElseThrow().status());
    assertEquals(HuntingCocoonService.CastStatus.APPLIED,
        runtime.castHuntingCocoon(cocoon(owner, 42, 0L)).orElseThrow().status());
    assertEquals(ShadowSwingReconciliationController.StartStatus.STARTED,
        runtime.startShadowSwing(new ShadowSwingReconciliationController.StartRequest(
            owner, true, 0, 0, 0, 10, 0, 0, 0L)).orElseThrow().status());
  }

  private static BlackThreadService.CastRequest thread(UUID owner, int target, long nowMs) {
    return new BlackThreadService.CastRequest(
        owner, target, 2.0, ControlProfile.TargetKind.NORMAL, nowMs, true);
  }

  private static PassiveVenomRequest passive(UUID owner, int target, long nowMs) {
    return new PassiveVenomRequest(
        owner, target, true, true, true, VenomDamageCause.DIRECT_HIT, nowMs, () -> false);
  }

  private static HuntingCocoonService.CastRequest cocoon(UUID owner, int target, long nowMs) {
    return new HuntingCocoonService.CastRequest(
        owner, target, 2.0, nowMs, true, VenomDamageCause.DIRECT_HIT, () -> false);
  }

  private static ShadowSwingReconciliationController.StepRequest step(UUID owner, long nowMs) {
    return new ShadowSwingReconciliationController.StepRequest(
        owner, 1, 0, 0, 0, 0, 0, nowMs);
  }
}
