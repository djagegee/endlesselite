package de.shadow.seuchenweber;

import java.util.function.Consumer;

/** Range validation with operator-readable warnings and explicit temporary defaults. */
final class OperatorConfigValidation {
  private OperatorConfigValidation() { }

  static double decimal(String path, double value, double minimum, double maximum, double defaultValue,
      Consumer<String> warningSink) {
    if (Double.isFinite(value) && value >= minimum && value <= maximum) return value;
    warningSink.accept("Ungültiger Wert für " + path + ": " + readable(value) + ". Erlaubter Bereich: "
        + OperatorConfigNumbers.formatForStorage(minimum) + " bis "
        + OperatorConfigNumbers.formatForStorage(maximum) + ". Es wird vorübergehend der Standardwert "
        + OperatorConfigNumbers.formatForStorage(defaultValue) + " verwendet.");
    return defaultValue;
  }

  static int integer(String path, int value, int minimum, int maximum, int defaultValue,
      Consumer<String> warningSink) {
    if (value >= minimum && value <= maximum) return value;
    warningSink.accept("Ungültiger Wert für " + path + ": " + value + ". Erlaubter Bereich: " + minimum
        + " bis " + maximum + ". Es wird vorübergehend der Standardwert " + defaultValue + " verwendet.");
    return defaultValue;
  }

  private static String readable(double value) {
    return Double.isFinite(value) ? OperatorConfigNumbers.formatForStorage(value) : Double.toString(value);
  }
}
