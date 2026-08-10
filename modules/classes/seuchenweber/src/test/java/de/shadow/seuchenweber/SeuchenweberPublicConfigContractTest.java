package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class SeuchenweberPublicConfigContractTest {
  @Test void publicConfigUsesExplicitSecondsAndReadableUnitNames() throws Exception {
    String config = resource("config/seuchenweber.json");
    String normalized = config.toLowerCase(Locale.ROOT);
    assertFalse(normalized.contains("_ms"), "Public config must not expose *_ms keys");
    assertFalse(normalized.contains("milliseconds"), "Public config must not expose milliseconds");
    for (String key : new String[] {
        "durationSeconds", "tickIntervalSeconds", "cooldownSeconds", "teleportRangeBlocks",
        "riftRadiusBlocks", "pulseIntervalSeconds", "maximumStacksPerOwner", "maximumTargetsPerPulse",
        "auraRadiusBlocks", "auraPulseIntervalSeconds"
    }) assertTrue(config.contains("\"" + key + "\""), key);
    String docs = resource("docs/CONFIGURATION.md");
    assertTrue(docs.contains("passives.necrotoxic_mastery.auraRadiusBlocks"));
    assertTrue(docs.contains("passives.necrotoxic_mastery.auraPulseIntervalSeconds"));
  }

  private static String resource(String path) throws Exception {
    try (InputStream input = SeuchenweberPublicConfigContractTest.class.getClassLoader().getResourceAsStream(path)) {
      assertNotNull(input, path);
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
