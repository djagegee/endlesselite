/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.airijko.endlessleveling.api.DamageEventListener
 *  com.airijko.endlessleveling.api.EndlessLevelingAPI
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.server.core.inventory.InventoryComponent
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.Universe
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.config.CustomSkillsConfig
 *  com.ziggfreed.mmoskilltree.config.SkillTreeConfig
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
 *  com.ziggfreed.mmoskilltree.skill.CombatTarget
 *  com.ziggfreed.mmoskilltree.skill.CustomSkill
 *  com.ziggfreed.mmoskilltree.skill.SkillRegistry
 *  com.ziggfreed.mmoskilltree.skill.TriggerType
 *  com.ziggfreed.mmoskilltree.skilltree.SkillReward
 *  com.ziggfreed.mmoskilltree.skilltree.SkillRewardType
 *  com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode
 *  com.ziggfreed.mmoskilltree.skilltree.SkillTreeService
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.api.DamageEventListener;
import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.config.CustomSkillsConfig;
import com.ziggfreed.mmoskilltree.config.SkillTreeConfig;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import com.ziggfreed.mmoskilltree.skill.CombatTarget;
import com.ziggfreed.mmoskilltree.skill.CustomSkill;
import com.ziggfreed.mmoskilltree.skill.SkillRegistry;
import com.ziggfreed.mmoskilltree.skill.TriggerType;
import com.ziggfreed.mmoskilltree.skilltree.SkillReward;
import com.ziggfreed.mmoskilltree.skilltree.SkillRewardType;
import com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode;
import com.ziggfreed.mmoskilltree.skilltree.SkillTreeService;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannConfig;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

final class HymannMmoBridge {
    static final String SKILL_ID = "HYMANN_ARMAMENT";
    private static final String ICON_PREFIX = "Hymann_Skill_";
    private static final String SKILL_ICON = "Hymann_Skill_Stormbreaker";
    private static final String DAMAGE_SOURCE = "shadow_hymann_armament";
    private static final List<Integer> TREE_LEVELS = List.of(2, 4, 5, 10, 11, 12, 15, 18, 20, 22, 25, 30, 32, 33, 35, 37, 38, 40, 44, 45, 50, 55, 60, 62, 65, 68, 70, 72, 75, 80, 85, 90, 92, 95, 97, 98, 99, 100);
    private static final long DAMAGE_BONUS_TTL_MS = 750L;
    private static final double BASE_SKILL_TREE_REWARD_MULTIPLIER = 0.25;
    private final HytaleLogger logger;
    private final DamageEventListener damageListener = this::onDamage;
    private boolean registered;

    HymannMmoBridge(HytaleLogger logger) {
        this.logger = logger;
    }

    void register() {
        if (this.registered) {
            return;
        }
        this.registerSkillCategory();
        EndlessLevelingAPI.get().registerDamageEventListener(this.damageListener);
        this.registered = true;
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered MMOSkillTree category %s for Starky armaments", (Object)SKILL_ID);
    }

    void unregister() {
        if (!this.registered) {
            return;
        }
        EndlessLevelingAPI.get().unregisterDamageEventListener(this.damageListener);
        this.registered = false;
    }

    private void registerSkillCategory() {
        SkillTreeConfig trees;
        SkillRegistry registry;
        CustomSkill custom = new CustomSkill(SKILL_ID, "Hymann", "COMBAT", "Hymann mastery for Mjolnir, Captain shields and maces. Its damage bonus applies only to those armaments.", EnumSet.of(TriggerType.DEAL_DAMAGE_PHYSICAL), "BLUNT", SKILL_ICON);
        CustomSkillsConfig config = CustomSkillsConfig.getInstance();
        if (config != null) {
            config.removeCustomSkill(SKILL_ID);
            config.addCustomSkill(custom);
            config.removeCustomSkill("HYMANN_CRITICAL");
            config.setMaxLevel(SKILL_ID, 100);
            config.save();
            SkillRegistry.reloadFromConfig();
        }
        if ((registry = SkillRegistry.getInstance()) != null && !registry.isKnownSkill(SKILL_ID)) {
            registry.register(custom);
        }
        if ((trees = SkillTreeConfig.getInstance()) != null) {
            trees.setSkillMilestonesByName(SKILL_ID, TREE_LEVELS);
            trees.setSkillTreeByName(SKILL_ID, HymannMmoBridge.buildTree());
            trees.save();
        }
    }

