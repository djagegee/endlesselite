package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class ShadowSwingReconciliationControllerTest {
  private static final UUID OWNER_A =
      UUID.fromString("a0000000-0000-0000-0000-000000000001");
  private static final UUID OWNER_B =
      UUID.fromString("a0000000-0000-0000-0000-000000000002");

  @Test void configPublishesReconciliationBoundsWithExplicitUnits() throws Exception {
    String config;
    try (var input = getClass().getResourceAsStream("/config/nachtweber.json")) {
      assertNotNull(input);
      config = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
    assertAll(
        () -> assertTrue(config.contains("\"maximumSessionSeconds\": 1.5")),
        () -> assertTrue(config.contains("\"maximumTetherDistanceBlocks\": 22.0")),
        () -> assertTrue(config.contains("\"releaseDistanceBlocks\": 1.25")),
        () -> assertTrue(config.contains("\"pullSpeedBlocksPerSecond\": 14.0")),
        () -> assertTrue(config.contains(
            "\"maximumVelocityCorrectionPerStepBlocksPerSecond\": 3.0")),
        () -> assertTrue(config.contains("\"minimumReattachIntervalSeconds\": 0.25")));
  }

  @Test void rejectsStaleStepsAndDropsNonFiniteServerStateFailClosed() {
    ShadowSwingReconciliationController controller = controller();
    controller.start(new ShadowSwingReconciliationController.StartRequest(
        OWNER_A, true, 0, 0, 0, 10, 0, 0, 100));

    assertEquals(ShadowSwingReconciliationController.StepStatus.STALE_STEP,
        controller.step(new ShadowSwingReconciliationController.StepRequest(
            OWNER_A, 1, 0, 0, 0, 0, 0, 99)).status());
    assertEquals(1, controller.activeSessionCount());

    assertEquals(ShadowSwingReconciliationController.StepStatus.INVALID_REQUEST,
        controller.step(new ShadowSwingReconciliationController.StepRequest(
            OWNER_A, 1, 0, 0, Double.NaN, 0, 0, 101)).status());
    assertEquals(0, controller.activeSessionCount());
  }

  @Test void rateLimitsReattachAndCleansOwnersWithoutCrossOwnerMutation() {
    ShadowSwingReconciliationController controller = controller();
    assertEquals(ShadowSwingReconciliationController.StartStatus.STARTED,
        controller.start(new ShadowSwingReconciliationController.StartRequest(
            OWNER_A, true, 0, 0, 0, 10, 0, 0, 0)).status());
    assertEquals(ShadowSwingReconciliationController.StartStatus.REATTACH_RATE_LIMIT,
        controller.start(new ShadowSwingReconciliationController.StartRequest(
            OWNER_A, true, 0, 0, 0, 0, 10, 0, 249)).status());
    assertEquals(ShadowSwingReconciliationController.StartStatus.STARTED,
        controller.start(new ShadowSwingReconciliationController.StartRequest(
            OWNER_A, true, 0, 0, 0, 0, 10, 0, 250)).status());
    assertEquals(ShadowSwingReconciliationController.StartStatus.STARTED,
        controller.start(new ShadowSwingReconciliationController.StartRequest(
            OWNER_B, true, 0, 0, 0, 10, 0, 0, 250)).status());

    assertTrue(controller.cancel(OWNER_A));
    assertFalse(controller.cancel(OWNER_A));
    assertEquals(1, controller.activeSessionCount());
    controller.cleanseOwner(OWNER_A);
    assertEquals(1, controller.activeSessionCount());

    controller.close();
    assertEquals(0, controller.activeSessionCount());
    assertEquals(ShadowSwingReconciliationController.StartStatus.CLOSED,
        controller.start(new ShadowSwingReconciliationController.StartRequest(
            OWNER_A, true, 0, 0, 0, 10, 0, 0, 1_000)).status());
  }

  @Test void rejectsAStartBeyondAnchorRangeAndBreaksAnOverstretchedTether() {
    ShadowSwingReconciliationController controller = controller();

    assertEquals(ShadowSwingReconciliationController.StartStatus.ANCHOR_OUT_OF_RANGE,
        controller.start(new ShadowSwingReconciliationController.StartRequest(
            OWNER_A, true, 0, 0, 0, 18.01, 0, 0, 0)).status());
    assertEquals(0, controller.activeSessionCount());

    assertEquals(ShadowSwingReconciliationController.StartStatus.STARTED,
        controller.start(new ShadowSwingReconciliationController.StartRequest(
            OWNER_A, true, 0, 0, 0, 10, 0, 0, 0)).status());
    assertEquals(ShadowSwingReconciliationController.StepStatus.TETHER_BROKEN,
        controller.step(new ShadowSwingReconciliationController.StepRequest(
            OWNER_A, -12.01, 0, 0, 0, 0, 0, 100)).status());
    assertEquals(0, controller.activeSessionCount());
  }

  @Test void stepClampsVelocityCorrectionTowardAnchorAndCompletesAtReleaseDistance() {
    ShadowSwingReconciliationController controller = controller();
    controller.start(new ShadowSwingReconciliationController.StartRequest(
        OWNER_A, true, 0, 0, 0, 10, 0, 0, 0));

    var pull = controller.step(new ShadowSwingReconciliationController.StepRequest(
        OWNER_A, 5, 0, 0, 0, 0, 0, 100));
    assertEquals(ShadowSwingReconciliationController.StepStatus.CORRECTION, pull.status());
    assertEquals(3.0, pull.correctionX(), 0.000001);
    assertEquals(0.0, pull.correctionY(), 0.000001);
    assertEquals(0.0, pull.correctionZ(), 0.000001);

    var brake = controller.step(new ShadowSwingReconciliationController.StepRequest(
        OWNER_A, 5, 0, 0, 20, 0, 0, 200));
    assertEquals(-3.0, brake.correctionX(), 0.000001);

    var reached = controller.step(new ShadowSwingReconciliationController.StepRequest(
        OWNER_A, 9, 0, 0, 0, 0, 0, 300));
    assertEquals(ShadowSwingReconciliationController.StepStatus.REACHED, reached.status());
    assertEquals(0, controller.activeSessionCount());
  }

  @Test void sessionIsOwnerIsolatedAndExpiresDeterministically() {
    ShadowSwingReconciliationController controller = controller();

    var started = controller.start(new ShadowSwingReconciliationController.StartRequest(
        OWNER_A, true, 0, 0, 0, 10, 0, 0, 0));
    assertEquals(ShadowSwingReconciliationController.StartStatus.STARTED, started.status());
    assertEquals(1, controller.activeSessionCount());

    var foreign = controller.step(new ShadowSwingReconciliationController.StepRequest(
        OWNER_B, 1, 0, 0, 0, 0, 0, 500));
    assertEquals(ShadowSwingReconciliationController.StepStatus.NO_SESSION, foreign.status());

    var expired = controller.step(new ShadowSwingReconciliationController.StepRequest(
        OWNER_A, 1, 0, 0, 0, 0, 0, 1_501));
    assertEquals(ShadowSwingReconciliationController.StepStatus.EXPIRED, expired.status());
    assertEquals(0, controller.activeSessionCount());
    assertEquals(ShadowSwingReconciliationController.StepStatus.NO_SESSION,
        controller.step(new ShadowSwingReconciliationController.StepRequest(
            OWNER_A, 1, 0, 0, 0, 0, 0, 1_502)).status());
  }

  private static ShadowSwingReconciliationController controller() {
    return new ShadowSwingReconciliationController(new ShadowSwingReconciliationRules(
        1_500, 18.0, 22.0, 1.25, 14.0, 3.0, 250));
  }
}
