package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NativePoisonVisualContractTest {
  @Test void mapsNekrotoxinStacksToNativePoisonTiersWithoutDowngradingAtCap() {
    assertEquals(NativePoisonVisualTier.NONE, NativePoisonVisualTier.forStacks(0));
    assertEquals(NativePoisonVisualTier.POISON_I, NativePoisonVisualTier.forStacks(1));
    assertEquals(NativePoisonVisualTier.POISON_I, NativePoisonVisualTier.forStacks(2));
    assertEquals(NativePoisonVisualTier.POISON_II, NativePoisonVisualTier.forStacks(3));
    assertEquals(NativePoisonVisualTier.POISON_II, NativePoisonVisualTier.forStacks(4));
    assertEquals(NativePoisonVisualTier.POISON_III, NativePoisonVisualTier.forStacks(5));
    assertEquals(NativePoisonVisualTier.POISON_III, NativePoisonVisualTier.forStacks(99));
  }

  @Test void usesStableVisualOnlyEffectIds() {
    assertEquals("Seuchenweber_Poison_Visual_T1", NativePoisonVisualTier.POISON_I.effectId());
    assertEquals("Seuchenweber_Poison_Visual_T2", NativePoisonVisualTier.POISON_II.effectId());
    assertEquals("Seuchenweber_Poison_Visual_T3", NativePoisonVisualTier.POISON_III.effectId());
  }

  @Test void choosesStrongestOwnerMarkForOneSharedTarget() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000L);
    UUID weakerOwner = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    UUID strongerOwner = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    ledger.apply(42, weakerOwner, 2, 10_000L, 0L);
    ledger.apply(42, strongerOwner, 4, 10_000L, 0L);

    assertEquals(4, ledger.maximumActiveStacks(42));
  }

  @Test void visualAssetsReuseNativePoisonPresentationButCannotDealDamage() throws Exception {
    for (NativePoisonVisualTier tier : new NativePoisonVisualTier[] {
        NativePoisonVisualTier.POISON_I,
        NativePoisonVisualTier.POISON_II,
        NativePoisonVisualTier.POISON_III}) {
      String resource = "/Server/Entity/Effects/Status/" + tier.effectId() + ".json";
      try (InputStream stream = getClass().getResourceAsStream(resource)) {
        assertNotNull(stream, resource);
        String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(json.contains("Effect_Poison"), resource);
        assertTrue(json.contains("\"EntityAnimationId\": \"Hurt\""), resource);
        assertTrue(json.contains("UI/StatusEffects/Poison.png"), resource);
        assertFalse(json.contains("DamageCalculator"), resource + " must remain visual-only");
        assertFalse(json.contains("BaseDamage"), resource + " must remain visual-only");
      }
    }
  }
}
