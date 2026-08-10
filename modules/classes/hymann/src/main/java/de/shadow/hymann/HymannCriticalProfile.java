/*
 * Decompiled with CFR 0.152.
 */
package de.shadow.hymann;

import de.shadow.hymann.HymannConfig;

final class HymannCriticalProfile {
    private HymannCriticalProfile() {
    }

    static Values forClassId(String classId) {
        HymannConfig.Values config = HymannConfig.values();
        return switch (classId == null ? "" : classId) {
            case "hymann_thunderforged" -> new Values(config.thunderforgedCritChance, config.thunderforgedCritBonus);
            case "hymann_aegis_vanguard" -> new Values(config.vanguardCritChance, config.vanguardCritBonus);
            case "hymann_skybreaker" -> new Values(config.skybreakerCritChance, config.skybreakerCritBonus);
            case "hymann_eternal_paragon" -> new Values(config.paragonCritChance, config.paragonCritBonus);
            default -> new Values(config.baseCritChance, config.baseCritBonus);
        };
    }

    record Values(double chance, double bonusDamage) {
    }
}

