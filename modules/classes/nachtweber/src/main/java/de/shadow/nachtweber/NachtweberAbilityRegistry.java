package de.shadow.nachtweber;

import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.LinkedHashMap;
import java.util.Map;

final class NachtweberAbilityRegistry {
  private NachtweberAbilityRegistry() { }
  static Map<String,AbilityEffect> createRuntimeEffects(BlackThreadService service) {
    return Map.of(BlackThreadAbilityContracts.EFFECT_ID,new BlackThreadAbility(service,System::currentTimeMillis));
  }
  static Map<String,AbilityEffect> createRuntimeEffects(BlackThreadService blackThread,HuntingCocoonService cocoon,VenomImmunityResolver immunity) {
    return Map.of(
        BlackThreadAbilityContracts.EFFECT_ID,new BlackThreadAbility(blackThread,System::currentTimeMillis),
        HuntingCocoonAbilityContracts.EFFECT_ID,new HuntingCocoonAbility(cocoon,immunity,System::currentTimeMillis));
  }
  static Map<String,AbilityEffect> createCompleteRuntimeEffects(BlackThreadService blackThread,ShadowSwingService shadowSwing,HuntingCocoonService cocoon,VenomImmunityResolver immunity) {
    return Map.of(
        BlackThreadAbilityContracts.EFFECT_ID,new BlackThreadAbility(blackThread,System::currentTimeMillis),
        ShadowSwingAbilityContracts.EFFECT_ID,new ShadowSwingAbility(shadowSwing,System::currentTimeMillis),
        HuntingCocoonAbilityContracts.EFFECT_ID,new HuntingCocoonAbility(cocoon,immunity,System::currentTimeMillis));
  }

  static Map<String, AbilityEffect> createStoreBoundEffects(
      NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator,
      VenomImmunityResolver immunity,
      OwnerEntityBindingPort ownerBindings) {
    Map<String, AbilityEffect> effects = new LinkedHashMap<>();
    effects.put(BlackThreadAbilityContracts.EFFECT_ID,
        new StoreBoundBlackThreadAbility(coordinator, System::currentTimeMillis));
    effects.put(ShadowSwingAbilityContracts.EFFECT_ID,
        new StoreBoundShadowSwingAbility(coordinator, System::currentTimeMillis));
    effects.put(HuntingCocoonAbilityContracts.EFFECT_ID,
        new StoreBoundHuntingCocoonAbility(
            coordinator, immunity, System::currentTimeMillis, ownerBindings));
    return Map.copyOf(effects);
  }
}
