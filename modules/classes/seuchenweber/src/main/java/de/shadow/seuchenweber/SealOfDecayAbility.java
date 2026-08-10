package de.shadow.seuchenweber;

import com.airijko.endlessleveling.systems.PlayerCombatSystem;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;

/** Server-authoritative active effect for the Seal of Decay ability. */
final class SealOfDecayAbility implements AbilityEffect {
  static final String EFFECT_ID = "SEUCHENWEBER_SEAL_OF_DECAY";
  private final NekrotoxinDamageSystem damageSystem;
  private final long toxinDurationMs;

  SealOfDecayAbility(NekrotoxinDamageSystem damageSystem, long toxinDurationMs) {
    this.damageSystem = damageSystem;
    this.toxinDurationMs = toxinDurationMs;
  }

  @Override public ParamSpec getParamSpec() {
    return SeuchenweberAbilityContracts.byEffectId(EFFECT_ID).paramSpec();
  }

  @Override public AbilityResult execute(CasterContext context, AbilityDefinition ability) {
    if (context == null || !context.isPlayer() || context.casterUuid() == null) {
      return AbilityResult.conditionFailed("Requires a player caster");
    }
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    if (store == null || caster == null || !caster.isValid() || caster.getStore() != store
        || damageSystem == null || toxinDurationMs <= 0L) {
      return AbilityResult.error("Seuchenweber runtime is unavailable");
    }
    double range = ability.getNumber("range", 0.0);
    Ref<EntityStore> target = SeuchenweberTargeting.closestVisibleHostile(store, caster, range);
    if (target == null) return AbilityResult.conditionFailed("No visible hostile target in range");
    int stacks = boundedPositiveInt(ability.getNumber("stacks", 0.0));
    if (stacks == 0) return AbilityResult.error("Invalid Nekrotoxin stack definition");
    damageSystem.apply(store, target, caster, context.casterUuid(), stacks, toxinDurationMs);
    float directDamage = (float) Math.max(0.0, ability.getNumber("directDamage", 0.0));
    directDamage = SeuchenweberTreeBonusResolver.applyPercent(directDamage,
        SeuchenweberTreeBonusResolver.claimedPercent(
            store, caster, SeuchenweberTreeBonusResolver.DIRECT_DAMAGE));
    if (directDamage > 0.0f) {
      Damage directHit = PlayerCombatSystem.createAbilityDamage(
          caster, directDamage, "seuchenweber_seal_of_decay");
      directHit.setDamageCauseIndex(SeuchenweberDamageCause.requireIndex());
      DamageSystems.executeDamage(target, store, directHit);
    }
    return AbilityResult.success();
  }

  private static int boundedPositiveInt(double value) {
    if (!Double.isFinite(value) || value < 1.0) return 0;
    return (int) Math.min(16, Math.floor(value));
  }
}
