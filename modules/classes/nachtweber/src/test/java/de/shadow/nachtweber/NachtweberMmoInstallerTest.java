package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class NachtweberMmoInstallerTest {
  @TempDir Path temp;

  @Test void contractDeclaresOneMasteryAndExactlySevenOriginalUnlocks() {
    assertEquals("NACHTWEBER_MASTERY", NachtweberMmoContract.SKILL_ID);
    assertEquals(Set.of("nachtweber_black_thread", "nachtweber_shadow_swing", "nachtweber_hunting_cocoon"),
        NachtweberMmoContract.activeAbilityIds());
    assertEquals(Set.of("nachtweber_passive_danger_sense", "nachtweber_passive_wall_hunter",
        "nachtweber_passive_toxic_glands", "nachtweber_passive_hunting_instinct"),
        NachtweberMmoContract.passiveAbilityIds());
    assertEquals(7, NachtweberMmoContract.unlocks().size());
    assertEquals(Set.of(1, 2, 25, 40, 55, 70, 90), NachtweberMmoContract.unlockLevels());
  }

  @Test void abilityInstallPreservesForeignEntriesCreatesOneBackupAndIsIdempotent() throws Exception {
    Path abilities = temp.resolve("abilities.json");
    String original = "{\"schemaVersion\":24,\"enabled\":true,\"abilities\":{\"foreign_spell\":{\"effect\":\"DAMAGE\"}}}";
    Files.writeString(abilities, original, StandardCharsets.UTF_8);

    assertEquals(7, NachtweberMmoInstaller.installAbilities(abilities));
    String first = Files.readString(abilities, StandardCharsets.UTF_8);
    JsonObject root = JsonParser.parseString(first).getAsJsonObject();
    JsonObject installed = root.getAsJsonObject("abilities");
    assertTrue(installed.has("foreign_spell"));
    for (String id : NachtweberMmoContract.allAbilityIds()) assertTrue(installed.has(id), id);
    JsonObject blackThread = installed.getAsJsonObject("nachtweber_black_thread");
    assertEquals("NACHTWEBER_BLACK_THREAD", blackThread.get("effect").getAsString());
    assertEquals(3_000, blackThread.get("cooldownMs").getAsInt());
    JsonObject blackThreadParams = blackThread.getAsJsonObject("params");
    assertEquals(12.0, blackThreadParams.get("range").getAsDouble());
    assertEquals(2, blackThreadParams.get("stacks").getAsInt());
    assertEquals(8_000, blackThreadParams.get("stateDurationMs").getAsInt());
    assertEquals(1_200, blackThreadParams.get("boundControlDurationMs").getAsInt());
    assertFalse(blackThreadParams.has("runtimeGate"));
    JsonObject shadow=installed.getAsJsonObject("nachtweber_shadow_swing");
    assertEquals("NACHTWEBER_SHADOW_SWING",shadow.get("effect").getAsString());
    assertEquals(5_000,shadow.get("cooldownMs").getAsInt());
    JsonObject shadowParams=shadow.getAsJsonObject("params");
    assertEquals(18.0,shadowParams.get("range").getAsDouble());
    assertEquals(2.0,shadowParams.get("minimumAnchorDistance").getAsDouble());
    assertEquals(12.0,shadowParams.get("horizontalImpulse").getAsDouble());
    assertEquals(4.0,shadowParams.get("verticalImpulse").getAsDouble());
    assertFalse(shadowParams.has("runtimeGate"));
    JsonObject cocoon = installed.getAsJsonObject("nachtweber_hunting_cocoon");
    assertEquals("NACHTWEBER_HUNTING_COCOON", cocoon.get("effect").getAsString());
    assertEquals(7_000, cocoon.get("cooldownMs").getAsInt());
    JsonObject cocoonParams = cocoon.getAsJsonObject("params");
    assertEquals(8.0, cocoonParams.get("range").getAsDouble());
    assertEquals(3, cocoonParams.get("requiredEntanglementStacks").getAsInt());
    assertEquals(2, cocoonParams.get("venomStacks").getAsInt());
    assertEquals(8_000, cocoonParams.get("venomDurationMs").getAsInt());
    assertFalse(cocoonParams.has("runtimeGate"));
    JsonObject danger=installed.getAsJsonObject("nachtweber_passive_danger_sense").getAsJsonObject("params");
    assertEquals(16.0,danger.get("radiusBlocks").getAsDouble());
    assertEquals(2_000,danger.get("warningCooldownMs").getAsInt());
    assertEquals(64,danger.get("maximumCandidatesPerScan").getAsInt());
    JsonObject wall=installed.getAsJsonObject("nachtweber_passive_wall_hunter").getAsJsonObject("params");
    assertEquals(4.0,wall.get("climbSpeed").getAsDouble());
    assertTrue(wall.get("requiresHorizontalWallContact").getAsBoolean());
    assertTrue(wall.get("requiresUpwardInput").getAsBoolean());
    JsonObject toxic=installed.getAsJsonObject("nachtweber_passive_toxic_glands").getAsJsonObject("params");
    assertEquals(1,toxic.get("baseVenomStacks").getAsInt());
    assertEquals(6_000,toxic.get("venomDurationMs").getAsInt());
    assertEquals(1_000,toxic.get("internalCooldownMs").getAsInt());
    assertEquals("nachtweber_verified_direct_hit_unregistered",
        toxic.getAsJsonObject("passiveTrigger").get("event").getAsString());
    JsonObject hunting=installed.getAsJsonObject("nachtweber_passive_hunting_instinct").getAsJsonObject("params");
    assertTrue(hunting.get("requiresOwnEntanglement").getAsBoolean());
    assertEquals(1,hunting.get("bonusVenomStacks").getAsInt());

    Path backup = abilities.resolveSibling("abilities.json.nachtweber-before.bak");
    assertEquals(JsonParser.parseString(original), JsonParser.parseString(Files.readString(backup)));
    assertEquals(7, NachtweberMmoInstaller.installAbilities(abilities));
    assertEquals(JsonParser.parseString(first), JsonParser.parseString(Files.readString(abilities)));
    assertEquals(JsonParser.parseString(original), JsonParser.parseString(Files.readString(backup)));
  }

  @Test void localizationInstallPreservesForeignKeysAndAddsGermanAndEnglishContracts() throws Exception {
    Path directory = temp.resolve("mmoskilltree");
    Files.createDirectories(directory);
    Path german = directory.resolve("messages-de-DE.json");
    Path english = directory.resolve("messages-en-US.json");
    Files.writeString(german, "{\"foreign.key\":\"Bleibt\"}", StandardCharsets.UTF_8);
    Files.writeString(english, "{\"foreign.key\":\"Keep\"}", StandardCharsets.UTF_8);

    assertEquals(16, NachtweberMmoInstaller.installLocalization(directory));
    JsonObject de = JsonParser.parseString(Files.readString(german)).getAsJsonObject();
    JsonObject en = JsonParser.parseString(Files.readString(english)).getAsJsonObject();
    assertEquals("Bleibt", de.get("foreign.key").getAsString());
    assertEquals("Keep", en.get("foreign.key").getAsString());
    assertEquals("Nachtweber", de.get("skill.nachtweber_mastery").getAsString());
    assertEquals("Nightweaver", en.get("skill.nachtweber_mastery").getAsString());
    for (String id : NachtweberMmoContract.allAbilityIds()) {
      assertTrue(de.has("ability." + id + ".name"), id);
      assertTrue(de.has("ability." + id + ".flavor"), id);
      assertTrue(en.has("ability." + id + ".name"), id);
      assertTrue(en.has("ability." + id + ".flavor"), id);
    }
    String firstGerman = Files.readString(german);
    String firstEnglish = Files.readString(english);
    assertEquals(16, NachtweberMmoInstaller.installLocalization(directory));
    assertEquals(JsonParser.parseString(firstGerman), JsonParser.parseString(Files.readString(german)));
    assertEquals(JsonParser.parseString(firstEnglish), JsonParser.parseString(Files.readString(english)));
    assertTrue(Files.exists(directory.resolve("messages-de-DE.json.nachtweber-before.bak")));
    assertTrue(Files.exists(directory.resolve("messages-en-US.json.nachtweber-before.bak")));
  }
}
