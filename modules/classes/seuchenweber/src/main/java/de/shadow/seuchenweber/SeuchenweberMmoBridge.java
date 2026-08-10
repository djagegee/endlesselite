package de.shadow.seuchenweber;

import com.hypixel.hytale.logger.HytaleLogger;
import com.ziggfreed.mmoskilltree.config.CustomSkillsConfig;
import com.ziggfreed.mmoskilltree.config.SkillTreeConfig;
import com.ziggfreed.mmoskilltree.skill.CombatTarget;
import com.ziggfreed.mmoskilltree.skill.CustomSkill;
import com.ziggfreed.mmoskilltree.skill.SkillRegistry;
import com.ziggfreed.mmoskilltree.skill.TriggerType;
import com.ziggfreed.mmoskilltree.skilltree.SkillReward;
import com.ziggfreed.mmoskilltree.skilltree.SkillRewardType;
import com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Dedicated MMOSkillTree category with seven ability unlocks and 31 class-focused bonus tiers. */
final class SeuchenweberMmoBridge {
  static final String SKILL_ID = "SEUCHENWEBER_MASTERY";
  static final String SKILL_ICON = "Seuchenweber_Skill_XP";
  private static final List<Integer> TREE_LEVELS = List.of(
      1, 2, 4, 5, 8, 12, 15, 18, 20, 22, 25, 28, 30, 32, 35, 38, 40, 42, 45,
      48, 50, 52, 55, 58, 60, 62, 65, 68, 70, 72, 75, 78, 80, 85, 90, 92, 95, 100);
  private static final List<TreeUnlock> TREE_UNLOCKS = List.of(
      new TreeUnlock(2, "seuchenweber_seal_of_decay", "Seal of Decay", false),
      new TreeUnlock(1, "seuchenweber_passive_necrotoxic_mastery", "Blight Breath", true),
      new TreeUnlock(25, "seuchenweber_astral_rift", "Astral Rift", false),
      new TreeUnlock(40, "seuchenweber_passive_astral_echo", "Astral Echo", true),
      new TreeUnlock(55, "seuchenweber_chronoblight", "Chronoblight", false),
      new TreeUnlock(70, "seuchenweber_passive_soul_diagnosis", "Soul Diagnosis", true),
      new TreeUnlock(90, "seuchenweber_passive_relic_attunement", "Relic Attunement", true));

  private final HytaleLogger logger;

  SeuchenweberMmoBridge(HytaleLogger logger) {
    this.logger = logger;
  }

  void register() {
    CustomSkill skill = new CustomSkill(
        SKILL_ID, "Seuchenweber", "COMBAT",
        "Mastery of the Cosmic Ruin Spellbook and plague-woven abilities.",
        EnumSet.of(TriggerType.DEAL_DAMAGE_PHYSICAL), "MAGIC", SKILL_ICON);
    CustomSkillsConfig customSkills = CustomSkillsConfig.getInstance();
    if (customSkills != null) {
      customSkills.removeCustomSkill(SKILL_ID);
      customSkills.addCustomSkill(skill);
      customSkills.setMaxLevel(SKILL_ID, 100);
      customSkills.save();
      SkillRegistry.reloadFromConfig();
    }
    SkillRegistry registry = SkillRegistry.getInstance();
    if (registry != null && !registry.isKnownSkill(SKILL_ID)) registry.register(skill);
    SkillTreeConfig trees = SkillTreeConfig.getInstance();
    if (trees != null) {
      trees.setSkillMilestonesByName(SKILL_ID, TREE_LEVELS);
      trees.setSkillTreeByName(SKILL_ID, buildTree());
      trees.save();
    }
    ((HytaleLogger.Api) logger.atInfo()).log(
        "Registered MMOSkillTree category %s with 38 milestones, 3 active and 4 passive unlocks", SKILL_ID);
  }

  static Set<String> activeAbilityIds() { return ids(false); }
  static Set<String> passiveAbilityIds() { return ids(true); }
  static List<TreeUnlock> treeUnlocks() { return TREE_UNLOCKS; }

  private static Set<String> ids(boolean passive) {
    LinkedHashSet<String> ids = new LinkedHashSet<>();
    TREE_UNLOCKS.stream().filter(entry -> entry.passive() == passive)
        .map(TreeUnlock::abilityId).forEach(ids::add);
    return Set.copyOf(ids);
  }

