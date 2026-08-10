/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 */
package de.shadow.hymann;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;

final class HymannConfig {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile Values values = Values.defaults();
    private static final String VERSION_TWO_SECTION = "\n# HYMMAN CONFIGURATION V2\n# These new values replace the old ratio keys above. Percent values are written as whole percentages.\n# 1.00 always means the normal Hymann baseline; it does not secretly mean 1%.\nconfig.version=2\n\nclass.evolutionPowerMultiplier=1.00\nmmo.skillTreeRewardMultiplier=1.00\nmmo.cooldownReductionPercentAtLevel100=20\nmmo.armamentBaseDamagePercent=0\nmmo.armamentDamagePercentPerLevel=0.10\nmmo.armamentDamageCapPercent=10\nstamina.extraRegenPercentPerSecond=1.2\ncombatSkills.signatureEnergyOnHitPercent=10\n\ncrit.baseChancePercent=25\ncrit.baseBonusDamagePercent=50\ncrit.thunderforgedChancePercent=40\ncrit.thunderforgedBonusDamagePercent=75\ncrit.vanguardChancePercent=55\ncrit.vanguardBonusDamagePercent=100\ncrit.skybreakerChancePercent=70\ncrit.skybreakerBonusDamagePercent=150\ncrit.paragonChancePercent=100\ncrit.paragonBonusDamagePercent=200\n\nstormFury.extraRegenPercent=50\nstormFury.damageTakenPercent=85\nthunderAegis.healPercent=100\nguardAura.manaCostPercent=8\n\n# Mjolnir in the main hand plus a Captain shield creates a permanent boss aura.\n# It marks only Endless Leveling boss-bar monsters within this radius.\nbossAura.radiusBlocks=10\n# A 15% defense break is applied safely as +15% player damage to the marked boss.\nbossAura.defenseReductionPercent=15\n";
    private static final String DEFAULT_FILE = "# Hymann configuration\n# This file is written once by the mod. Edit it while the server is stopped, then restart the server.\n# Multipliers use 1.00 as the normal baseline. Percent values use whole numbers: 25 means 25%.\n# Every setting in this file is in English so it can be shared and understood internationally.\nconfig.version=2\n\n# ENDLESS LEVELING CLASS\n# 1.00 is the standard Hymann evolution tuning. 1.25 means +25% to the class passives and weapon bonus.\nclass.evolutionPowerMultiplier=1.00\n\n# CLASS-EXCLUSIVE CRITICAL STATS\n# Values are Chance % / Bonus Damage % for each Endless evolution stage.\ncrit.baseChancePercent=25\ncrit.baseBonusDamagePercent=50\ncrit.thunderforgedChancePercent=40\ncrit.thunderforgedBonusDamagePercent=75\ncrit.vanguardChancePercent=55\ncrit.vanguardBonusDamagePercent=100\ncrit.skybreakerChancePercent=70\ncrit.skybreakerBonusDamagePercent=150\ncrit.paragonChancePercent=100\ncrit.paragonBonusDamagePercent=200\n\n# STAMINA\n# Extra stamina restored every second by Hymann itself.\nstamina.extraRegenPercentPerSecond=1.2\n\n# DAMAGING ACTIVE SKILLS\n# Lightning Step and Thunder Aegis each restore this percentage of maximum Signature Energy on a successful cast.\ncombatSkills.signatureEnergyOnHitPercent=10\n\n# MMO SKILL TREE\n# Multiplies all selectable Hymann stat rewards in the MMO Skill Tree.\nmmo.skillTreeRewardMultiplier=1.00\n# Total cooldown reduction granted by all ten dedicated Storm Rhythm nodes at level 100.\nmmo.cooldownReductionPercentAtLevel100=20\n# Armament damage applies only to Mjolnir, Captain shields and maces.\nmmo.armamentBaseDamagePercent=0\nmmo.armamentDamagePercentPerLevel=0.10\nmmo.armamentDamageCapPercent=10\n\n# LIGHTNING STEP\nlightningStep.cooldownMs=10000\nlightningStep.manaCost=35\nlightningStep.range=25\nlightningStep.radius=6\nlightningStep.damage=75\nlightningStep.stunMs=1500\n\n# STORM FURY\n# 50 means +50% regeneration. 85 means the player takes 85% damage (15% reduction).\nstormFury.cooldownMs=20000\nstormFury.manaCost=50\nstormFury.durationMs=10000\nstormFury.extraRegenPercent=50\nstormFury.damageTakenPercent=85\n\n# THUNDER AEGIS (ULTIMATE)\n# healPercent=100 fully restores every ally inside the radius.\nthunderAegis.cooldownMs=30000\nthunderAegis.manaCost=75\nthunderAegis.radius=9\nthunderAegis.damage=250\nthunderAegis.healPercent=100\nthunderAegis.stunMs=2000\n\n# GUARD AURA\n# A small defensive pulse while blocking. It is utility, not a primary damage source.\n# Perfect Utils owns the stun so the stated duration is exact.\nguardAura.cooldownMs=2500\nguardAura.stunMs=900\nguardAura.radius=9\nguardAura.minimumManaCost=15\nguardAura.manaCostPercent=8\nguardAura.healBase=10\nguardAura.healPerAscension=5\nguardAura.damageBase=20\nguardAura.damagePerAscension=5\n\n# GUARD WAVE\n# Block with the Captain shield, then use the charged Mjolnir Guard Bash with left mouse button.\n# It creates one small Lightning-Step-style frost explosion around Hymann and does not conflict with Shift-based dodge mods.\nguardWave.cooldownMs=10000\nguardWave.manaCost=35\n# This is the radius of the circular frost explosion around Hymann.\nguardWave.range=7\n# Light initial damage. The control effect is the main purpose of this skill.\nguardWave.damage=85\n# Freeze duration in milliseconds. Enemies are then chilled and slowed for five seconds by EndgameQoL.\nguardWave.stunMs=900\n\n# BOSS DEFENSE AURA\n# Requires Mjolnir in the main hand and any Captain shield equipped in the combined inventory.\nbossAura.radiusBlocks=10\n# 15 means marked boss monsters take 15% more player damage while inside the aura.\nbossAura.defenseReductionPercent=15\n";
    private static final String GUARD_WAVE_SECTION = "# GUARD WAVE\n# Block with the Captain shield, then use the charged Mjolnir Guard Bash with left mouse button.\n# It creates one small Lightning-Step-style frost explosion around Hymann and does not conflict with Shift-based dodge mods.\nguardWave.cooldownMs=10000\nguardWave.manaCost=35\n# This is the radius of the circular frost explosion around Hymann.\nguardWave.range=7\n# Light initial damage. The control effect is the main purpose of this skill.\nguardWave.damage=85\n# Freeze duration in milliseconds. Enemies are then chilled and slowed for five seconds by EndgameQoL.\nguardWave.stunMs=900\n";

