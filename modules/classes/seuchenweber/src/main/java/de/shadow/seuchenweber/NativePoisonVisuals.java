package de.shadow.seuchenweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Applies Cave-Spider-style poison presentation without introducing a second damage source. */
final class NativePoisonVisuals {
  private NativePoisonVisuals() { }

  static boolean synchronize(Store<EntityStore> store, Ref<EntityStore> target,
      int stacks, long remainingDurationMs) {
    if (store == null || target == null || !target.isValid() || target.getStore() != store) return false;
    EffectControllerComponent controller = store.getComponent(
        target, EffectControllerComponent.getComponentType());
    if (controller == null) return false;

    NativePoisonVisualTier desired = NativePoisonVisualTier.forStacks(stacks);
    for (NativePoisonVisualTier tier : NativePoisonVisualTier.values()) {
      if (tier == NativePoisonVisualTier.NONE || tier == desired) continue;
      int index = EntityEffect.getAssetMap().getIndex(tier.effectId());
      if (index != Integer.MIN_VALUE && controller.hasEffect(index)) {
        controller.removeEffect(target, index, store);
      }
    }
    if (desired == NativePoisonVisualTier.NONE || remainingDurationMs <= 0L) {
      if (desired == NativePoisonVisualTier.NONE) removeDesiredIfPresent(controller, store, target);
      return true;
    }

    EntityEffect effect = EntityEffect.getAssetMap().getAsset(desired.effectId());
    if (effect == null) return false;
    int index = EntityEffect.getAssetMap().getIndex(desired.effectId());
    if (index == Integer.MIN_VALUE) return false;
    float durationSeconds = Math.max(0.05f, remainingDurationMs / 1_000.0f);
    return controller.addEffect(target, index, effect, durationSeconds, OverlapBehavior.OVERWRITE, store);
  }

  private static void removeDesiredIfPresent(EffectControllerComponent controller,
      Store<EntityStore> store, Ref<EntityStore> target) {
    for (NativePoisonVisualTier tier : NativePoisonVisualTier.values()) {
      if (tier == NativePoisonVisualTier.NONE) continue;
      int index = EntityEffect.getAssetMap().getIndex(tier.effectId());
      if (index != Integer.MIN_VALUE && controller.hasEffect(index)) {
        controller.removeEffect(target, index, store);
      }
    }
  }
}
