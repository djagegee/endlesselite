package de.shadow.seuchenweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;

import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;

/** Applies Chronoblight's direct hit and owner-isolated Nekrotoxin mark. */
final class ChronoblightAbility implements AbilityEffect {
  static final String EFFECT_ID = "SEUCHENWEBER_CHRONOBLIGHT";
  private final NekrotoxinDamageSystem damageSystem;
  private final long toxinDurationMs;

  ChronoblightAbility(NekrotoxinDamageSystem damageSystem, long toxinDurationMs) {
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
      return AbilityResult.error("Chronoblight runtime is unavailable");
    }
    Ref<EntityStore> target = SeuchenweberTargeting.closestVisibleHostile(store, caster, ability.getNumber("range", 0.0));
    if (target == null) return AbilityResult.conditionFailed("No visible hostile target in range");
    int stacks = boundedPositiveInt(ability.getNumber("stacks", 0.0));
    if (stacks == 0) return AbilityResult.error("Invalid Chronoblight stack definition");
    int resultingStacks = damageSystem.apply(
        store, target, caster, context.casterUuid(), stacks, toxinDurationMs);
    long fullMarkStunMs = Math.max(0L, (long) ability.getNumber("fullMarkStunMs", 0.0));
    if (shouldApplyFullMarkStun(damageSystem.isFullMark(resultingStacks), fullMarkStunMs)) {
      PerfectUtilsControl.applyStun(store, target, fullMarkStunMs, caster);
    }
    return AbilityResult.success();
  }

  static boolean shouldApplyFullMarkStun(boolean fullMark, long durationMs) {
    return fullMark && durationMs > 0L;
  }

  private static int boundedPositiveInt(double value) {
    if (!Double.isFinite(value) || value < 1.0) return 0;
    return (int) Math.min(16, Math.floor(value));
  }
}