    private HymannConfig() {
    }

    static void load(Path file, HytaleLogger logger) {
        if (file == null) {
            return;
        }
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            if (!Files.isRegularFile(file, new LinkOption[0])) {
                Files.writeString(file, (CharSequence)DEFAULT_FILE, StandardCharsets.UTF_8, new OpenOption[0]);
                ((HytaleLogger.Api)logger.atInfo()).log("Created Hymann configuration: %s", (Object)file);
            }
            Properties properties = new Properties();
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);){
                properties.load(reader);
            }
            HymannConfig.appendVersionTwoSettingsIfMissing(file, properties);
            values = Values.from(properties, logger);
            ((HytaleLogger.Api)logger.atInfo()).log("Loaded Hymann configuration from %s", (Object)file);
        }
        catch (IOException error) {
            values = Values.defaults();
            ((HytaleLogger.Api)((HytaleLogger.Api)logger.atWarning()).withCause((Throwable)error)).log("Could not load Hymann configuration; using safe defaults");
        }
    }

    static Values values() {
        return values;
    }

    private static void appendVersionTwoSettingsIfMissing(Path file, Properties properties) throws IOException {
        if ("2".equals(properties.getProperty("config.version"))) {
            return;
        }
        Files.writeString(file, (CharSequence)(System.lineSeparator() + VERSION_TWO_SECTION), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        properties.setProperty("config.version", "2");
        properties.setProperty("class.evolutionPowerMultiplier", "1.00");
        properties.setProperty("mmo.skillTreeRewardMultiplier", "1.00");
        properties.setProperty("mmo.cooldownReductionPercentAtLevel100", "20");
        properties.setProperty("mmo.armamentBaseDamagePercent", "0");
        properties.setProperty("mmo.armamentDamagePercentPerLevel", "0.10");
        properties.setProperty("mmo.armamentDamageCapPercent", "10");
        properties.setProperty("stamina.extraRegenPercentPerSecond", "1.2");
        properties.setProperty("combatSkills.signatureEnergyOnHitPercent", "10");
        properties.setProperty("crit.baseChancePercent", "25");
        properties.setProperty("crit.baseBonusDamagePercent", "50");
        properties.setProperty("crit.thunderforgedChancePercent", "40");
        properties.setProperty("crit.thunderforgedBonusDamagePercent", "75");
        properties.setProperty("crit.vanguardChancePercent", "55");
        properties.setProperty("crit.vanguardBonusDamagePercent", "100");
        properties.setProperty("crit.skybreakerChancePercent", "70");
        properties.setProperty("crit.skybreakerBonusDamagePercent", "150");
        properties.setProperty("crit.paragonChancePercent", "100");
        properties.setProperty("crit.paragonBonusDamagePercent", "200");
        properties.setProperty("stormFury.extraRegenPercent", "50");
        properties.setProperty("stormFury.damageTakenPercent", "85");
        properties.setProperty("thunderAegis.healPercent", "100");
        properties.setProperty("guardAura.manaCostPercent", "8");
        properties.setProperty("bossAura.radiusBlocks", "10");
        properties.setProperty("bossAura.defenseReductionPercent", "15");
    }

    static void applyAbilitySettings(Path abilitiesFile, HytaleLogger logger) {
        if (abilitiesFile == null || !Files.isRegularFile(abilitiesFile, new LinkOption[0])) {
            ((HytaleLogger.Api)logger.atWarning()).log("MMOSkillTree abilities.json was not found; Hymann ability settings were not synced");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(abilitiesFile, StandardCharsets.UTF_8);){
            JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            JsonObject abilities = HymannConfig.object(root, "abilities");
            Values settings = values;
            HymannConfig.configureAbility(abilities, "hymann_thunder_step", "HYMANN_THUNDER_STEP", "Lightning Step", "Blink to a visible hostile enemy, then stun and shock the area.", settings.lightningStepCooldownMs, settings.lightningStepManaCost, HymannConfig.params("range", settings.lightningStepRange, "radius", settings.lightningStepRadius, "damage", Float.valueOf(settings.lightningStepDamage), "stunDurationMs", settings.lightningStepStunMs));
            HymannConfig.configureAbility(abilities, "hymann_storm_fury", "HYMANN_STORM_FURY", "Storm Fury", "For a short time, increase Mana and Signature Energy regeneration and reduce incoming damage.", settings.stormFuryCooldownMs, settings.stormFuryManaCost, HymannConfig.params("durationMs", settings.stormFuryDurationMs));
            HymannConfig.configureAbility(abilities, "hymann_thunder_aegis", "HYMANN_THUNDER_AEGIS", "Thunder Aegis", "Fully heal nearby allies and call down a devastating lightning storm.", settings.thunderAegisCooldownMs, settings.thunderAegisManaCost, HymannConfig.params("damage", Float.valueOf(settings.thunderAegisDamage), "radius", settings.thunderAegisRadius, "healPercentOfMaxHealth", Float.valueOf(settings.thunderAegisHealPercent), "stunDurationMs", settings.thunderAegisStunMs));
            HymannConfig.configurePassive(abilities, "hymann_passive_stormbreaker_aura", 4, "Stormbreaker Aura", "While Mjolnir and a Captain shield are equipped, renew your boss-breaking lightning aura every 5 seconds.");
            HymannConfig.configurePassive(abilities, "hymann_passive_tempest_rebirth", 11, "Tempest Rebirth", "On a kill, restore 20% maximum health and empower your next attack by 50%.");
            HymannConfig.configurePassive(abilities, "hymann_passive_stormbreaker_pulse_1", 37, "Stormbreaker Pulse I", "Every 10 seconds, strike nearby enemies with lightning and shock them for 2 seconds.");
            HymannConfig.configurePassive(abilities, "hymann_passive_stormbreaker_pulse_2", 50, "Stormbreaker Pulse II", "Every 8 seconds, strike nearby enemies with stronger lightning and shock them for 2 seconds.");
            HymannConfig.configurePassive(abilities, "hymann_passive_stormbreaker_pulse_3", 97, "Stormbreaker Pulse III", "Every 5 seconds, strike nearby enemies with devastating lightning and shock them for 2 seconds.");
            HymannConfig.configurePassive(abilities, "hymann_passive_thunder_cadence", 99, "Thunder Cadence", "Every second melee hit deals 1.5x damage and adds 10 lightning true damage.");
            Path temporary = abilitiesFile.resolveSibling(String.valueOf(abilitiesFile.getFileName()) + ".hymann.tmp");
            Files.writeString(temporary, (CharSequence)(JSON.toJson((JsonElement)root) + System.lineSeparator()), StandardCharsets.UTF_8, new OpenOption[0]);
            Files.move(temporary, abilitiesFile, StandardCopyOption.REPLACE_EXISTING);
            ((HytaleLogger.Api)logger.atInfo()).log("Synced Hymann ability costs, cooldowns and values to MMOSkillTree");
        }
        catch (Exception error) {
            ((HytaleLogger.Api)((HytaleLogger.Api)logger.atWarning()).withCause((Throwable)error)).log("Could not sync Hymann ability settings to MMOSkillTree");
        }
    }

    private static JsonObject object(JsonObject parent, String name) {
        if (parent.has(name) && parent.get(name).isJsonObject()) {
            return parent.getAsJsonObject(name);
        }
        JsonObject child = new JsonObject();
        parent.add(name, (JsonElement)child);
        return child;
    }

    private static JsonObject params(Object ... values) {
        JsonObject result = new JsonObject();
        for (int index = 0; index < values.length; index += 2) {
            String key = String.valueOf(values[index]);
            Object value = values[index + 1];
            if (value instanceof Number) {
                Number number = (Number)value;
                result.addProperty(key, number);
                continue;
            }
            result.addProperty(key, String.valueOf(value));
        }
        return result;
    }

    private static void configureAbility(JsonObject abilities, String id, String effect, String displayName, String description, long cooldownMs, int manaCost, JsonObject parameters) {
        JsonObject ability = HymannConfig.object(abilities, id);
        ability.addProperty("cooldownMs", (Number)cooldownMs);
        ability.addProperty("effect", effect);
        ability.addProperty("displayName", displayName);
        ability.addProperty("description", description);
        ability.addProperty("icon", "Weapon_Mjolnir_Starky");
        ability.addProperty("costStat", "MANA");
        ability.addProperty("costAmount", (Number)manaCost);
        ability.add("params", (JsonElement)parameters);
        JsonArray xpSkills = new JsonArray();
        xpSkills.add("HYMANN_ARMAMENT");
        ability.add("xpSkills", (JsonElement)xpSkills);
    }

    private static void configurePassive(JsonObject abilities, String id, int unlockLevel, String displayName, String description) {
        JsonObject ability = HymannConfig.object(abilities, id);
        ability.addProperty("cooldownMs", (Number)0);
        ability.addProperty("effect", "NEXT_HIT_BUFF");
        ability.addProperty("displayName", displayName);
        ability.addProperty("description", "Unlocked at Hymann level " + unlockLevel + ". " + description);
        ability.addProperty("icon", "Weapon_Mjolnir_Starky");
        ability.addProperty("passive", Boolean.valueOf(true));
        JsonObject trigger = new JsonObject();
        trigger.addProperty("event", "hymann_internal");
        JsonObject params = new JsonObject();
        params.addProperty("damageMultiplier", (Number)1.0);
        params.addProperty("armedDurationMs", (Number)1L);
        params.add("passiveTrigger", (JsonElement)trigger);
        ability.add("params", (JsonElement)params);
        JsonArray xpSkills = new JsonArray();
        xpSkills.add("HYMANN_ARMAMENT");
        ability.add("xpSkills", (JsonElement)xpSkills);
    }

    static final class Values {
        final double evolutionBonusScale;
        final double treeRewardScale;
        final double cooldownReductionAtLevel100;
        final double armamentDamageBase;
        final double armamentDamagePerLevel;
        final double armamentDamageCap;
        final float extraStaminaRegenPerSecond;
        final float attackSkillSignatureGainPercent;
        final double baseCritChance;
        final double baseCritBonus;
        final double thunderforgedCritChance;
        final double thunderforgedCritBonus;
        final double vanguardCritChance;
        final double vanguardCritBonus;
        final double skybreakerCritChance;
        final double skybreakerCritBonus;
        final double paragonCritChance;
        final double paragonCritBonus;
        final long lightningStepCooldownMs;
        final int lightningStepManaCost;
        final double lightningStepRange;
        final double lightningStepRadius;
        final float lightningStepDamage;
        final long lightningStepStunMs;
        final long stormFuryCooldownMs;
        final int stormFuryManaCost;
        final long stormFuryDurationMs;
        final float stormFuryExtraRegenMultiplier;
        final float stormFuryDamageTakenMultiplier;
        final long thunderAegisCooldownMs;
        final int thunderAegisManaCost;
        final double thunderAegisRadius;
        final float thunderAegisDamage;
        final float thunderAegisHealPercent;
        final long thunderAegisStunMs;
        final long guardWaveCooldownMs;
        final int guardWaveManaCost;
        final double guardWaveRange;
        final double guardWaveConeDot;
        final float guardWaveDamage;
        final long guardWaveStunMs;
        final long guardAuraCooldownMs;
        final long guardAuraStunMs;
        final double guardAuraRadius;
        final float guardAuraMinManaCost;
        final float guardAuraManaCostPercent;
        final float guardAuraHealBase;
        final float guardAuraHealPerAscension;
        final float guardAuraDamageBase;
        final float guardAuraDamagePerAscension;
        final double bossAuraRadiusBlocks;
        final float bossAuraDamageTakenMultiplier;

        private Values(double evolutionBonusScale, double treeRewardScale, double cooldownReductionAtLevel100, double armamentDamageBase, double armamentDamagePerLevel, double armamentDamageCap, float extraStaminaRegenPerSecond, float attackSkillSignatureGainPercent, double baseCritChance, double baseCritBonus, double thunderforgedCritChance, double thunderforgedCritBonus, double vanguardCritChance, double vanguardCritBonus, double skybreakerCritChance, double skybreakerCritBonus, double paragonCritChance, double paragonCritBonus, long lightningStepCooldownMs, int lightningStepManaCost, double lightningStepRange, double lightningStepRadius, float lightningStepDamage, long lightningStepStunMs, long stormFuryCooldownMs, int stormFuryManaCost, long stormFuryDurationMs, float stormFuryExtraRegenMultiplier, float stormFuryDamageTakenMultiplier, long thunderAegisCooldownMs, int thunderAegisManaCost, double thunderAegisRadius, float thunderAegisDamage, float thunderAegisHealPercent, long thunderAegisStunMs, long guardWaveCooldownMs, int guardWaveManaCost, double guardWaveRange, double guardWaveConeDot, float guardWaveDamage, long guardWaveStunMs, long guardAuraCooldownMs, long guardAuraStunMs, double guardAuraRadius, float guardAuraMinManaCost, float guardAuraManaCostPercent, float guardAuraHealBase, float guardAuraHealPerAscension, float guardAuraDamageBase, float guardAuraDamagePerAscension, double bossAuraRadiusBlocks, float bossAuraDamageTakenMultiplier) {
            this.evolutionBonusScale = evolutionBonusScale;
            this.treeRewardScale = treeRewardScale;
            this.cooldownReductionAtLevel100 = cooldownReductionAtLevel100;
            this.armamentDamageBase = armamentDamageBase;
            this.armamentDamagePerLevel = armamentDamagePerLevel;
            this.armamentDamageCap = armamentDamageCap;
            this.extraStaminaRegenPerSecond = extraStaminaRegenPerSecond;
            this.attackSkillSignatureGainPercent = attackSkillSignatureGainPercent;
            this.baseCritChance = baseCritChance;
            this.baseCritBonus = baseCritBonus;
            this.thunderforgedCritChance = thunderforgedCritChance;
            this.thunderforgedCritBonus = thunderforgedCritBonus;
            this.vanguardCritChance = vanguardCritChance;
            this.vanguardCritBonus = vanguardCritBonus;
            this.skybreakerCritChance = skybreakerCritChance;
            this.skybreakerCritBonus = skybreakerCritBonus;
            this.paragonCritChance = paragonCritChance;
            this.paragonCritBonus = paragonCritBonus;
            this.lightningStepCooldownMs = lightningStepCooldownMs;
            this.lightningStepManaCost = lightningStepManaCost;
            this.lightningStepRange = lightningStepRange;
            this.lightningStepRadius = lightningStepRadius;
            this.lightningStepDamage = lightningStepDamage;
            this.lightningStepStunMs = lightningStepStunMs;
            this.stormFuryCooldownMs = stormFuryCooldownMs;
            this.stormFuryManaCost = stormFuryManaCost;
            this.stormFuryDurationMs = stormFuryDurationMs;
            this.stormFuryExtraRegenMultiplier = stormFuryExtraRegenMultiplier;
            this.stormFuryDamageTakenMultiplier = stormFuryDamageTakenMultiplier;
            this.thunderAegisCooldownMs = thunderAegisCooldownMs;
            this.thunderAegisManaCost = thunderAegisManaCost;
            this.thunderAegisRadius = thunderAegisRadius;
            this.thunderAegisDamage = thunderAegisDamage;
            this.thunderAegisHealPercent = thunderAegisHealPercent;
            this.thunderAegisStunMs = thunderAegisStunMs;
            this.guardWaveCooldownMs = guardWaveCooldownMs;
            this.guardWaveManaCost = guardWaveManaCost;
            this.guardWaveRange = guardWaveRange;
            this.guardWaveConeDot = guardWaveConeDot;
            this.guardWaveDamage = guardWaveDamage;
            this.guardWaveStunMs = guardWaveStunMs;
            this.guardAuraCooldownMs = guardAuraCooldownMs;
            this.guardAuraStunMs = guardAuraStunMs;
            this.guardAuraRadius = guardAuraRadius;
            this.guardAuraMinManaCost = guardAuraMinManaCost;
            this.guardAuraManaCostPercent = guardAuraManaCostPercent;
            this.guardAuraHealBase = guardAuraHealBase;
            this.guardAuraHealPerAscension = guardAuraHealPerAscension;
            this.guardAuraDamageBase = guardAuraDamageBase;
            this.guardAuraDamagePerAscension = guardAuraDamagePerAscension;
            this.bossAuraRadiusBlocks = bossAuraRadiusBlocks;
            this.bossAuraDamageTakenMultiplier = bossAuraDamageTakenMultiplier;
        }

        static Values defaults() {
            return new Values(1.0, 1.0, 0.2, 0.0, 0.001, 0.1, 0.012f, 0.1f, 0.25, 0.5, 0.4, 0.75, 0.55, 1.0, 0.7, 1.5, 1.0, 2.0, 10000L, 35, 25.0, 6.0, 75.0f, 1500L, 20000L, 50, 10000L, 0.5f, 0.85f, 30000L, 75, 9.0, 250.0f, 1.0f, 2000L, 10000L, 35, 7.0, 0.55, 85.0f, 900L, 2500L, 900L, 9.0, 15.0f, 0.08f, 10.0f, 5.0f, 20.0f, 5.0f, 10.0, 1.25f);
        }

        static Values from(Properties input, HytaleLogger logger) {
            Values defaults = Values.defaults();
            return new Values(Values.decimal(input, "class.evolutionPowerMultiplier", defaults.evolutionBonusScale, 0.1, 5.0, logger), Values.decimal(input, "mmo.skillTreeRewardMultiplier", defaults.treeRewardScale, 0.1, 5.0, logger), Values.percent(input, "mmo.cooldownReductionPercentAtLevel100", defaults.cooldownReductionAtLevel100, 0.0, 95.0, logger), Values.percent(input, "mmo.armamentBaseDamagePercent", defaults.armamentDamageBase, 0.0, 500.0, logger), Values.percent(input, "mmo.armamentDamagePercentPerLevel", defaults.armamentDamagePerLevel, 0.0, 100.0, logger), Values.percent(input, "mmo.armamentDamageCapPercent", defaults.armamentDamageCap, 0.0, 1000.0, logger), (float)Values.percent(input, "stamina.extraRegenPercentPerSecond", defaults.extraStaminaRegenPerSecond, 0.0, 100.0, logger), (float)Values.percent(input, "combatSkills.signatureEnergyOnHitPercent", defaults.attackSkillSignatureGainPercent, 0.0, 100.0, logger), Values.percent(input, "crit.baseChancePercent", defaults.baseCritChance, 0.0, 100.0, logger), Values.percent(input, "crit.baseBonusDamagePercent", defaults.baseCritBonus, 0.0, 1000.0, logger), Values.percent(input, "crit.thunderforgedChancePercent", defaults.thunderforgedCritChance, 0.0, 100.0, logger), Values.percent(input, "crit.thunderforgedBonusDamagePercent", defaults.thunderforgedCritBonus, 0.0, 1000.0, logger), Values.percent(input, "crit.vanguardChancePercent", defaults.vanguardCritChance, 0.0, 100.0, logger), Values.percent(input, "crit.vanguardBonusDamagePercent", defaults.vanguardCritBonus, 0.0, 1000.0, logger), Values.percent(input, "crit.skybreakerChancePercent", defaults.skybreakerCritChance, 0.0, 100.0, logger), Values.percent(input, "crit.skybreakerBonusDamagePercent", defaults.skybreakerCritBonus, 0.0, 1000.0, logger), Values.percent(input, "crit.paragonChancePercent", defaults.paragonCritChance, 0.0, 100.0, logger), Values.percent(input, "crit.paragonBonusDamagePercent", defaults.paragonCritBonus, 0.0, 1000.0, logger), Values.whole(input, "lightningStep.cooldownMs", defaults.lightningStepCooldownMs, 250L, 300000L, logger), (int)Values.whole(input, "lightningStep.manaCost", defaults.lightningStepManaCost, 0L, 100000L, logger), Values.decimal(input, "lightningStep.range", defaults.lightningStepRange, 1.0, 100.0, logger), Values.decimal(input, "lightningStep.radius", defaults.lightningStepRadius, 1.0, 50.0, logger), (float)Values.decimal(input, "lightningStep.damage", defaults.lightningStepDamage, 0.0, 1000000.0, logger), Values.whole(input, "lightningStep.stunMs", defaults.lightningStepStunMs, 0L, 30000L, logger), Values.whole(input, "stormFury.cooldownMs", defaults.stormFuryCooldownMs, 250L, 300000L, logger), (int)Values.whole(input, "stormFury.manaCost", defaults.stormFuryManaCost, 0L, 100000L, logger), Values.whole(input, "stormFury.durationMs", defaults.stormFuryDurationMs, 1000L, 120000L, logger), (float)Values.percent(input, "stormFury.extraRegenPercent", defaults.stormFuryExtraRegenMultiplier, 0.0, 2000.0, logger), (float)Values.percent(input, "stormFury.damageTakenPercent", defaults.stormFuryDamageTakenMultiplier, 5.0, 100.0, logger), Values.whole(input, "thunderAegis.cooldownMs", defaults.thunderAegisCooldownMs, 250L, 300000L, logger), (int)Values.whole(input, "thunderAegis.manaCost", defaults.thunderAegisManaCost, 0L, 100000L, logger), Values.decimal(input, "thunderAegis.radius", defaults.thunderAegisRadius, 1.0, 50.0, logger), (float)Values.decimal(input, "thunderAegis.damage", defaults.thunderAegisDamage, 0.0, 1000000.0, logger), (float)Values.percent(input, "thunderAegis.healPercent", defaults.thunderAegisHealPercent, 0.0, 100.0, logger), Values.whole(input, "thunderAegis.stunMs", defaults.thunderAegisStunMs, 0L, 30000L, logger), Values.whole(input, "guardWave.cooldownMs", defaults.guardWaveCooldownMs, 250L, 300000L, logger), (int)Values.whole(input, "guardWave.manaCost", defaults.guardWaveManaCost, 0L, 100000L, logger), Values.decimal(input, "guardWave.range", defaults.guardWaveRange, 1.0, 50.0, logger), Values.decimal(input, "guardWave.coneDot", defaults.guardWaveConeDot, 0.0, 1.0, logger), (float)Values.decimal(input, "guardWave.damage", defaults.guardWaveDamage, 0.0, 1000000.0, logger), Values.whole(input, "guardWave.stunMs", defaults.guardWaveStunMs, 0L, 30000L, logger), Values.whole(input, "guardAura.cooldownMs", defaults.guardAuraCooldownMs, 250L, 300000L, logger), Values.whole(input, "guardAura.stunMs", defaults.guardAuraStunMs, 0L, 30000L, logger), Values.decimal(input, "guardAura.radius", defaults.guardAuraRadius, 1.0, 50.0, logger), (float)Values.decimal(input, "guardAura.minimumManaCost", defaults.guardAuraMinManaCost, 0.0, 100000.0, logger), (float)Values.percent(input, "guardAura.manaCostPercent", defaults.guardAuraManaCostPercent, 0.0, 100.0, logger), (float)Values.decimal(input, "guardAura.healBase", defaults.guardAuraHealBase, 0.0, 1000000.0, logger), (float)Values.decimal(input, "guardAura.healPerAscension", defaults.guardAuraHealPerAscension, 0.0, 1000000.0, logger), (float)Values.decimal(input, "guardAura.damageBase", defaults.guardAuraDamageBase, 0.0, 1000000.0, logger), (float)Values.decimal(input, "guardAura.damagePerAscension", defaults.guardAuraDamagePerAscension, 0.0, 1000000.0, logger), Values.decimal(input, "bossAura.radiusBlocks", defaults.bossAuraRadiusBlocks, 1.0, 32.0, logger), (float)(1.0 + Values.percent(input, "bossAura.defenseReductionPercent", 0.15, 0.0, 75.0, logger)));
        }

        private static double percent(Properties input, String key, double fallbackRatio, double minPercent, double maxPercent, HytaleLogger logger) {
            return Values.decimal(input, key, fallbackRatio * 100.0, minPercent, maxPercent, logger) / 100.0;
        }

        private static double decimal(Properties input, String key, double fallback, double min, double max, HytaleLogger logger) {
            String raw = input.getProperty(key);
            if (raw == null || raw.isBlank()) {
                return fallback;
            }
            try {
                double value = Double.parseDouble(raw.trim());
                if (Double.isFinite(value) && value >= min && value <= max) {
                    return value;
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            ((HytaleLogger.Api)logger.atWarning()).log("Invalid Hymann config value for %s; using default %s", (Object)key, fallback);
            return fallback;
        }

        private static long whole(Properties input, String key, long fallback, long min, long max, HytaleLogger logger) {
            String raw = input.getProperty(key);
            if (raw == null || raw.isBlank()) {
                return fallback;
            }
            try {
                long value = Long.parseLong(raw.trim());
                if (value >= min && value <= max) {
                    return value;
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            ((HytaleLogger.Api)logger.atWarning()).log("Invalid Hymann config value for %s; using default %s", (Object)key, fallback);
            return fallback;
        }
    }
}

