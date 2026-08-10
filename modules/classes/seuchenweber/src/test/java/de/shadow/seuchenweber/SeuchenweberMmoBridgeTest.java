package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import com.ziggfreed.mmoskilltree.skilltree.SkillRewardType;
import org.junit.jupiter.api.Test;

class SeuchenweberMmoBridgeTest {
  @Test void exposesDedicatedSkillAndExactlyThreeActiveAndFourPassiveUnlocks() {
    assertEquals("SEUCHENWEBER_MASTERY", SeuchenweberMmoBridge.SKILL_ID);
    assertEquals(Set.of(
        "seuchenweber_seal_of_decay",
        "seuchenweber_astral_rift",
        "seuchenweber_chronoblight"), SeuchenweberMmoBridge.activeAbilityIds());
    assertEquals(Set.of(
        "seuchenweber_passive_necrotoxic_mastery",
        "seuchenweber_passive_astral_echo",
        "seuchenweber_passive_soul_diagnosis",
        "seuchenweber_passive_relic_attunement"), SeuchenweberMmoBridge.passiveAbilityIds());
    assertEquals(7, SeuchenweberMmoBridge.treeUnlocks().size());
  }

  @Test void everyUnlockUsesAUniquePositiveLevel() {
    var levels = SeuchenweberMmoBridge.treeUnlocks().stream().map(SeuchenweberMmoBridge.TreeUnlock::level).toList();
    assertEquals(levels.size(), Set.copyOf(levels).size());
    assertTrue(levels.stream().allMatch(level -> level > 0 && level <= 100));
    var pesthauch = SeuchenweberMmoBridge.treeUnlocks().stream()
        .filter(unlock -> unlock.abilityId().equals("seuchenweber_passive_necrotoxic_mastery"))
        .findFirst().orElseThrow();
    assertEquals(1, pesthauch.level());
  }

  @Test void providesAFullHymannStyleLevelOneHundredTreeWithClassFocusedBonuses() {
    var nodes = SeuchenweberMmoBridge.buildTree();
    assertEquals(38, nodes.size());
    assertEquals(1, nodes.getFirst().getLevelRequired());
    assertEquals(100, nodes.getLast().getLevelRequired());
    assertEquals(nodes.size(), Set.copyOf(nodes.stream().map(node -> node.getLevelRequired()).toList()).size());
    var rewards = nodes.stream().flatMap(node -> node.getChoices().stream()).toList();
    assertEquals(rewards.size(), Set.copyOf(rewards.stream().map(reward -> reward.getId()).toList()).size());
    assertTrue(rewards.stream().filter(reward -> reward.getType() == SkillRewardType.COOLDOWN_REDUCTION).count() >= 12);
    assertTrue(rewards.stream().filter(reward -> reward.getType() == SkillRewardType.MANA_REGEN).count() >= 12);
    assertTrue(rewards.stream().filter(reward -> reward.getType() == SkillRewardType.CRITICAL_CHANCE).count() >= 6);
    assertTrue(rewards.stream().filter(reward -> reward.getType() == SkillRewardType.STAT_DEFENSE).count() >= 6);
    assertTrue(rewards.stream().anyMatch(reward -> reward.getId().startsWith("seuchenweber_dot_damage_")));
    assertTrue(rewards.stream().anyMatch(reward -> reward.getId().startsWith("seuchenweber_direct_damage_")));
    assertTrue(rewards.stream().anyMatch(reward -> reward.getId().startsWith("seuchenweber_crit_damage_")));
    assertTrue(rewards.stream().anyMatch(reward -> reward.getId().startsWith("seuchenweber_stamina_regen_")));
  }

  @Test void everyTreeCardHasGermanAndEnglishLocalization() throws Exception {
    for (String locale : List.of("de-DE", "en-US")) {
      String path = "Server/Languages/" + locale + "/server.lang";
      String text;
      try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
        assertTrue(stream != null, path);
        text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      }
      for (var unlock : SeuchenweberMmoBridge.treeUnlocks()) {
        String prefix = "seuchenweber.tree." + unlock.abilityId();
        assertTrue(text.contains(prefix + ".name="), locale + " " + unlock.abilityId());
        assertTrue(text.contains(prefix + ".description="), locale + " " + unlock.abilityId());
      }
    }
  }
}
