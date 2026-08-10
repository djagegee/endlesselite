package de.shadow.seuchenweber;

/** Defensive target budget for one Astral Echo proc. */
final class AstralEchoBudget {
  static final int GLOBAL_MAX_TARGETS = 16;

  private AstralEchoBudget() { }

  static int boundedTargetLimit(int configuredLimit) {
    if (configuredLimit <= 0) return 0;
    return Math.min(configuredLimit, GLOBAL_MAX_TARGETS);
  }
}
