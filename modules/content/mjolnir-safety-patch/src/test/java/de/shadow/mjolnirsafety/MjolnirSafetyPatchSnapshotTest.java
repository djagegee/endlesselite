package de.shadow.mjolnirsafety;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class MjolnirSafetyPatchSnapshotTest {
  private static final Path SNAPSHOT = Path.of("src", "main", "snapshot");
  private static final Path PATCH = SNAPSHOT.resolve(Path.of("Server", "Item", "Items", "Weapon_Mjolnir_Starky.json.patch"));

  @Test
  void preservesTheOriginalPatchByteExactly() throws Exception {
    assertEquals("9860162f7fb77455f5cb29dc67ff27bb175875400dc728678f1505874309f5c1",
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(PATCH))));
  }

  @Test
  void patchKeepsChargingSpinButDoesNotReintroduceTheHeldPassivePulse() throws Exception {
    String text = Files.readString(PATCH, StandardCharsets.UTF_8);
    JsonObject patch = JsonParser.parseString(text).getAsJsonObject();
    assertEquals(10000, patch.get("$Priority").getAsInt());
    assertTrue(text.contains("Mjolnir_Charging_Spin_Loop"));
    assertFalse(text.contains("Mjolnir_Held_Passive_Pulse"));
  }

  @Test
  void manifestVersionIsPreservedAndDeploymentRemainsFailClosed() throws Exception {
    JsonObject manifest = JsonParser.parseString(Files.readString(SNAPSHOT.resolve("manifest.json"), StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals("Shadow", manifest.get("Group").getAsString());
    assertEquals("MjolnirSafetyPatch", manifest.get("Name").getAsString());
    assertEquals("1.0.0", manifest.get("Version").getAsString());
    JsonObject gate = JsonParser.parseString(Files.readString(SNAPSHOT.resolve("release-gate.json"), StandardCharsets.UTF_8)).getAsJsonObject();
    assertFalse(gate.get("deployment_allowed").getAsBoolean());
  }
}
