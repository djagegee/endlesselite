package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SeuchenweberClientLocalizationTest {
  @Test void packagesClientResolvedMmoSkillTreeNamespaceForBothLocales() throws Exception {
    for (String locale : new String[] {"de-DE", "en-US"}) {
      String resource = "/Server/Languages/" + locale + "/mmoskilltree.lang";
      try (InputStream stream = getClass().getResourceAsStream(resource)) {
        assertNotNull(stream, resource);
        String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(text.contains("skill.seuchenweber_mastery = Seuchenweber"), resource);
        assertTrue(text.contains("skill.seuchenweber_mastery.desc = "), resource);
        for (var reward : SeuchenweberMmoBridge.buildTree().stream()
            .flatMap(node -> node.getChoices().stream()).toList()) {
          assertTrue(text.contains(reward.getNameKey() + " = "),
              resource + " missing " + reward.getNameKey());
          assertTrue(text.contains(reward.getDescriptionKey() + " = "),
              resource + " missing " + reward.getDescriptionKey());
        }
        for (var unlock : SeuchenweberMmoBridge.treeUnlocks()) {
          String flavorKey = "ability." + unlock.abilityId() + ".flavor";
          assertTrue(text.contains(flavorKey + " = "), resource + " missing " + flavorKey);
        }
        if (locale.equals("de-DE")) {
          assertTrue(text.contains("ability.bind_page.passive_tag = PASSIV"));
          assertTrue(text.contains("ability.seuchenweber_passive_necrotoxic_mastery.flavor = Alle 2 Sekunden erhalten feindliche Ziele im Umkreis von 8 Blöcken einen Nekrotoxin-Stapel."));
          assertTrue(text.contains("seuchenweber.tree.seuchenweber_passive_necrotoxic_mastery.name = Pesthauch"));
        } else {
          assertTrue(text.contains("ability.bind_page.passive_tag = PASSIVE"));
          assertTrue(text.contains("ability.seuchenweber_passive_necrotoxic_mastery.flavor = Every 2 seconds, hostile targets within 8 blocks gain one Nekrotoxin stack."));
          assertTrue(text.contains("seuchenweber.tree.seuchenweber_passive_necrotoxic_mastery.name = Blight Breath"));
        }
      }
      String itemResource = "/Server/Languages/" + locale + "/server.lang";
      try (InputStream stream = getClass().getResourceAsStream(itemResource)) {
        assertNotNull(stream, itemResource);
        String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        String expectedName;
        String expectedTreeDescription;
        if (locale.equals("de-DE")) {
          expectedName = "Pesthauch";
          expectedTreeDescription = "Passiv · Ab Level 1: Alle 2 Sekunden erhalten feindliche Ziele im Umkreis von 8 Blöcken einen Nekrotoxin-Stapel.";
        } else {
          expectedName = "Blight Breath";
          expectedTreeDescription = "Passive · Level 1: Every 2 seconds, hostile targets within 8 blocks gain one Nekrotoxin stack.";
        }
        assertTrue(text.contains("items.Seuchenweber_Skill_Necrotoxic_Mastery.name=" + expectedName));
        assertTrue(text.contains("seuchenweber.tree.seuchenweber_passive_necrotoxic_mastery.name=" + expectedName));
        assertTrue(text.contains("seuchenweber.tree.seuchenweber_passive_necrotoxic_mastery.description=" + expectedTreeDescription));
      }
    }
  }
}
