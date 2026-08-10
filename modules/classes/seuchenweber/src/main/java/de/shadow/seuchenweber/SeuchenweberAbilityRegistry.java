package de.shadow.seuchenweber;

import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the three active effects from the single central contract source. */
final class SeuchenweberAbilityRegistry {
  private SeuchenweberAbilityRegistry() { }

  static Map<String, AbilityEffect> createEffects() {
    Map<String, AbilityEffect> effects = new LinkedHashMap<>();
    for (SeuchenweberAbilityContracts.Contract contract : SeuchenweberAbilityContracts.contracts()) {
      effects.put(contract.effectId(), new ContractBoundEffect(contract));
    }
    return Map.copyOf(effects);
  }

  static Map<String, AbilityEffect> createRuntimeEffects(NekrotoxinDamageSystem damageSystem, long toxinDurationMs,
      AstralRiftPulseSystem riftPulseSystem, long riftDurationMs) {
    Map<String, AbilityEffect> effects = new LinkedHashMap<>(createEffects());
    effects.put(SealOfDecayAbility.EFFECT_ID, new SealOfDecayAbility(damageSystem, toxinDurationMs));
    effects.put(AstralRiftAbility.EFFECT_ID, new AstralRiftAbility(riftPulseSystem, riftDurationMs));
    effects.put(ChronoblightAbility.EFFECT_ID, new ChronoblightAbility(damageSystem, toxinDurationMs));
    return Map.copyOf(effects);
  }

  private record ContractBoundEffect(SeuchenweberAbilityContracts.Contract contract) implements AbilityEffect {
    @Override public ParamSpec getParamSpec() {
      return contract.paramSpec();
    }

    @Override public AbilityResult execute(CasterContext context, AbilityDefinition ability) {
      return AbilityResult.conditionFailed("Seuchenweber runtime integration is not active");
    }
  }
}
