package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class OperatorConfigNumbersTest {
  @Test void readsDecimalPointIndependentlyOfSystemLocale() {
    Locale previous = Locale.getDefault();
    try {
      Locale.setDefault(Locale.GERMANY);
      assertEquals(0.9, OperatorConfigNumbers.requiredDouble("{\"damageMultiplier\":0.9}", "damageMultiplier"));
    } finally {
      Locale.setDefault(previous);
    }
  }

  @Test void rejectsDecimalCommaWithUnderstandableMessage() {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> OperatorConfigNumbers.requiredDouble("{\"damageMultiplier\":0,9}", "damageMultiplier"));
    assertTrue(error.getMessage().contains("decimal point"));
  }

  @Test void formatsGeneratedDecimalsWithAtMostTwoUsefulPlaces() {
    assertEquals("0.9", OperatorConfigNumbers.formatForStorage(0.8999999761581421));
    assertEquals("0.88", OperatorConfigNumbers.formatForStorage(0.8750000001));
    assertEquals("1.0", OperatorConfigNumbers.formatForStorage(1.0));
  }
}
