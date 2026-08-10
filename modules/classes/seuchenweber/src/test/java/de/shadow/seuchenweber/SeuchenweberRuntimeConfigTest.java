package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SeuchenweberRuntimeConfigTest {
  @TempDir Path dataDirectory;
  @Test void loadsNekrotoxinRuntimeValuesFromTheBundledCentralConfig() {
    SeuchenweberRuntimeConfig config = SeuchenweberRuntimeConfig.loadBundled();
    assertEquals(10_000L, config.toxinDurationMs());
    assertEquals(5_000L, config.tickIntervalMs());
    assertEquals(5, config.maxStacksPerOwner());
    assertEquals(6.0f, config.baseTickDamage());
    assertEquals(1.25, config.masteryTickDamageMultiplier());
    assertEquals(8.0, config.pesthauchRadiusBlocks());
    assertEquals(2_000L, config.pesthauchPulseIntervalMs());
    assertEquals(5.0, config.riftRadiusBlocks());
    assertEquals(5_000L, config.riftDurationMs());
    assertEquals(1_000L, config.riftPulseIntervalMs());
    assertEquals(8, config.riftMaxTargetsPerPulse());
    assertEquals(1, config.riftPulseStacksApplied());
    assertEquals(2, config.astralEchoMaxTargetsPerTick());
    assertEquals(5.0, config.astralEchoRadiusBlocks());
    assertEquals(1, config.astralEchoStacksApplied());
    assertEquals(3, config.soulDiagnosisStackThreshold());
    assertEquals(8.0f, config.relicManaRefundAmount());
    assertEquals(1_200L, config.relicCooldownRefundMs());
  }

  @Test void createsAndLoadsOperatorConfigFromPluginDataDirectory() throws Exception {
    SeuchenweberRuntimeConfig config = SeuchenweberRuntimeConfig.load(dataDirectory);
    Path operatorConfig = dataDirectory.resolve("seuchenweber.json");
    assertTrue(Files.exists(operatorConfig));
    assertTrue(Files.readString(operatorConfig).contains("\"schemaVersion\": 4"));
    assertEquals(10_000L, config.toxinDurationMs());
    assertEquals(8, config.riftMaxTargetsPerPulse());
  }
}
