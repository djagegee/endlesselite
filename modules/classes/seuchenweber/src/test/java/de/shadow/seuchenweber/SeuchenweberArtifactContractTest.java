package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class SeuchenweberArtifactContractTest {
  private static final List<String> ACTIVE = List.of("seal_of_decay", "astral_rift", "chronoblight");
  private static final List<String> PASSIVE = List.of("necrotoxic_mastery", "astral_echo", "soul_diagnosis", "relic_attunement");
  private static final List<String> ICONS = List.of(
      "Seuchenweber_Class_Icon", "Seuchenweber_Skill_Seal_Of_Decay", "Seuchenweber_Skill_Astral_Rift",
      "Seuchenweber_Skill_Chronoblight", "Seuchenweber_Skill_Necrotoxic_Mastery", "Seuchenweber_Skill_Astral_Echo",
      "Seuchenweber_Skill_Soul_Diagnosis", "Seuchenweber_Skill_Relic_Attunement");

  @Test void centralContractContainsExactlyThreeActivesFourPassivesAndDotDominance() throws Exception {
    String config = resource("config/seuchenweber.json");
    assertSectionKeys(config, "abilities", ACTIVE);
    assertSectionKeys(config, "passives", PASSIVE);
    assertTrue(config.contains("\"minimumDotShare\": 0.7"));
    assertTrue(config.contains("\"representativeDirectDamage\": 30.0"));
    assertTrue(config.contains("\"representativeDotDamage\": 120.0"));
    assertTrue(config.contains("\"registrationGate\""));
    for (String requiredKey : List.of("bossDamageMultiplier", "eliteDamageMultiplier", "pvpPlayerDamageMultiplier",
        "pvpSummonDamageMultiplier", "overdueTickPolicy", "rejectDotProcRecursion", "rejectReflectionRecursion")) {
      assertTrue(config.contains("\"" + requiredKey + "\""), requiredKey);
    }
  }

  @Test void eachSkillHasLocalizedItemAndAnOriginal64PixelIcon() throws Exception {
    String de = resource("Server/Languages/de-DE/server.lang");
    String en = resource("Server/Languages/en-US/server.lang");
    for (String id : ACTIVE.stream().map(SeuchenweberArtifactContractTest::itemSuffix).toList()) assertSkillResource(id, de, en);
    for (String id : PASSIVE.stream().map(SeuchenweberArtifactContractTest::itemSuffix).toList()) assertSkillResource(id, de, en);
    assertEquals(8, ICONS.size());
    for (String icon : ICONS) {
      try (InputStream input = getClass().getClassLoader().getResourceAsStream("Common/Icons/ItemsGenerated/" + icon + ".png")) {
        BufferedImage image = ImageIO.read(input);
        assertNotNull(image, icon);
        assertEquals(64, image.getWidth(), icon);
        assertEquals(64, image.getHeight(), icon);
      }
    }
  }

  private void assertSkillResource(String suffix, String de, String en) throws Exception {
    String item = "Seuchenweber_Skill_" + suffix;
    assertTrue(resource("Server/Item/Items/" + item + ".json").contains("Icons/ItemsGenerated/" + item + ".png"));
    for (String lang : List.of(de, en)) {
      assertTrue(lang.contains("items." + item + ".name="), item + " name");
      assertTrue(lang.contains("items." + item + ".description="), item + " description");
    }
  }

  private static String itemSuffix(String abilityId) {
    return String.join("_", java.util.Arrays.stream(abilityId.split("_")).map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1)).toList());
  }

  private static void assertSectionKeys(String json, String section, List<String> expected) {
    int start = json.indexOf("\"" + section + "\"");
    int end = json.indexOf("\n  }", start);
    String body = json.substring(start, end);
    for (String key : expected) assertTrue(body.contains("\"" + key + "\""), key);
    long entries = body.lines().skip(1).filter(line -> line.stripLeading().startsWith("\"") && line.contains("\": {")).count();
    assertEquals(expected.size(), entries, section);
  }

  private static String resource(String path) throws Exception {
    try (InputStream input = SeuchenweberArtifactContractTest.class.getClassLoader().getResourceAsStream(path)) {
      assertNotNull(input, path);
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
