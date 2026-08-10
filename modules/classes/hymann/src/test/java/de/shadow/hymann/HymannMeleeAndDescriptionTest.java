package de.shadow.hymann;

import com.airijko.endlessleveling.augments.AugmentRoleWeightRules;
import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import com.ziggfreed.mmoskilltree.skilltree.SkillReward;
import com.ziggfreed.mmoskilltree.skilltree.SkillRewardType;
import com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HymannMeleeAndDescriptionTest {
    @Test
    void everyHymannStageUsesTheEndlessLevelingVanguardMeleeRole() throws Exception {
        Method buildDefinitions = HymannRegistrar.class.getDeclaredMethod("buildDefinitions");
        buildDefinitions.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<CharacterClassDefinition> definitions =
                (List<CharacterClassDefinition>) buildDefinitions.invoke(null);

        assertEquals(5, definitions.size());
        for (CharacterClassDefinition definition : definitions) {
            assertEquals("melee", definition.getRangeType());
            assertEquals(List.of("Vanguard"), definition.getRoles());
            assertFalse(definition.getRoles().contains("Marksman"));
            Set<String> weapons = definition.getWeaponMultipliers().keySet().stream()
                    .map(key -> key.toUpperCase(Locale.ROOT))
                    .collect(java.util.stream.Collectors.toSet());
            assertTrue(weapons.contains("MACE"));
            assertTrue(weapons.contains("SHIELD"));
            assertFalse(weapons.contains("BOW"));
            assertFalse(weapons.contains("CROSSBOW"));
        }
    }

    @Test
    void everyHymannAbilityHasAnExplicitMmoSkillUnlockLevel() throws Exception {
        Method buildTree = HymannMmoBridge.class.getDeclaredMethod("buildTree");
        buildTree.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<SkillTreeNode> nodes = (List<SkillTreeNode>) buildTree.invoke(null);
        java.util.Map<Integer, String> expectedUnlocks = java.util.Map.of(
                2, "hymann_thunder_step",
                4, "hymann_passive_stormbreaker_aura",
                11, "hymann_passive_tempest_rebirth",
                32, "hymann_storm_fury",
                37, "hymann_passive_stormbreaker_pulse_1",
                44, "hymann_thunder_aegis",
                50, "hymann_passive_stormbreaker_pulse_2",
                97, "hymann_passive_stormbreaker_pulse_3",
                99, "hymann_passive_thunder_cadence"
        );

        for (java.util.Map.Entry<Integer, String> expected : expectedUnlocks.entrySet()) {
            SkillTreeNode node = nodes.stream()
                    .filter(candidate -> candidate.getLevelRequired() == expected.getKey())
                    .findFirst()
                    .orElseThrow();
            assertEquals(1, node.getChoices().size());
            SkillReward reward = node.getChoices().getFirst();
            assertEquals(expected.getValue(), reward.getId());
            assertEquals(SkillRewardType.ABILITY_UNLOCK, reward.getType());
            assertFalse(reward.getDescription().isBlank());
            assertTrue(reward.hasAbilityIdFilter());
        }
    }

    @Test
    void lightningStepProvidesDescriptionParametersForTheMmoUnlockCard() throws Exception {
        var constructor = HymannThunderStepAbility.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AbilityEffect effect = (AbilityEffect) constructor.newInstance();

        ParamSpec spec = effect.getParamSpec();
        Set<String> keys = spec.entries().stream()
                .map(ParamSpec.ParamEntry::key)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(Set.of("range", "radius", "damage", "stunDurationMs"), keys);
        assertTrue(spec.entries().stream().allMatch(ParamSpec.ParamEntry::required));
    }

    @Test
    void endlessLevelingRejectsRangedAugmentsForEveryHymannStage() throws Exception {
        Method buildDefinitions = HymannRegistrar.class.getDeclaredMethod("buildDefinitions");
        buildDefinitions.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<CharacterClassDefinition> definitions =
                (List<CharacterClassDefinition>) buildDefinitions.invoke(null);

        for (CharacterClassDefinition definition : definitions) {
            assertFalse(AugmentRoleWeightRules.isAugmentAllowedForClass(definition, "magic_bow"));
            assertTrue(AugmentRoleWeightRules.isAugmentAllowedForClass(definition, "executioner"));
            assertTrue(AugmentRoleWeightRules.isAugmentAllowedForClass(definition, "bulwark_edge"));
        }
    }

    @Test
    void itemTooltipKeysExistInTheHytaleServerLanguageNamespace() throws IOException {
        Set<String> iconIds = Set.of(
                "XP", "Damage", "Lifesteal", "Combo", "Cooldown", "Stamina",
                "Fall_Guard", "Block", "Stamina_Regen", "Mana_Regen", "Sorcery",
                "Crit", "Thunder_Step", "Storm_Fury", "Thunder_Aegis", "Stormbreaker"
        );

        for (String language : List.of("en-US", "de-DE")) {
            String resource = "Server/Languages/" + language + "/server.lang";
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
                assertTrue(stream != null, "Missing " + resource);
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                for (String iconId : iconIds) {
                    String prefix = "items.Hymann_Skill_" + iconId;
                    assertTrue(text.contains(prefix + ".name = "), "Missing name: " + prefix);
                    assertTrue(text.contains(prefix + ".description = "), "Missing description: " + prefix);
                }
            }
        }
    }
}
