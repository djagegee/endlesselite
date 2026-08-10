package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class ShadowSwingReconciliationController {
  enum StartStatus {
    STARTED, INVALID_REQUEST, NOT_AUTHORIZED, ANCHOR_OUT_OF_RANGE,
    REATTACH_RATE_LIMIT, CLOSED
  }
  enum StepStatus {
    CORRECTION, NO_SESSION, EXPIRED, REACHED, TETHER_BROKEN,
    STALE_STEP, INVALID_REQUEST
  }

  record StartRequest(
      UUID owner,
      boolean serverVerified,
      double playerX,
      double playerY,
      double playerZ,
      double anchorX,
      double anchorY,
      double anchorZ,
      long nowMs) { }

  record StartOutcome(StartStatus status) { }

  record StepRequest(
      UUID owner,
      double playerX,
      double playerY,
      double playerZ,
      double velocityX,
      double velocityY,
      double velocityZ,
      long nowMs) { }

  record StepOutcome(
      StepStatus status,
      double correctionX,
      double correctionY,
      double correctionZ) {
    static StepOutcome withoutCorrection(StepStatus status) {
      return new StepOutcome(status, 0, 0, 0);
    }
  }

  private record Session(
      double anchorX, double anchorY, double anchorZ,
      long startedAtMs, long lastStepAtMs) { }

  private final ShadowSwingReconciliationRules rules;
  private final Map<UUID, Session> sessions = new HashMap<>();
  private final Map<UUID, Long> reattachReadyAt = new HashMap<>();
  private boolean closed;

  ShadowSwingReconciliationController(ShadowSwingReconciliationRules rules) {
    this.rules = Objects.requireNonNull(rules, "rules");
  }

  synchronized StartOutcome start(StartRequest request) {
    if (closed) return new StartOutcome(StartStatus.CLOSED);
    if (!valid(request)) return new StartOutcome(StartStatus.INVALID_REQUEST);
    if (!request.serverVerified()) return new StartOutcome(StartStatus.NOT_AUTHORIZED);
    double anchorDistance = distance(
        request.playerX(), request.playerY(), request.playerZ(),
        request.anchorX(), request.anchorY(), request.anchorZ());
    if (!Double.isFinite(anchorDistance)) {
      return new StartOutcome(StartStatus.INVALID_REQUEST);
    }
    if (anchorDistance > rules.maximumAnchorDistanceBlocks()) {
      return new StartOutcome(StartStatus.ANCHOR_OUT_OF_RANGE);
    }
    long currentReadyAt = reattachReadyAt.getOrDefault(request.owner(), 0L);
    if (request.nowMs() < currentReadyAt) {
      return new StartOutcome(StartStatus.REATTACH_RATE_LIMIT);
    }
    sessions.put(request.owner(), new Session(
        request.anchorX(), request.anchorY(), request.anchorZ(),
        request.nowMs(), request.nowMs()));
    reattachReadyAt.put(request.owner(), TimeMath.saturatedAdd(
        request.nowMs(), rules.minimumReattachIntervalMs()));
    return new StartOutcome(StartStatus.STARTED);
  }

  synchronized StepOutcome step(StepRequest request) {
    if (closed) return StepOutcome.withoutCorrection(StepStatus.NO_SESSION);
    if (request == null || request.owner() == null) {
      return StepOutcome.withoutCorrection(StepStatus.INVALID_REQUEST);
    }
    Session session = sessions.get(request.owner());
    if (session == null) return StepOutcome.withoutCorrection(StepStatus.NO_SESSION);
    if (request.nowMs() < 0 || !finite(
        request.playerX(), request.playerY(), request.playerZ(),
        request.velocityX(), request.velocityY(), request.velocityZ())) {
      sessions.remove(request.owner());
      return StepOutcome.withoutCorrection(StepStatus.INVALID_REQUEST);
    }
    if (request.nowMs() < session.lastStepAtMs()) {
      return StepOutcome.withoutCorrection(StepStatus.STALE_STEP);
    }
    if (request.nowMs() > TimeMath.saturatedAdd(
        session.startedAtMs(), rules.maximumSessionMs())) {
      sessions.remove(request.owner());
      return StepOutcome.withoutCorrection(StepStatus.EXPIRED);
    }
    double dx = session.anchorX() - request.playerX();
    double dy = session.anchorY() - request.playerY();
    double dz = session.anchorZ() - request.playerZ();
    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (distance > rules.maximumTetherDistanceBlocks()) {
      sessions.remove(request.owner());
      return StepOutcome.withoutCorrection(StepStatus.TETHER_BROKEN);
    }
    if (distance <= rules.releaseDistanceBlocks()) {
      sessions.remove(request.owner());
      return StepOutcome.withoutCorrection(StepStatus.REACHED);
    }
    double desiredX = dx / distance * rules.pullSpeedBlocksPerSecond();
    double desiredY = dy / distance * rules.pullSpeedBlocksPerSecond();
    double desiredZ = dz / distance * rules.pullSpeedBlocksPerSecond();
    double correctionX = desiredX - request.velocityX();
    double correctionY = desiredY - request.velocityY();
    double correctionZ = desiredZ - request.velocityZ();
    double correctionLength = Math.sqrt(
        correctionX * correctionX + correctionY * correctionY + correctionZ * correctionZ);
    if (correctionLength > rules.maximumVelocityCorrectionPerStep()) {
      double factor = rules.maximumVelocityCorrectionPerStep() / correctionLength;
      correctionX *= factor;
      correctionY *= factor;
      correctionZ *= factor;
    }
    sessions.put(request.owner(), new Session(
        session.anchorX(), session.anchorY(), session.anchorZ(),
        session.startedAtMs(), request.nowMs()));
    return new StepOutcome(
        StepStatus.CORRECTION, correctionX, correctionY, correctionZ);
  }

  synchronized int activeSessionCount() { return sessions.size(); }

  synchronized boolean cancel(UUID owner) {
    return owner != null && sessions.remove(owner) != null;
  }

  synchronized void cleanseOwner(UUID owner) {
    if (owner == null) return;
    sessions.remove(owner);
    reattachReadyAt.remove(owner);
  }

  synchronized void close() {
    if (closed) return;
    closed = true;
    sessions.clear();
    reattachReadyAt.clear();
  }

  private static boolean valid(StartRequest request) {
    return request != null
        && request.owner() != null
        && request.nowMs() >= 0
        && finite(request.playerX(), request.playerY(), request.playerZ())
        && finite(request.anchorX(), request.anchorY(), request.anchorZ());
  }

  private static boolean finite(double... values) {
    for (double value : values) if (!Double.isFinite(value)) return false;
    return true;
  }

  private static double distance(
      double fromX, double fromY, double fromZ,
      double toX, double toY, double toZ) {
    double dx = toX - fromX;
    double dy = toY - fromY;
    double dz = toZ - fromZ;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }
}