    private static List<SkillTreeNode> buildTree() {
        ArrayList<SkillTreeNode> nodes = new ArrayList<SkillTreeNode>();
        for (int index = 0; index < TREE_LEVELS.size(); ++index) {
            int tier = index + 1;
            TreeTier definition = HymannMmoBridge.treeTier(TREE_LEVELS.get(index));
            nodes.add(new SkillTreeNode(tier, TREE_LEVELS.get(index).intValue(), definition.choicesRequired(), definition.choices()));
        }
        return nodes;
    }

    private static TreeTier treeTier(int level) {
        return switch (level) {
            case 2 -> HymannMmoBridge.tier(1, HymannMmoBridge.active("hymann_thunder_step", "Lightning Step", "Teleport to a visible enemy and unleash a lightning impact."));
            case 4 -> HymannMmoBridge.tier(1, HymannMmoBridge.passive("hymann_passive_stormbreaker_aura", "Stormbreaker Aura", 4, "Every 5 seconds during combat, the aura renews and shocks nearby bosses."));
            case 5 -> HymannMmoBridge.tier(1, HymannMmoBridge.xp(10.0, level));
            case 10 -> HymannMmoBridge.tier(1, HymannMmoBridge.flatDamage(2.0, level), HymannMmoBridge.lifesteal(2.0, level));
            case 11 -> HymannMmoBridge.tier(1, HymannMmoBridge.passive("hymann_passive_tempest_rebirth", "Tempest Rebirth", 11, "On a kill, heal for 20% of maximum health and empower the next attack by 50%."));
            case 12 -> HymannMmoBridge.tier(1, HymannMmoBridge.cooldown(10.0, level));
            case 15 -> HymannMmoBridge.tier(2, HymannMmoBridge.flatDamage(3.0, level), HymannMmoBridge.lifesteal(3.0, level), HymannMmoBridge.maximumStamina(10.0, level));
            case 18 -> HymannMmoBridge.tier(2, HymannMmoBridge.cooldown(10.0, level), HymannMmoBridge.block(5.0, level));
            case 20 -> HymannMmoBridge.tier(3, HymannMmoBridge.flatDamage(4.0, level), HymannMmoBridge.lifesteal(4.0, level), HymannMmoBridge.staminaRegen(1.0, level), HymannMmoBridge.manaRegen(1.0, level));
            case 22 -> HymannMmoBridge.tier(2, HymannMmoBridge.cooldown(10.0, level), HymannMmoBridge.sorcery(10.0, level));
            case 25 -> HymannMmoBridge.tier(1, HymannMmoBridge.critDamage(10.0, level), HymannMmoBridge.cooldown(10.0, level), HymannMmoBridge.fallDamageReduction(15.0, level));
            case 30 -> HymannMmoBridge.tier(3, HymannMmoBridge.xp(10.0, level), HymannMmoBridge.flatDamage(5.0, level), HymannMmoBridge.lifesteal(5.0, level), HymannMmoBridge.staminaRegen(1.0, level), HymannMmoBridge.manaRegen(1.0, level));
            case 32 -> HymannMmoBridge.tier(1, HymannMmoBridge.active("hymann_storm_fury", "Storm Fury", "For 10 seconds, dramatically improve mana and signature regeneration and reduce damage taken."));
            case 33 -> HymannMmoBridge.tier(1, HymannMmoBridge.fallDamageReduction(20.0, level), HymannMmoBridge.staminaRegen(5.0, level));
            case 35 -> HymannMmoBridge.tier(2, HymannMmoBridge.flatDamage(4.0, level), HymannMmoBridge.lifesteal(4.0, level), HymannMmoBridge.staminaRegen(1.0, level), HymannMmoBridge.manaRegen(1.0, level));
            case 37 -> HymannMmoBridge.tier(1, HymannMmoBridge.passive("hymann_passive_stormbreaker_pulse_1", "Stormbreaker Pulse I", 37, "Every 10 seconds, strike enemies within 8 blocks for lightning damage and shock them for 2 seconds."));
            case 38 -> HymannMmoBridge.tier(1, HymannMmoBridge.block(5.0, level));
            case 40 -> HymannMmoBridge.tier(3, HymannMmoBridge.xp(10.0, level), HymannMmoBridge.flatDamage(5.0, level), HymannMmoBridge.lifesteal(5.0, level), HymannMmoBridge.critDamage(10.0, level), HymannMmoBridge.skillDamage(10.0, level));
            case 44 -> HymannMmoBridge.tier(1, HymannMmoBridge.active("hymann_thunder_aegis", "Thunder Aegis", "Fully heal nearby allies and call down a devastating lightning storm."));
            case 45 -> HymannMmoBridge.tier(3, HymannMmoBridge.flatDamage(5.0, level), HymannMmoBridge.comboDamage(10.0, level), HymannMmoBridge.critDamage(10.0, level), HymannMmoBridge.skillDamage(10.0, level));
            case 50 -> HymannMmoBridge.tier(1, HymannMmoBridge.passive("hymann_passive_stormbreaker_pulse_2", "Stormbreaker Pulse II", 50, "Every 8 seconds, strike enemies within 8 blocks for stronger lightning damage and shock them for 2 seconds."));
            case 55, 60 -> HymannMmoBridge.tier(3, HymannMmoBridge.xp(10.0, level), HymannMmoBridge.flatDamage(5.0, level), HymannMmoBridge.comboDamage(10.0, level), HymannMmoBridge.staminaRegen(2.0, level), HymannMmoBridge.manaRegen(2.0, level));
            case 62 -> HymannMmoBridge.tier(1, HymannMmoBridge.fallDamageReduction(20.0, level), HymannMmoBridge.staminaRegen(5.0, level), HymannMmoBridge.manaRegen(5.0, level));
            case 65, 70 -> HymannMmoBridge.tier(3, HymannMmoBridge.xp(10.0, level), HymannMmoBridge.flatDamage(5.0, level), HymannMmoBridge.lifesteal(5.0, level), HymannMmoBridge.critDamage(10.0, level), HymannMmoBridge.skillDamage(10.0, level));
            case 68, 72 -> HymannMmoBridge.tier(2, HymannMmoBridge.block(5.0, level), HymannMmoBridge.sorcery(10.0, level));
            case 75 -> HymannMmoBridge.tier(3, HymannMmoBridge.xp(10.0, level), HymannMmoBridge.flatDamage(5.0, level), HymannMmoBridge.comboDamage(10.0, level), HymannMmoBridge.staminaRegen(2.0, level), HymannMmoBridge.manaRegen(2.0, level));
            case 80 -> HymannMmoBridge.tier(4, HymannMmoBridge.xp(10.0, level), HymannMmoBridge.flatDamage(8.0, level), HymannMmoBridge.comboDamage(12.0, level), HymannMmoBridge.staminaRegen(2.0, level), HymannMmoBridge.manaRegen(2.0, level));
            case 85, 90, 95 -> HymannMmoBridge.tier(4, HymannMmoBridge.block(5.0, level), HymannMmoBridge.flatDamage(10.0, level), HymannMmoBridge.comboDamage(15.0, level), HymannMmoBridge.critDamage(10.0, level), HymannMmoBridge.skillDamage(10.0, level));
            case 92 -> HymannMmoBridge.tier(4, HymannMmoBridge.staminaRegen(5.0, level), HymannMmoBridge.manaRegen(5.0, level), HymannMmoBridge.critDamage(10.0, level), HymannMmoBridge.skillDamage(10.0, level));
            case 97 -> HymannMmoBridge.tier(1, HymannMmoBridge.passive("hymann_passive_stormbreaker_pulse_3", "Stormbreaker Pulse III", 97, "Every 5 seconds, strike enemies within 8 blocks for devastating lightning damage and shock them for 2 seconds."));
            case 98 -> HymannMmoBridge.tier(2, HymannMmoBridge.block(5.0, level), HymannMmoBridge.sorcery(10.0, level));
            case 99 -> HymannMmoBridge.tier(1, HymannMmoBridge.passive("hymann_passive_thunder_cadence", "Thunder Cadence", 99, "Every second melee hit deals 1.5x damage and adds 10 lightning true damage."));
            case 100 -> HymannMmoBridge.tier(5, HymannMmoBridge.block(10.0, level), HymannMmoBridge.cooldown(10.0, level), HymannMmoBridge.lifesteal(15.0, level), HymannMmoBridge.critDamage(10.0, level), HymannMmoBridge.skillDamage(10.0, level));
            default -> throw new IllegalArgumentException("Missing Hymann tree definition for level " + level);
        };
    }

