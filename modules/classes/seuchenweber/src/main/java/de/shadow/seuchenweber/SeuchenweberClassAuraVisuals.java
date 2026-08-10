package de.shadow.seuchenweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.RemovalBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Synchronizes the damage-free class-selection aura. */
final class SeuchenweberClassAuraVisuals {
  static final String EFFECT_ID = "Seuchenweber_Class_Aura";

  enum Action { ADD, RESTART, REMOVE, NONE }

  private SeuchenweberClassAuraVisuals() { }

  static Action decide(boolean shouldEnable, boolean alreadyActive) {
    return decide(shouldEnable, alreadyActive, false);
  }

  static Action decide(boolean shouldEnable, boolean alreadyActive, boolean restartRequired) {
    if (shouldEnable && alreadyActive && restartRequired) return Action.RESTART;
    if (shouldEnable) return alreadyActive ? Action.NONE : Action.ADD;
    return alreadyActive ? Action.REMOVE : Action.NONE;
  }

  static boolean synchronize(Store<EntityStore> store, Ref<EntityStore> player, boolean shouldEnable) {
    return synchronize(store, player, shouldEnable, false);
  }

  static boolean synchronize(Store<EntityStore> store, Ref<EntityStore> player,
      boolean shouldEnable, boolean restartRequired) {
    if (store == null || player == null || !player.isValid() || player.getStore() != store) return false;
    EffectControllerComponent controller = store.getComponent(player, EffectControllerComponent.getComponentType());
    if (controller == null) return false;
    int effectIndex = EntityEffect.getAssetMap().getIndex(EFFECT_ID);
    if (effectIndex == Integer.MIN_VALUE) return false;
    EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectIndex);
    if (effect == null) return false;

    return switch (decide(shouldEnable, controller.hasEffect(effectIndex), restartRequired)) {
      case ADD -> controller.addInfiniteEffect(player, effectIndex, effect, store);
      case RESTART -> {
        controller.removeEffect(player, effectIndex, RemovalBehavior.COMPLETE, store);
        yield controller.addInfiniteEffect(player, effectIndex, effect, store);
      }
      case REMOVE -> {
        controller.removeEffect(player, effectIndex, RemovalBehavior.COMPLETE, store);
        yield true;
      }
      case NONE -> false;
    };
  }
}
