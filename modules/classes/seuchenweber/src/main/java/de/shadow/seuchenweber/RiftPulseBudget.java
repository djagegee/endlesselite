package de.shadow.seuchenweber;

/** Global safety boundary for any Seuchenweber multi-target cast or pulse. */
final class RiftPulseBudget {
  static final int GLOBAL_MAX_TARGETS = 16;

  private RiftPulseBudget() { }

  static int boundedTargetLimit(int configuredLimit) {
    return configuredLimit <= 0 ? 0 : Math.min(configuredLimit, GLOBAL_MAX_TARGETS);
  }
}