    private static TreeTier tier(int choicesRequired, SkillReward ... choices) {
        return new TreeTier(choicesRequired, List.of(choices));
    }

    private static SkillReward xp(double percent, int level) {
        return HymannMmoBridge.localized("xp", HymannMmoBridge.armament("hymann_xp_" + HymannMmoBridge.whole(percent) + "_l" + level, SkillRewardType.BONUS_XP, percent / 100.0, "Stormbound Study", "+" + HymannMmoBridge.whole(percent) + "% Hymann experience from Hymann armament hits."));
    }

    private static SkillReward flatDamage(double amount, int level) {
        return HymannMmoBridge.localized("flat_damage", HymannMmoBridge.armament("hymann_flat_damage_" + HymannMmoBridge.whole(amount) + "_l" + level, SkillRewardType.FLAT_DAMAGE, amount, "Armament Mastery", "+" + HymannMmoBridge.whole(amount) + " flat damage with Hymann armaments."));
    }

    private static SkillReward lifesteal(double percent, int level) {
        return HymannMmoBridge.localized("lifesteal", HymannMmoBridge.armament("hymann_lifesteal_" + HymannMmoBridge.whole(percent) + "_l" + level, SkillRewardType.LIFESTEAL, percent / 100.0, "Storm Leech", "Restore " + HymannMmoBridge.whole(percent) + "% of damage dealt by Hymann armaments."));
    }

