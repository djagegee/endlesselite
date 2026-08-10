package de.shadow.portalspawn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class PortalSpawnSnapshotTest {
  private static final Path SNAPSHOT = Path.of("src", "main", "snapshot");

  @Test
  void preservesTheTwoDistinctPrefabSourcesByteExactly() throws Exception {
    assertEquals("5c36ec3c99da499931b94a415343bc20927cfdc8352f36f49fdeeebe4c092038",
        sha256(SNAPSHOT.resolve(Path.of("Server", "Prefabs", "Portal Spawn.prefab.json"))));
    assertEquals("32573eb519f6f8e249e0780cadf76530396530e1fb5da9d5e29ffd145b7f493d",
        sha256(SNAPSHOT.resolve(Path.of("Server", "Prefabs", "spawn.prefab.json"))));
  }

  @Test
  void snapshotJsonIsValidAndContainsBlocks() throws Exception {
    for (String file : new String[] {"Portal Spawn.prefab.json", "spawn.prefab.json"}) {
      try (Reader reader = Files.newBufferedReader(SNAPSHOT.resolve(Path.of("Server", "Prefabs", file)), StandardCharsets.UTF_8)) {
        JsonObject prefab = JsonParser.parseReader(reader).getAsJsonObject();
        assertTrue(prefab.getAsJsonArray("blocks").size() > 0, file);
      }
    }
  }

  @Test
  void sourceManifestDefectsKeepDeploymentFailClosed() throws Exception {
    JsonObject manifest;
    try (Reader reader = Files.newBufferedReader(SNAPSHOT.resolve("manifest.json"), StandardCharsets.UTF_8)) {
      manifest = JsonParser.parseReader(reader).getAsJsonObject();
    }
    assertEquals("Portal Spawn", manifest.get("Group").getAsString());
    assertEquals("Endless Elite", manifest.get("Name").getAsString());
    assertFalse(manifest.has("Version"));
    assertFalse(manifest.has("Main"));
    assertFalse(manifest.get("IncludesAssetPack").getAsBoolean());

    JsonObject gate;
    try (Reader reader = Files.newBufferedReader(SNAPSHOT.resolve("release-gate.json"), StandardCharsets.UTF_8)) {
      gate = JsonParser.parseReader(reader).getAsJsonObject();
    }
    assertFalse(gate.get("deployment_allowed").getAsBoolean());
  }

  @Test
  void publicSnapshotMetadataDoesNotExposeALocalWindowsPath() throws Exception {
    JsonObject inventory = JsonParser.parseString(
        Files.readString(SNAPSHOT.resolve("SOURCE_SNAPSHOT.json"), StandardCharsets.UTF_8)).getAsJsonObject();
    String source = inventory.get("source").getAsString();
    assertFalse(source.matches("(?i)^[a-z]:[\\\\/].*"));
    assertFalse(source.matches("(?i).*[/\\\\]Users[/\\\\].*"));
  }

  private static String sha256(Path path) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
  }
}
