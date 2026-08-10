package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SeuchenweberClassAuraContractTest {
  @Test void auraUsesNativePoisonParticlesWithoutDamageOrDebuff() throws Exception {
    String resource = "/Server/Entity/Effects/Status/Seuchenweber_Class_Aura.json";
    try (InputStream stream = getClass().getResourceAsStream(resource)) {
      assertNotNull(stream, resource);
      String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(json.contains("\"SystemId\": \"Effect_Poison\""));
      assertTrue(json.contains("\"Debuff\": false"));
      assertTrue(json.contains("\"Infinite\": true"));
      assertFalse(json.contains("DamageCalculator"));
      assertFalse(json.contains("BaseDamage"));
      assertFalse(json.contains("StatusEffectIcon"));
    }
  }

  @Test void auraLifecycleIsIdempotent() {
    assertTrue(SeuchenweberClassAuraVisuals.decide(true, false)
        == SeuchenweberClassAuraVisuals.Action.ADD);
    assertTrue(SeuchenweberClassAuraVisuals.decide(true, true)
        == SeuchenweberClassAuraVisuals.Action.NONE);
    assertTrue(SeuchenweberClassAuraVisuals.decide(false, true)
        == SeuchenweberClassAuraVisuals.Action.REMOVE);
    assertTrue(SeuchenweberClassAuraVisuals.decide(false, false)
        == SeuchenweberClassAuraVisuals.Action.NONE);
    assertTrue(SeuchenweberClassAuraVisuals.decide(true, true, true)
        == SeuchenweberClassAuraVisuals.Action.RESTART);
    assertTrue(SeuchenweberClassAuraVisuals.decide(true, true, false)
        == SeuchenweberClassAuraVisuals.Action.NONE);
  }
}