    private static SkillReward comboDamage(double amount, int level) {
        return HymannMmoBridge.localized("combo_damage", HymannMmoBridge.armament("hymann_combo_damage_" + HymannMmoBridge.whole(amount) + "_l" + level, SkillRewardType.FLAT_COMBO_DAMAGE, amount, "Thunder Combo", "+" + HymannMmoBridge.whole(amount) + " combo damage with Hymann armaments."));
    }

    private static SkillReward cooldown(double percent, int level) {
        return HymannMmoBridge.localized("cooldown", new SkillReward("hymann_cooldown_" + HymannMmoBridge.whole(percent) + "_l" + level, SkillRewardType.COOLDOWN_REDUCTION, percent / 100.0, "Storm Rhythm", "+" + HymannMmoBridge.whole(percent) + "% cooldown reduction", CombatTarget.ALL).withIcon(HymannMmoBridge.icon("Cooldown")));
    }

    private static SkillReward maximumStamina(double amount, int level) {
        return HymannMmoBridge.localized("maximum_stamina", new SkillReward("hymann_max_stamina_" + HymannMmoBridge.whole(amount) + "_l" + level, SkillRewardType.STAT_STAMINA, amount, "Aegis Reserve", "+" + HymannMmoBridge.whole(amount) + " maximum stamina", CombatTarget.ALL).withIcon(HymannMmoBridge.icon("Stamina")));
    }

