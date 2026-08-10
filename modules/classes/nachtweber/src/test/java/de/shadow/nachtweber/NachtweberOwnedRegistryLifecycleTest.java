package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import de.shadow.endlesselite.core.OwnedRegistryLifecycle;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class NachtweberOwnedRegistryLifecycleTest {
  @Test void registersAllEffectsAndUnregistersOnlyExactOwnedInstances() {
    var registry = new FakeRegistry();
    Map<String, AbilityEffect> effects = effects();
    var lifecycle = new OwnedRegistryLifecycle<>(registry, effects);

    assertTrue(lifecycle.start());
    assertEquals(effects, registry.values);
    assertFalse(lifecycle.start());

    lifecycle.shutdown();
    assertTrue(registry.values.isEmpty());
    assertEquals(3, registry.exactUnregisterCalls);
    lifecycle.shutdown();
    assertEquals(3, registry.exactUnregisterCalls);
  }

  @Test void collisionCompensatesEveryAttemptWithoutRemovingForeignOwner() {
    var registry = new FakeRegistry();
    AbilityEffect foreign = effects().get(BlackThreadAbilityContracts.EFFECT_ID);
    registry.values.put(ShadowSwingAbilityContracts.EFFECT_ID, foreign);
    Map<String, AbilityEffect> requested = effects();
    var lifecycle = new OwnedRegistryLifecycle<>(registry, requested);

    assertFalse(lifecycle.start());
    assertEquals(Map.of(ShadowSwingAbilityContracts.EFFECT_ID, foreign), registry.values);
    assertSame(foreign, registry.values.get(ShadowSwingAbilityContracts.EFFECT_ID));
    assertEquals(registry.registerCalls, registry.exactUnregisterCalls);
  }

  private static Map<String, AbilityEffect> effects() {
    BlackThreadService black = new BlackThreadService(new EntanglementLedger(5, 3),
        new BlackThreadRules(12, 2, 8_000, 3_000, 1_200), ControlProfile.defaults());
    HuntingCocoonService cocoon = new HuntingCocoonService(new EntanglementLedger(5, 3),
        new VenomLedger(4, 1_000), new HuntingCocoonRules(8, 3, 2, 8_000, 7_000));
    ShadowSwingService swing = new ShadowSwingService(new ShadowSwingRules(18, 2, 12, 4, 5_000));
    return new LinkedHashMap<>(NachtweberAbilityRegistry.createCompleteRuntimeEffects(
        black, swing, cocoon, VenomImmunityResolver.none()));
  }

  private static final class FakeRegistry implements OwnedRegistryLifecycle.Registry<AbilityEffect> {
    final Map<String, AbilityEffect> values = new LinkedHashMap<>();
    int registerCalls;
    int exactUnregisterCalls;

    @Override public boolean registerIfAbsent(String id, AbilityEffect effect) {
      registerCalls++;
      return values.putIfAbsent(id, effect) == null;
    }

    @Override public boolean unregister(String id, AbilityEffect expected) {
      exactUnregisterCalls++;
      return values.remove(id, expected);
    }
  }
}
