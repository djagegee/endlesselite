package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SeuchenweberPresentationInstallerTest {
  @TempDir Path temp;

  @Test void installsTrackAndAbilityLocalizationWithoutRemovingForeignKeys() throws Exception {
    Path de = temp.resolve("messages-de-DE.json");
    Files.writeString(de, "{\"hymann.keep\":\"bestehen\"}", StandardCharsets.UTF_8);

    int installed = SeuchenweberPresentationInstaller.install(temp);

    assertEquals(19, installed);
    JsonObject german = parse(de);
    JsonObject english = parse(temp.resolve("messages-en-US.json"));
    assertEquals("bestehen", german.get("hymann.keep").getAsString());
    assertEquals("Seuchenweber", german.get("skill.seuchenweber_mastery").getAsString());
    assertEquals("Gewährt Seuchenweber-Meisterschaftserfahrung.",
        german.get("shop.shop_xp_seuchenweber_mastery.desc").getAsString());
    assertEquals("Seuchenweber", english.get("skill.seuchenweber_mastery").getAsString());
    assertEquals("Pesthauch", german.get("ability.seuchenweber_passive_necrotoxic_mastery.name").getAsString());
    assertEquals("Blight Breath", english.get("ability.seuchenweber_passive_necrotoxic_mastery.name").getAsString());
    assertTrue(german.get("skill.seuchenweber_mastery.desc").getAsString().contains("Cosmic Ruin"));
    for (var unlock : SeuchenweberMmoBridge.treeUnlocks()) {
      String prefix = "ability." + unlock.abilityId();
      assertTrue(german.has(prefix + ".name"), prefix);
      assertTrue(german.has(prefix + ".flavor"), prefix);
      assertTrue(english.has(prefix + ".name"), prefix);
      assertTrue(english.has(prefix + ".flavor"), prefix);
    }
  }

  @Test void skillOverviewUsesARegisteredItemAssetAsIcon() {
    assertEquals("Seuchenweber_Skill_XP", SeuchenweberMmoBridge.SKILL_ICON);
    List<String> resources = List.of(
        "Server/Item/Items/Seuchenweber_Skill_XP.json",
        "Common/Icons/ItemsGenerated/Seuchenweber_Skill_XP.png");
    for (String resource : resources) {
      assertNotNull(getClass().getClassLoader().getResource(resource), resource);
    }
  }

  private static JsonObject parse(Path file) throws Exception {
    return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
  }
}
