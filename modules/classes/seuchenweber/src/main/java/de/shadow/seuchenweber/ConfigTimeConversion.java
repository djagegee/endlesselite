package de.shadow.seuchenweber;

/** Converts operator-facing seconds into internal runtime units. */
final class ConfigTimeConversion {
  private ConfigTimeConversion() { }

  static long secondsToMilliseconds(double seconds) {
    if (!Double.isFinite(seconds) || seconds < 0.0 || seconds > Long.MAX_VALUE / 1000.0) {
      throw new IllegalArgumentException("seconds must be finite, non-negative and representable");
    }
    return Math.round(seconds * 1000.0);
  }
}
