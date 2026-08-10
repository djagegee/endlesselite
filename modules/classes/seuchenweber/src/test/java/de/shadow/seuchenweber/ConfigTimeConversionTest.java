package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConfigTimeConversionTest {
  @Test void convertsReadableSecondsToInternalMilliseconds() {
    assertEquals(10_000L, ConfigTimeConversion.secondsToMilliseconds(10.0));
    assertEquals(1_200L, ConfigTimeConversion.secondsToMilliseconds(1.2));
    assertEquals(750L, ConfigTimeConversion.secondsToMilliseconds(0.75));
    assertEquals(50L, ConfigTimeConversion.secondsToMilliseconds(0.05));
  }

  @Test void rejectsInvalidPublicTimeValues() {
    assertThrows(IllegalArgumentException.class, () -> ConfigTimeConversion.secondsToMilliseconds(-0.1));
    assertThrows(IllegalArgumentException.class, () -> ConfigTimeConversion.secondsToMilliseconds(Double.NaN));
    assertThrows(IllegalArgumentException.class, () -> ConfigTimeConversion.secondsToMilliseconds(Double.POSITIVE_INFINITY));
  }
}
