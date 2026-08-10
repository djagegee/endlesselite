package de.shadow.seuchenweber;

import java.util.function.Consumer;

/** Range validation with operator-readable warnings and explicit temporary defaults. */
final class OperatorConfigValidation {
  private OperatorConfigValidation() { }

  static double decimal(String path, double value, double minimum, double maximum, double defaultValue,
      Consumer<String> warningSink) {
    if (Double.isFinite(value) && value >= minimum && value <= maximum) return value;
    warningSink.accept("Invalid value for " + path + ": " + readable(value) + ". Allowed range: "
        + OperatorConfigNumbers.formatForStorage(minimum) + " to "
        + OperatorConfigNumbers.formatForStorage(maximum) + ". Temporarily using the default value "
        + OperatorConfigNumbers.formatForStorage(defaultValue) + ".");
    return defaultValue;
  }

  static int integer(String path, int value, int minimum, int maximum, int defaultValue,
      Consumer<String> warningSink) {
    if (value >= minimum && value <= maximum) return value;
    warningSink.accept("Invalid value for " + path + ": " + value + ". Allowed range: " + minimum
        + " to " + maximum + ". Temporarily using the default value " + defaultValue + ".");
    return defaultValue;
  }

  private static String readable(double value) {
    return Double.isFinite(value) ? OperatorConfigNumbers.formatForStorage(value) : Double.toString(value);
  }
}
