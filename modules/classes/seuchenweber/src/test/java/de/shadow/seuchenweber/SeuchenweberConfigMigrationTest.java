package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SeuchenweberConfigMigrationTest {
  @TempDir Path temporaryDirectory;

  @Test void backsUpAndMigratesKnownLegacyUnitsWhilePreservingUnknownKeys() throws Exception {
    Path config = temporaryDirectory.resolve("seuchenweber.json");
    String legacy = """
        {
          "schemaVersion": 1,
          "cooldownMilliseconds": 12000,
          "damagePercent": 90,
          "thirdPartyUnknownSetting": "keep-me"
        }
        """;
    Files.writeString(config, legacy, StandardCharsets.UTF_8);

    SeuchenweberConfigMigration.Result result = SeuchenweberConfigMigration.migrate(config);

    assertTrue(result.migrated());
    assertTrue(Files.exists(result.backup()));
    assertEquals(legacy, Files.readString(result.backup(), StandardCharsets.UTF_8));
    String migrated = Files.readString(config, StandardCharsets.UTF_8);
    assertTrue(migrated.contains("\"schemaVersion\": 4"));
    assertTrue(migrated.contains("\"cooldownSeconds\": 12.0"));
    assertTrue(migrated.contains("\"damageMultiplier\": 0.9"));
    assertTrue(migrated.contains("\"thirdPartyUnknownSetting\": \"keep-me\""));
    assertEquals(2, result.changes().size());

    SeuchenweberConfigMigration.Result second = SeuchenweberConfigMigration.migrate(config);
    assertFalse(second.migrated());
    assertEquals(result.backup(), second.backup());
  }

  @Test void migratesOnlyUnchangedSchemaTwoCombatDefaults() throws Exception {
    Path config = temporaryDirectory.resolve("schema-two.json");
    String schemaTwo = """
        {
          "schemaVersion": 2,
          "tickIntervalSeconds": 1.0,
          "maximumEchoTargetsPerTick": 1,
          "operatorCustomValue": 77
        }
        """;
    Files.writeString(config, schemaTwo, StandardCharsets.UTF_8);
    SeuchenweberConfigMigration.Result result = SeuchenweberConfigMigration.migrate(config);
    assertTrue(result.migrated());
    assertEquals(schemaTwo, Files.readString(result.backup(), StandardCharsets.UTF_8));
    String migrated = Files.readString(config, StandardCharsets.UTF_8);
    assertTrue(migrated.contains("\"schemaVersion\": 4"));
    assertTrue(migrated.contains("\"tickIntervalSeconds\": 5.0"));
    assertTrue(migrated.contains("\"maximumEchoTargetsPerTick\": 2"));
    assertTrue(migrated.contains("\"operatorCustomValue\": 77"));
  }

  @Test void preservesCustomizedSchemaTwoCombatValues() throws Exception {
    Path config = temporaryDirectory.resolve("custom-schema-two.json");
    Files.writeString(config,
        "{\"schemaVersion\":2,\"tickIntervalSeconds\":2.5,\"maximumEchoTargetsPerTick\":4}",
        StandardCharsets.UTF_8);
    SeuchenweberConfigMigration.migrate(config);
    String migrated = Files.readString(config, StandardCharsets.UTF_8);
    assertTrue(migrated.contains("\"tickIntervalSeconds\":2.5"));
    assertTrue(migrated.contains("\"maximumEchoTargetsPerTick\":4"));
  }

  @Test void backsUpSchemaThreeAndAddsOnlyMissingPesthauchDefaults() throws Exception {
    Path config = temporaryDirectory.resolve("schema-three.json");
    String schemaThree = """
        {
          "schemaVersion": 3,
          "passives": {
            "necrotoxic_mastery": {
              "tickDamageMultiplier": 1.4,
              "operatorUnknown": "keep"
            }
          }
        }
        """;
    Files.writeString(config, schemaThree, StandardCharsets.UTF_8);

    SeuchenweberConfigMigration.Result result = SeuchenweberConfigMigration.migrate(config);

    assertTrue(result.migrated());
    assertEquals(schemaThree, Files.readString(result.backup(), StandardCharsets.UTF_8));
    String migrated = Files.readString(config, StandardCharsets.UTF_8);
    assertTrue(migrated.contains("\"schemaVersion\": 4"));
    assertTrue(migrated.contains("\"auraRadiusBlocks\": 8.0"));
    assertTrue(migrated.contains("\"auraPulseIntervalSeconds\": 2.0"));
    assertTrue(migrated.contains("\"operatorUnknown\": \"keep\""));
    assertFalse(SeuchenweberConfigMigration.migrate(config).migrated());
  }
}