  static List<SkillTreeNode> buildTree() {
    ArrayList<SkillTreeNode> nodes = new ArrayList<>();
    for (int index = 0; index < TREE_LEVELS.size(); index++) {
      int level = TREE_LEVELS.get(index);
      TreeTier tier = treeTier(level);
      nodes.add(new SkillTreeNode(index + 1, level, tier.choicesRequired(), tier.choices()));
    }
    return List.copyOf(nodes);
  }

  private static TreeTier treeTier(int level) {
    TreeUnlock unlock = TREE_UNLOCKS.stream().filter(value -> value.level() == level).findFirst().orElse(null);
    if (unlock != null) return tier(1, abilityReward(unlock));
    return switch (level) {
      case 4 -> tier(1, cooldown(5, level));
      case 5 -> tier(1, manaRegen(5, level));
      case 8 -> tier(1, criticalChance(2.5, level), defense(3, level));
      case 12 -> tier(1, cooldown(5, level), manaRegen(5, level));
      case 15 -> tier(2, manaRegen(5, level), staminaRegen(5, level), directDamage(5, level));
      case 18 -> tier(1, cooldown(5, level), dotDamage(5, level));
      case 20 -> tier(2, criticalChance(2.5, level), criticalDamage(10, level), defense(3, level));
      case 22 -> tier(2, cooldown(5, level), manaRegen(5, level), dotDamage(5, level));
      case 28 -> tier(2, manaRegen(5, level), staminaRegen(5, level), directDamage(5, level));
      case 30 -> tier(2, cooldown(5, level), dotDamage(5, level), defense(3, level));
      case 32 -> tier(2, manaRegen(5, level), criticalChance(2.5, level), criticalDamage(10, level));
      case 35 -> tier(2, cooldown(5, level), manaRegen(5, level), directDamage(5, level));
      case 38 -> tier(2, defense(3, level), dotDamage(5, level), staminaRegen(5, level));
      case 42 -> tier(2, cooldown(5, level), manaRegen(5, level), dotDamage(5, level));
      case 45 -> tier(2, criticalChance(2.5, level), criticalDamage(10, level), directDamage(5, level));
      case 48 -> tier(2, cooldown(5, level), manaRegen(5, level), staminaRegen(5, level));
      case 50 -> tier(2, defense(3, level), dotDamage(5, level), directDamage(5, level));
      case 52 -> tier(2, cooldown(5, level), manaRegen(5, level), criticalChance(2.5, level));
      case 58 -> tier(2, cooldown(5, level), manaRegen(5, level), dotDamage(5, level));
      case 60 -> tier(2, defense(3, level), criticalDamage(10, level), directDamage(5, level));
      case 62 -> tier(2, cooldown(5, level), manaRegen(5, level), staminaRegen(5, level));
      case 65 -> tier(2, dotDamage(5, level), directDamage(5, level), criticalChance(2.5, level));
      case 68 -> tier(2, cooldown(5, level), manaRegen(5, level), defense(3, level));
      case 72 -> tier(2, cooldown(5, level), dotDamage(5, level), criticalDamage(10, level));
      case 75 -> tier(2, manaRegen(5, level), staminaRegen(5, level), directDamage(5, level));
      case 78 -> tier(2, cooldown(5, level), manaRegen(5, level), criticalChance(2.5, level));
      case 80 -> tier(2, defense(3, level), dotDamage(5, level), directDamage(5, level));
      case 85 -> tier(2, cooldown(5, level), manaRegen(5, level), criticalDamage(10, level));
      case 92 -> tier(2, cooldown(5, level), manaRegen(5, level), dotDamage(5, level));
      case 95 -> tier(2, criticalChance(2.5, level), criticalDamage(10, level), directDamage(5, level));
      case 100 -> tier(3, cooldown(10, level), manaRegen(10, level), dotDamage(10, level),
          criticalDamage(15, level), defense(5, level));
      default -> throw new IllegalArgumentException("Missing Seuchenweber tree definition for level " + level);
    };
  }

  private static TreeTier tier(int required, SkillReward... rewards) {
    return new TreeTier(required, List.of(rewards));
  }

  private static SkillReward cooldown(double percent, int level) {
    return nativePercent("cooldown", SkillRewardType.COOLDOWN_REDUCTION, percent, level,
        "Chronic Acceleration", "+" + format(percent) + "% cooldown reduction");
  }

