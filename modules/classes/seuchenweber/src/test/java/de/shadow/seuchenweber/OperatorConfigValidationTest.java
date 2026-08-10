package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperatorConfigValidationTest {
  @Test void usesSafeDefaultAndProducesUnderstandableRangeWarning() {
    List<String> warnings = new ArrayList<>();
    double value = OperatorConfigValidation.decimal(
        "classes.seuchenweber.pvp.damageMultiplier", 7.5, 0.0, 2.0, 0.7, warnings::add);
    assertEquals(0.7, value);
    assertEquals(1, warnings.size());
    String warning = warnings.getFirst();
    assertTrue(warning.contains("classes.seuchenweber.pvp.damageMultiplier"));
    assertTrue(warning.contains("7.5"));
    assertTrue(warning.contains("0.0 bis 2.0"));
    assertTrue(warning.contains("Standardwert 0.7"));
  }

  @Test void acceptsInclusiveBoundsAndRejectsNonFiniteValues() {
    List<String> warnings = new ArrayList<>();
    assertEquals(0.0, OperatorConfigValidation.decimal("chance", 0.0, 0.0, 1.0, 0.5, warnings::add));
    assertEquals(1.0, OperatorConfigValidation.decimal("chance", 1.0, 0.0, 1.0, 0.5, warnings::add));
    assertEquals(0.5, OperatorConfigValidation.decimal("chance", Double.NaN, 0.0, 1.0, 0.5, warnings::add));
    assertEquals(1, warnings.size());
  }

  @Test void validatesWholeNumberCounts() {
    List<String> warnings = new ArrayList<>();
    assertEquals(8, OperatorConfigValidation.integer("maximumTargets", 8, 1, 16, 8, warnings::add));
    assertEquals(8, OperatorConfigValidation.integer("maximumTargets", 17, 1, 16, 8, warnings::add));
    assertEquals(1, warnings.size());
  }
}