    private static SkillReward fallDamageReduction(double percent, int level) {
        return HymannMmoBridge.localized("fall_damage", new SkillReward("hymann_fall_reduction_" + HymannMmoBridge.whole(percent) + "_l" + level, SkillRewardType.FALL_DAMAGE_REDUCTION, percent / 100.0, "Skyfall Guard", "+" + HymannMmoBridge.whole(percent) + "% fall damage reduction", CombatTarget.ALL).withIcon(HymannMmoBridge.icon("Fall_Guard")));
    }

    private static SkillReward block(double percent, int level) {
        return HymannMmoBridge.localized("block", new SkillReward("hymann_block_" + HymannMmoBridge.whole(percent) + "_l" + level, SkillRewardType.STAT_DEFENSE, percent / 100.0, "Aegis Guard", "+" + HymannMmoBridge.whole(percent) + "% damage reduction", CombatTarget.ALL).withIcon(HymannMmoBridge.icon("Block")));
    }

    private static SkillReward staminaRegen(double percent, int level) {
        return HymannMmoBridge.localized("stamina_regen", HymannMmoBridge.custom("hymann_stamina_regen_" + HymannMmoBridge.whole(percent) + "_l" + level, percent, "Windrunner", "+" + HymannMmoBridge.whole(percent) + "% stamina regeneration while using Hymann armaments."));
    }

    private static SkillReward manaRegen(double percent, int level) {
        return HymannMmoBridge.localized("mana_regen", HymannMmoBridge.custom("hymann_mana_regen_" + HymannMmoBridge.whole(percent) + "_l" + level, percent, "Stormwell", "+" + HymannMmoBridge.whole(percent) + "% mana regeneration while using Hymann armaments."));
    }

    private static SkillReward sorcery(double percent, int level) {
        return HymannMmoBridge.localized("sorcery", HymannMmoBridge.custom("hymann_sorcery_" + HymannMmoBridge.whole(percent) + "_l" + level, percent, "Lightning Lore", "+" + HymannMmoBridge.whole(percent) + " flat magic damage for Hymann lightning abilities."));
    }

    private static SkillReward critDamage(double percent, int level) {
        return HymannMmoBridge.localized("crit_damage", HymannMmoBridge.custom("hymann_crit_damage_" + HymannMmoBridge.whole(percent) + "_l" + level, percent, "Thunderforged Fury", "+" + HymannMmoBridge.whole(percent) + "% critical damage for Hymann."));
    }

    private static SkillReward skillDamage(double percent, int level) {
        return HymannMmoBridge.localized("skill_damage", HymannMmoBridge.custom("hymann_skill_damage_" + HymannMmoBridge.whole(percent) + "_l" + level, percent, "Stormcraft", "+" + HymannMmoBridge.whole(percent) + "% damage for Hymann skills."));
    }

    private static SkillReward passive(String id, String name, int unlockLevel, String description) {
        return HymannMmoBridge.localized("passive." + id, SkillReward.forAbility((String)id, (SkillRewardType)SkillRewardType.ABILITY_UNLOCK, (double)0.0, (String)name, (String)("Unlocked at Hymann level " + unlockLevel + ". " + description), (String)id).withIcon(HymannMmoBridge.abilityIcon(id)));
    }

    private static SkillReward active(String id, String name, String description) {
        return HymannMmoBridge.localized("active." + id, SkillReward.forAbility((String)id, (SkillRewardType)SkillRewardType.ABILITY_UNLOCK, (double)0.0, (String)name, (String)description, (String)id).withIcon(HymannMmoBridge.abilityIcon(id)));
    }

