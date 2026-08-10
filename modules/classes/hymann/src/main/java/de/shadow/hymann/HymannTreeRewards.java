/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
 */
package de.shadow.hymann;

import com.ziggfreed.mmoskilltree.data.SkillComponent;
import java.util.List;

final class HymannTreeRewards {
    private HymannTreeRewards() {
    }

    static boolean hasPassive(SkillComponent skills, String abilityId) {
        return skills != null && abilityId != null && skills.hasUnlockedAbility(abilityId);
    }

    static double flatDamage(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_flat_damage_");
    }

    static double lifestealPercent(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_lifesteal_");
    }

    static double comboDamage(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_combo_damage_");
    }

    static double blockPercent(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_block_");
    }

    static double staminaRegenPercent(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_stamina_regen_");
    }

    static double manaRegenPercent(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_mana_regen_");
    }

    static double sorceryPercent(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_sorcery_");
    }

    static double critDamagePercent(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_crit_damage_");
    }

    static double skillDamagePercent(SkillComponent skills) {
        return HymannTreeRewards.sum(skills, "hymann_skill_damage_");
    }

    private static double sum(SkillComponent skills, String prefix) {
        if (skills == null) {
            return 0.0;
        }
        double total = 0.0;
        List<?> rewardTiers = skills.claimedRewards.getOrDefault("HYMANN_ARMAMENT", List.of());
        for (Object rawTier : rewardTiers) {
            if (!(rawTier instanceof String tier)) continue;
            if (tier == null || tier.isBlank()) continue;
            for (String rewardId : tier.split(",")) {
                int valueStart;
                int valueEnd;
                if (!(rewardId = rewardId.trim()).startsWith(prefix) || (valueEnd = rewardId.indexOf("_l", valueStart = prefix.length())) <= valueStart) continue;
                try {
                    total += Double.parseDouble(rewardId.substring(valueStart, valueEnd));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        return total;
    }
}

