package de.shadow.seuchenweber;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Locale-independent parsing and stable formatting for operator-facing decimal values. */
final class OperatorConfigNumbers {
  private OperatorConfigNumbers() { }

  static double requiredDouble(String json, String key) {
    int keyIndex = json.indexOf("\"" + key + "\"");
    int colon = keyIndex < 0 ? -1 : json.indexOf(':', keyIndex);
    if (colon < 0) throw new IllegalArgumentException("Missing numeric config path: " + key);
    int start = colon + 1;
    while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
    int end = start;
    while (end < json.length() && isNumberCharacter(json.charAt(end))) end++;
    if (end < json.length() && json.charAt(end) == ',' && end + 1 < json.length()
        && Character.isDigit(json.charAt(end + 1))) {
      throw new IllegalArgumentException("Invalid value for " + key + ": use a decimal point instead of a decimal comma");
    }
    String token = json.substring(start, end);
    try {
      double parsed = Double.parseDouble(token);
      if (Double.isFinite(parsed)) return parsed;
    } catch (NumberFormatException ignored) { }
    throw new IllegalArgumentException("Invalid finite decimal value for " + key + ": " + token);
  }

  static String formatForStorage(double value) {
    if (!Double.isFinite(value)) throw new IllegalArgumentException("value must be finite");
    String formatted = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    return formatted.contains(".") ? formatted : formatted + ".0";
  }

  private static boolean isNumberCharacter(char character) {
    return Character.isDigit(character) || character == '-' || character == '+' || character == '.'
        || character == 'e' || character == 'E';
  }
}