    private static SkillReward armament(String id, SkillRewardType type, double value, String name, String description) {
        return new SkillReward(id, type, value, name, description, CombatTarget.ALL, SKILL_ID, "hymann_internal").withIcon(HymannMmoBridge.armamentIcon(id));
    }

    private static SkillReward custom(String id, double value, String name, String description) {
        return new SkillReward(id, SkillRewardType.ABILITY_MOD, value, name, description, CombatTarget.ALL, SKILL_ID, "hymann_internal").withIcon(HymannMmoBridge.customIcon(id));
    }

    private static String icon(String name) {
        return ICON_PREFIX + name;
    }

    private static String armamentIcon(String id) {
        if (id.contains("_xp_")) {
            return HymannMmoBridge.icon("XP");
        }
        if (id.contains("_lifesteal_")) {
            return HymannMmoBridge.icon("Lifesteal");
        }
        if (id.contains("_combo_damage_")) {
            return HymannMmoBridge.icon("Combo");
        }
        return HymannMmoBridge.icon("Damage");
    }

    private static String customIcon(String id) {
        if (id.contains("_stamina_regen_")) {
            return HymannMmoBridge.icon("Stamina_Regen");
        }
        if (id.contains("_mana_regen_")) {
            return HymannMmoBridge.icon("Mana_Regen");
        }
        if (id.contains("_sorcery_")) {
            return HymannMmoBridge.icon("Sorcery");
        }
        if (id.contains("_crit_damage_")) {
            return HymannMmoBridge.icon("Crit");
        }
        if (id.contains("_skill_damage_")) {
            return HymannMmoBridge.icon("Storm_Fury");
        }
        return HymannMmoBridge.icon("Stormbreaker");
    }

    private static String abilityIcon(String id) {
        return switch (id) {
            case "hymann_thunder_step" -> HymannMmoBridge.icon("Thunder_Step");
            case "hymann_storm_fury" -> HymannMmoBridge.icon("Storm_Fury");
            case "hymann_thunder_aegis" -> HymannMmoBridge.icon("Thunder_Aegis");
            case "hymann_passive_stormbreaker_aura", "hymann_passive_stormbreaker_pulse_1", "hymann_passive_stormbreaker_pulse_2", "hymann_passive_stormbreaker_pulse_3" -> HymannMmoBridge.icon("Stormbreaker");
            case "hymann_passive_tempest_rebirth" -> HymannMmoBridge.icon("Storm_Fury");
            case "hymann_passive_thunder_cadence" -> HymannMmoBridge.icon("Combo");
            default -> HymannMmoBridge.icon("Stormbreaker");
        };
    }

    private static SkillReward localized(String key, SkillReward reward) {
        return reward.withDisplayKeys("hymann.tree." + key + ".name", "hymann.tree." + key + ".description");
    }

    private static String whole(double value) {
        return Integer.toString((int)Math.round(value));
    }

