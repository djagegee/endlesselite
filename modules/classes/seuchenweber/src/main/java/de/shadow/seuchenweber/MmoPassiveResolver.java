package de.shadow.seuchenweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;

/** Confirmed MMOSkillTree 1.5.2 unlock lookup; fails closed when player data is unavailable. */
final class MmoPassiveResolver {
  static final String NECROTOXIC_MASTERY_ID = "seuchenweber_passive_necrotoxic_mastery";
  static final String ASTRAL_ECHO_ID = "seuchenweber_passive_astral_echo";
  static final String SOUL_DIAGNOSIS_ID = "seuchenweber_passive_soul_diagnosis";
  static final String RELIC_ATTUNEMENT_ID = "seuchenweber_passive_relic_attunement";

  private MmoPassiveResolver() { }

  static boolean hasUnlocked(Store<EntityStore> store, Ref<EntityStore> owner, String abilityId) {
    if (store == null || owner == null || abilityId == null || abilityId.isBlank() || !owner.isValid()
        || owner.getStore() != store) return false;
    SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, owner);
    return skills != null && skills.hasUnlockedAbility(abilityId);
  }
}
