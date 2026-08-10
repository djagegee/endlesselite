package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SeuchenweberAbilityConfigInstallerTest {
  @TempDir Path temporary;

  @Test void preservesUnrelatedAbilitiesAndInstallsSevenDefinitionsIdempotently() throws Exception {
    Path balance = temporary.resolve("seuchenweber.json");
    try (var source = getClass().getClassLoader().getResourceAsStream("config/seuchenweber.json")) {
      Files.write(balance, source.readAllBytes());
    }
    Path abilities = temporary.resolve("mmoskilltree/abilities.json");
    Files.createDirectories(abilities.getParent());
    Files.writeString(abilities, "{\"schemaVersion\":24,\"enabled\":true,\"abilities\":{\"hymann_existing\":{\"effect\":\"KEEP\"}}}", StandardCharsets.UTF_8);

    assertEquals(7, SeuchenweberAbilityConfigInstaller.install(balance, abilities));
    String first = Files.readString(abilities, StandardCharsets.UTF_8);
    assertEquals(7, SeuchenweberAbilityConfigInstaller.install(balance, abilities));
    assertEquals(first, Files.readString(abilities, StandardCharsets.UTF_8));

    JsonObject installed = JsonParser.parseString(first).getAsJsonObject().getAsJsonObject("abilities");
    assertTrue(installed.has("hymann_existing"));
    assertEquals(8, installed.size());
    assertEquals("SEUCHENWEBER_SEAL_OF_DECAY",
        installed.getAsJsonObject("seuchenweber_seal_of_decay").get("effect").getAsString());
    assertEquals("SEUCHENWEBER_MASTERY",
        installed.getAsJsonObject("seuchenweber_chronoblight").getAsJsonArray("xpSkills").get(0).getAsString());
    JsonObject pesthauch = installed.getAsJsonObject("seuchenweber_passive_necrotoxic_mastery");
    assertEquals("Blight Breath", pesthauch.get("displayName").getAsString());
    assertEquals("Every 2 seconds, hostile targets within 8 blocks receive one Necrotoxin stack.",
        pesthauch.get("description").getAsString());
    assertEquals("Seuchenweber_Skill_Necrotoxic_Mastery", pesthauch.get("icon").getAsString());
    for (String abilityId : SeuchenweberMmoBridge.treeUnlocks().stream()
        .map(SeuchenweberMmoBridge.TreeUnlock::abilityId).toList()) {
      assertTrue(installed.getAsJsonObject(abilityId).getAsJsonObject("params")
          .get("useCustomCardDescription").getAsBoolean(), abilityId);
    }
    assertTrue(Files.exists(abilities.resolveSibling("abilities.json.seuchenweber-before.bak")));
  }
}
