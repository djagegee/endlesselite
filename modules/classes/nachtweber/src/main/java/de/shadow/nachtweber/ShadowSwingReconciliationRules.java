package de.shadow.nachtweber;

record ShadowSwingReconciliationRules(
    long maximumSessionMs,
    double maximumAnchorDistanceBlocks,
    double maximumTetherDistanceBlocks,
    double releaseDistanceBlocks,
    double pullSpeedBlocksPerSecond,
    double maximumVelocityCorrectionPerStep,
    long minimumReattachIntervalMs) {
  ShadowSwingReconciliationRules {
    if (maximumSessionMs < 1
        || !Double.isFinite(maximumTetherDistanceBlocks)
        || !Double.isFinite(maximumAnchorDistanceBlocks)
        || !Double.isFinite(releaseDistanceBlocks)
        || !Double.isFinite(pullSpeedBlocksPerSecond)
        || !Double.isFinite(maximumVelocityCorrectionPerStep)
        || maximumAnchorDistanceBlocks <= 0
        || maximumTetherDistanceBlocks <= maximumAnchorDistanceBlocks
        || releaseDistanceBlocks <= 0
        || releaseDistanceBlocks >= maximumTetherDistanceBlocks
        || pullSpeedBlocksPerSecond <= 0
        || maximumVelocityCorrectionPerStep <= 0
        || minimumReattachIntervalMs < 0) {
      throw new IllegalArgumentException("invalid shadow swing reconciliation rules");
    }
  }
}