    private void onDamage(UUID playerUuid, int targetEntityId, double damage, Object source, boolean critical) {
        PlayerRef player = Universe.get().getPlayer(playerUuid);
        if (player == null || !HymannMmoBridge.isHymann(playerUuid)) {
            return;
        }
        Ref playerEntityRef = player.getReference();
        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            return;
        }
        Store store = playerEntityRef.getStore();
        ItemStack heldItem = InventoryComponent.getItemInHand((ComponentAccessor)store, (Ref)playerEntityRef);
        if (heldItem == null || !HymannAccess.isArmament(heldItem.getItemId())) {
            EndlessLevelingAPI.get().setExternalDamageBonus(playerUuid, DAMAGE_SOURCE, 0.0, System.currentTimeMillis() + 1L);
            return;
        }
        HymannMmoBridge.applyArmamentBonus(playerUuid, (Store<EntityStore>)store, (Ref<EntityStore>)playerEntityRef);
    }

    static boolean isHymann(UUID playerUuid) {
        String classId = EndlessLevelingAPI.get().getPrimaryClassId(playerUuid);
        return classId != null && classId.startsWith("hymann");
    }

    private static void applyArmamentBonus(UUID playerUuid, Store<EntityStore> store, Ref<EntityStore> playerEntityRef) {
        int level = Math.max(0, MMOSkillTreeAPI.getLevel(store, playerEntityRef, (String)SKILL_ID));
        HymannConfig.Values config = HymannConfig.values();
        double bonus = Math.min(config.armamentDamageCap, config.armamentDamageBase + (double)level * config.armamentDamagePerLevel);
        EndlessLevelingAPI.get().setExternalDamageBonus(playerUuid, DAMAGE_SOURCE, bonus, System.currentTimeMillis() + 750L);
    }

    static int ensureMilestoneAbilities(Store<EntityStore> store, Ref<EntityStore> playerEntityRef) {
        int level = MMOSkillTreeAPI.getLevel(store, playerEntityRef, (String)SKILL_ID);
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, playerEntityRef);
        if (skills == null) {
            return 0;
        }
        PlayerRef player = (PlayerRef)store.getComponent(playerEntityRef, PlayerRef.getComponentType());
        int pending = player == null ? 0 : SkillTreeService.checkMilestonesByName((PlayerRef)player, (SkillComponent)skills, (String)SKILL_ID).size();
        int before = HymannMmoBridge.customAbilityCount(skills);
        HymannMmoBridge.syncCustomAbilityUnlocks(skills, level);
        return pending + Math.max(0, HymannMmoBridge.customAbilityCount(skills) - before);
    }

    static void syncCustomAbilityUnlocks(SkillComponent skills, int level) {
        List claimed = skills.claimedRewards.getOrDefault(SKILL_ID, List.of());
        HymannMmoBridge.syncCustomAbility(skills, "hymann_thunder_step", claimed.contains("hymann_thunder_step") || level >= 2);
        HymannMmoBridge.syncCustomAbility(skills, "hymann_storm_fury", claimed.contains("hymann_storm_fury") || level >= 32);
        HymannMmoBridge.syncCustomAbility(skills, "hymann_thunder_aegis", claimed.contains("hymann_thunder_aegis") || level >= 44);
        for (String passiveId : List.of("hymann_passive_stormbreaker_aura", "hymann_passive_tempest_rebirth", "hymann_passive_stormbreaker_pulse_1", "hymann_passive_stormbreaker_pulse_2", "hymann_passive_stormbreaker_pulse_3", "hymann_passive_thunder_cadence")) {
            HymannMmoBridge.syncCustomAbility(skills, passiveId, claimed.contains(passiveId));
        }
    }

    private static void syncCustomAbility(SkillComponent skills, String abilityId, boolean unlocked) {
        if (unlocked) {
            HymannMmoBridge.unlockIfMissing(skills, abilityId);
        } else {
            skills.lockAbility(abilityId);
        }
    }

    private static int customAbilityCount(SkillComponent skills) {
        int count = 0;
        for (String abilityId : List.of("hymann_thunder_step", "hymann_storm_fury", "hymann_thunder_aegis")) {
            if (!skills.hasUnlockedAbility(abilityId)) continue;
            ++count;
        }
        return count;
    }

    private static int unlockIfMissing(SkillComponent skills, String abilityId) {
        if (skills.hasUnlockedAbility(abilityId)) {
            return 0;
        }
        skills.unlockAbility(abilityId);
        return 1;
    }

    private static String format(double value) {
        return value == Math.rint(value) ? Integer.toString((int)value) : String.format("%.1f", value);
    }

    private static String formatPercent(double value) {
        return Integer.toString((int)Math.round(value * 100.0)) + "%";
    }

    private static double scaled(double value) {
        return value * 0.25 * HymannConfig.values().treeRewardScale;
    }

    private record TreeTier(int choicesRequired, List<SkillReward> choices) {
    }
}
