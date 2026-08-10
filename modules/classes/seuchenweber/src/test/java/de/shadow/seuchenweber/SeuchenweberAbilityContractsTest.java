package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class SeuchenweberAbilityContractsTest {
  @Test void exposesExactlyThreeRuntimeEffectIdsForTheThreeActiveAbilities() {
    assertEquals(Set.of(
        "SEUCHENWEBER_SEAL_OF_DECAY",
        "SEUCHENWEBER_ASTRAL_RIFT",
        "SEUCHENWEBER_CHRONOBLIGHT"), SeuchenweberAbilityContracts.effectIds());
  }

  @Test void everyActiveAbilityHasARequiredNonEmptyMmoParameterContract() {
    for (SeuchenweberAbilityContracts.Contract contract : SeuchenweberAbilityContracts.contracts()) {
      assertFalse(contract.paramSpec().entries().isEmpty(), contract.effectId());
      assertTrue(contract.paramSpec().entries().stream().allMatch(entry -> entry.required()), contract.effectId());
    }
  }

  @Test void contractsExposeTheMechanicsNeededByTheCentralBalanceConfig() {
    assertTrue(SeuchenweberAbilityContracts.byEffectId("SEUCHENWEBER_SEAL_OF_DECAY").paramKeys().containsAll(Set.of("range", "stacks", "directDamage")));
    assertTrue(SeuchenweberAbilityContracts.byEffectId("SEUCHENWEBER_ASTRAL_RIFT").paramKeys().containsAll(Set.of("teleportRange", "riftDurationMs")));
    assertTrue(SeuchenweberAbilityContracts.byEffectId("SEUCHENWEBER_CHRONOBLIGHT").paramKeys().containsAll(Set.of("range", "stacks", "fullMarkStunMs")));
  }
}