  private static SkillReward manaRegen(double percent, int level) {
    return nativePercent("mana_regen", SkillRewardType.MANA_REGEN, percent, level,
        "Astral Influx", "+" + format(percent) + "% mana regeneration");
  }

  private static SkillReward criticalChance(double percent, int level) {
    return nativePercent("critical_chance", SkillRewardType.CRITICAL_CHANCE, percent, level,
        "Virulent Precision", "+" + format(percent) + "% critical hit chance");
  }

  private static SkillReward defense(double percent, int level) {
    return nativePercent("defense", SkillRewardType.STAT_DEFENSE, percent, level,
        "Plague Armor", "+" + format(percent) + "% damage reduction");
  }

  private static SkillReward staminaRegen(double percent, int level) {
    return customPercent("stamina_regen", percent, level, "Toxic Endurance",
        "+" + format(percent) + "% stamina regeneration while playing as Seuchenweber");
  }

  private static SkillReward directDamage(double percent, int level) {
    return customPercent("direct_damage", percent, level, "Cosmic Impact",
        "+" + format(percent) + "% direct damage while playing as Seuchenweber");
  }

  private static SkillReward dotDamage(double percent, int level) {
    return customPercent("dot_damage", percent, level, "Necrotoxic Potency",
        "+" + format(percent) + "% Necrotoxin damage over time");
  }

  private static SkillReward criticalDamage(double percent, int level) {
    return customPercent("crit_damage", percent, level, "Plague Execution",
        "+" + format(percent) + "% critical damage while playing as Seuchenweber");
  }

  private static SkillReward nativePercent(String kind, SkillRewardType type, double percent,
      int level, String name, String description) {
    return localized(kind, new SkillReward(id(kind, percent, level), type, percent / 100.0,
        name, description, CombatTarget.ALL).withIcon(SKILL_ICON));
  }

  private static SkillReward customPercent(String kind, double percent, int level,
      String name, String description) {
    return localized(kind, new SkillReward(id(kind, percent, level), SkillRewardType.ABILITY_MOD,
        percent, name, description, CombatTarget.ALL, SKILL_ID, "seuchenweber_internal").withIcon(SKILL_ICON));
  }

  private static String id(String kind, double percent, int level) {
    return "seuchenweber_" + kind + "_" + format(percent).replace('.', '_') + "_l" + level;
  }

  private static SkillReward localized(String kind, SkillReward reward) {
    return reward.withDisplayKeys("seuchenweber.tree." + kind + ".name",
        "seuchenweber.tree." + kind + ".description");
  }

  private static SkillReward abilityReward(TreeUnlock unlock) {
    String kind = unlock.passive() ? "Passive" : "Active";
    return SkillReward.forAbility(unlock.abilityId(), SkillRewardType.ABILITY_UNLOCK, 0.0,
        unlock.displayName(), kind + " · Unlock at Seuchenweber level " + unlock.level() + ".",
        unlock.abilityId()).withIcon(iconFor(unlock.abilityId())).withDisplayKeys(
            "seuchenweber.tree." + unlock.abilityId() + ".name",
            "seuchenweber.tree." + unlock.abilityId() + ".description");
  }

  private static String iconFor(String abilityId) {
    return switch (abilityId) {
      case "seuchenweber_seal_of_decay" -> "Seuchenweber_Skill_Seal_Of_Decay";
      case "seuchenweber_astral_rift" -> "Seuchenweber_Skill_Astral_Rift";
      case "seuchenweber_chronoblight" -> "Seuchenweber_Skill_Chronoblight";
      case "seuchenweber_passive_necrotoxic_mastery" -> "Seuchenweber_Skill_Necrotoxic_Mastery";
      case "seuchenweber_passive_astral_echo" -> "Seuchenweber_Skill_Astral_Echo";
      case "seuchenweber_passive_soul_diagnosis" -> "Seuchenweber_Skill_Soul_Diagnosis";
      case "seuchenweber_passive_relic_attunement" -> "Seuchenweber_Skill_Relic_Attunement";
      default -> SKILL_ICON;
    };
  }

  private static String format(double value) {
    return value == Math.rint(value) ? Integer.toString((int) value) : Double.toString(value);
  }

  record TreeUnlock(int level, String abilityId, String displayName, boolean passive) { }
  private record TreeTier(int choicesRequired, List<SkillReward> choices) { }
}
