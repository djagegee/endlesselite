package de.shadow.seuchenweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import com.ziggfreed.mmoskilltree.skilltree.SkillReward;
import java.util.HashSet;
import java.util.Set;

/** Resolves only actually claimed Seuchenweber ABILITY_MOD rewards. */
final class SeuchenweberTreeBonusResolver {
  static final String DOT_DAMAGE = "dot_damage";
  static final String DIRECT_DAMAGE = "direct_damage";
  static final String CRIT_DAMAGE = "crit_damage";
  static final String STAMINA_REGEN = "stamina_regen";
  private static final double MAX_TOTAL_PERCENT = 100.0;

  private SeuchenweberTreeBonusResolver() { }

  static double claimedPercent(Store<EntityStore> store, Ref<EntityStore> player, String kind) {
    if (store == null || player == null || !player.isValid() || player.getStore() != store) return 0.0;
    SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, player);
    if (skills == null) return 0.0;
    return claimedPercent(new HashSet<>(skills.getClaimedRewardsByName(SeuchenweberMmoBridge.SKILL_ID)), kind);
  }

  static double claimedPercent(Set<String> claimedIds, String kind) {
    if (claimedIds == null || claimedIds.isEmpty() || kind == null || kind.isBlank()) return 0.0;
    String prefix = "seuchenweber_" + kind + "_";
    double total = 0.0;
    for (SkillReward reward : SeuchenweberMmoBridge.buildTree().stream()
        .flatMap(node -> node.getChoices().stream()).toList()) {
      if (reward.getId().startsWith(prefix) && claimedIds.contains(reward.getId())
          && Double.isFinite(reward.getValue()) && reward.getValue() > 0.0) {
        total += reward.getValue();
      }
    }
    return Math.min(MAX_TOTAL_PERCENT, total);
  }

  static float applyPercent(float amount, double percent) {
    if (!Float.isFinite(amount) || amount <= 0.0f || !Double.isFinite(percent) || percent < 0.0) return 0.0f;
    double scaled = amount * (1.0 + Math.min(MAX_TOTAL_PERCENT, percent) / 100.0);
    return Double.isFinite(scaled) && scaled <= Float.MAX_VALUE ? (float) scaled : 0.0f;
  }
}
