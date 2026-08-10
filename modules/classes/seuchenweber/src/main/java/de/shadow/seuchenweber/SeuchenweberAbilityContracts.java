package de.shadow.seuchenweber;

import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Central source of truth for the three MMOSkillTree active-effect contracts. */
final class SeuchenweberAbilityContracts {
  private static final List<Contract> CONTRACTS = List.of(
      contract("SEUCHENWEBER_SEAL_OF_DECAY", ParamSpec.of(
          required("range", 22.0, "Maximum hostile-target range in blocks"),
          required("stacks", 2.0, "Owner-isolated Nekrotoxin stacks to apply"),
          required("directDamage", 18.0, "Small direct impact before periodic damage"))),
      contract("SEUCHENWEBER_ASTRAL_RIFT", ParamSpec.of(
          required("teleportRange", 12.0, "Maximum line-of-sight displacement in blocks"),
          required("riftDurationMs", 5000.0, "Finite server-authoritative rift duration"))),
      contract("SEUCHENWEBER_CHRONOBLIGHT", ParamSpec.of(
          required("range", 20.0, "Maximum hostile-target range in blocks"),
          required("stacks", 2.0, "Additional owner-isolated stacks to apply"),
          required("fullMarkStunMs", 750.0, "Optional Perfect Utils control duration at cap"))));

  private static final Map<String, Contract> BY_EFFECT_ID = CONTRACTS.stream()
      .collect(java.util.stream.Collectors.toUnmodifiableMap(Contract::effectId, contract -> contract));

  private SeuchenweberAbilityContracts() { }

  static List<Contract> contracts() {
    return CONTRACTS;
  }

  static Set<String> effectIds() {
    return BY_EFFECT_ID.keySet();
  }

  static Contract byEffectId(String effectId) {
    Contract contract = BY_EFFECT_ID.get(effectId);
    if (contract == null) throw new IllegalArgumentException("Unknown Seuchenweber effect: " + effectId);
    return contract;
  }

  private static Contract contract(String effectId, ParamSpec paramSpec) {
    return new Contract(effectId, paramSpec, paramSpec.entries().stream().map(ParamSpec.ParamEntry::key)
        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
  }

  private static ParamSpec.ParamEntry required(String key, double defaultValue, String description) {
    return ParamSpec.ParamEntry.required(key, ParamSpec.ParamType.NUMBER, defaultValue, description);
  }

  record Contract(String effectId, ParamSpec paramSpec, Set<String> paramKeys) { }
}
