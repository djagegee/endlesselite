package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SeuchenweberAbilityRegistryTest {
  @Test void buildsOneEffectForEveryCentralActiveContract() {
    Map<String, AbilityEffect> effects = SeuchenweberAbilityRegistry.createEffects();
    assertEquals(SeuchenweberAbilityContracts.effectIds(), effects.keySet());
    assertTrue(effects.entrySet().stream().allMatch(entry -> entry.getValue().getParamSpec()
        == SeuchenweberAbilityContracts.byEffectId(entry.getKey()).paramSpec()));
  }
  @Test void bindsSealOfDecayToItsConcreteRuntimeEffectOnlyWhenRuntimeDependenciesExist() {
    Map<String, AbilityEffect> effects = SeuchenweberAbilityRegistry.createRuntimeEffects(null, 10_000L, null, 5_000L);
    assertTrue(effects.get("SEUCHENWEBER_SEAL_OF_DECAY") instanceof SealOfDecayAbility);
    assertEquals(SeuchenweberAbilityContracts.effectIds(), effects.keySet());
  }
  @Test void bindsAstralRiftToItsConcreteRuntimeEffect() {
    Map<String, AbilityEffect> effects = SeuchenweberAbilityRegistry.createRuntimeEffects(null, 10_000L, null, 5_000L);
    assertTrue(effects.get("SEUCHENWEBER_ASTRAL_RIFT") instanceof AstralRiftAbility);
  }
  @Test void bindsChronoblightToItsConcreteRuntimeEffect() {
    Map<String, AbilityEffect> effects = SeuchenweberAbilityRegistry.createRuntimeEffects(null, 10_000L, null, 5_000L);
    assertTrue(effects.get("SEUCHENWEBER_CHRONOBLIGHT") instanceof ChronoblightAbility);
  }

  @Test void chronoblightContractOnlyPublishesImplementedParameters() {
    var keys = SeuchenweberAbilityContracts.byEffectId("SEUCHENWEBER_CHRONOBLIGHT").paramKeys();
    assertEquals(java.util.Set.of("range", "stacks", "fullMarkStunMs"), keys);
    assertTrue(ChronoblightAbility.shouldApplyFullMarkStun(true, 750L));
    assertTrue(!ChronoblightAbility.shouldApplyFullMarkStun(false, 750L));
    assertTrue(!ChronoblightAbility.shouldApplyFullMarkStun(true, 0L));
  }
}
