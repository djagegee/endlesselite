package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class HuntingCocoonServiceTest {
  private static final UUID OWNER_A = UUID.fromString("20000000-0000-0000-0000-000000000001");
  private static final UUID OWNER_B = UUID.fromString("20000000-0000-0000-0000-000000000002");

  @Test void bundledConfigUsesSecondsBlocksAndCountsForHuntingCocoon() throws Exception {
    try (var stream = getClass().getClassLoader().getResourceAsStream("config/nachtweber.json")) {
      assertNotNull(stream);
      var root = JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();
      assertEquals(9, root.get("schemaVersion").getAsInt());
      var config = root.getAsJsonObject("abilities").getAsJsonObject("huntingCocoon");
      assertEquals(8.0, config.get("rangeBlocks").getAsDouble());
      assertEquals(7.0, config.get("cooldownSeconds").getAsDouble());
      assertEquals(3, config.get("requiredEntanglementStacks").getAsInt());
      assertEquals(2, config.get("venomStacks").getAsInt());
      assertEquals(8.0, config.get("venomDurationSeconds").getAsDouble());
    }
  }

  @Test void registryAddsCocoonEffectWithVerifiedContractAndFailsClosedWithoutCaster() {
    BlackThreadService blackThread = new BlackThreadService(new EntanglementLedger(5, 3),
        new BlackThreadRules(12.0, 2, 8_000, 3_000, 1_200), ControlProfile.defaults());
    HuntingCocoonService cocoon = new HuntingCocoonService(new EntanglementLedger(5, 3),
        new VenomLedger(4, 1_000), new HuntingCocoonRules(8.0, 3, 2, 8_000, 7_000));
    Map<String, AbilityEffect> effects = NachtweberAbilityRegistry.createRuntimeEffects(
        blackThread, cocoon, VenomImmunityResolver.none());
    assertEquals(Set.of("NACHTWEBER_BLACK_THREAD", "NACHTWEBER_HUNTING_COCOON"), effects.keySet());
    AbilityEffect effect = effects.get("NACHTWEBER_HUNTING_COCOON");
    assertEquals(Set.of("range", "requiredEntanglementStacks", "venomStacks", "venomDurationMs"),
        effect.getParamSpec().entries().stream().map(entry -> entry.key()).collect(java.util.stream.Collectors.toSet()));
    AbilityDefinition definition = new AbilityDefinition("nachtweber_hunting_cocoon", 7_000,
        "NACHTWEBER_HUNTING_COCOON", "Jagdkokon", "", "", 0,
        Map.of("range", 8.0, "requiredEntanglementStacks", 3.0, "venomStacks", 2.0, "venomDurationMs", 8_000.0));
    AbilityResult result = effect.execute(null, definition);
    assertFalse(result.isSuccess());
    assertEquals("Requires a player caster", result.getErrorDetail());
  }

  @Test void rangeAndCooldownRejectionsPreserveReplenishedOwnerState() {
    EntanglementLedger entanglement = new EntanglementLedger(5, 3);
    VenomLedger venom = new VenomLedger(4, 1_000);
    entanglement.apply(42, OWNER_A, 3, 10_000, 0);
    HuntingCocoonService service = new HuntingCocoonService(entanglement, venom,
        new HuntingCocoonRules(8.0, 3, 2, 8_000, 7_000));

    assertEquals(HuntingCocoonService.CastStatus.REJECTED_OUT_OF_RANGE, service.cast(
        new HuntingCocoonService.CastRequest(OWNER_A, 42, 8.01, 0, true, VenomDamageCause.DIRECT_HIT, () -> false)).status());
    assertEquals(3, entanglement.stacks(42, OWNER_A, 0));
    assertEquals(HuntingCocoonService.CastStatus.APPLIED, service.cast(
        new HuntingCocoonService.CastRequest(OWNER_A, 42, 8.0, 0, true, VenomDamageCause.DIRECT_HIT, () -> false)).status());

    entanglement.apply(42, OWNER_A, 3, 10_000, 1);
    HuntingCocoonService.CastOutcome cooldown = service.cast(new HuntingCocoonService.CastRequest(
        OWNER_A, 42, 1.0, 1, true, VenomDamageCause.DIRECT_HIT, () -> false));
    assertEquals(HuntingCocoonService.CastStatus.REJECTED_COOLDOWN, cooldown.status());
    assertEquals(7_000, cooldown.nextReadyAtMs());
    assertEquals(3, entanglement.stacks(42, OWNER_A, 1));
    assertEquals(2, venom.consumeOneDueTick(42, OWNER_A, 1_000).stacks());
  }

  @Test void extremeServerClockSaturatesVenomExpiryTickAndCooldown() {
    EntanglementLedger entanglement = new EntanglementLedger(5, 3);
    VenomLedger venom = new VenomLedger(4, 1_000);
    long now = Long.MAX_VALUE - 10;
    entanglement.apply(42, OWNER_A, 3, 10_000, now);
    HuntingCocoonService service = new HuntingCocoonService(entanglement, venom,
        new HuntingCocoonRules(8.0, 3, 2, 8_000, 7_000));
    HuntingCocoonService.CastOutcome outcome = service.cast(new HuntingCocoonService.CastRequest(
        OWNER_A, 42, 1.0, now, true, VenomDamageCause.DIRECT_HIT, () -> false));
    assertEquals(HuntingCocoonService.CastStatus.APPLIED, outcome.status());
    assertEquals(Long.MAX_VALUE, outcome.nextReadyAtMs());
    assertTrue(venom.hasState(42, OWNER_A, now + 1));
  }

  @Test void rejectedCastsDoNotConsumeEntanglementApplyVenomOrStartCooldown() {
    EntanglementLedger entanglement = new EntanglementLedger(5, 3);
    VenomLedger venom = new VenomLedger(4, 1_000);
    entanglement.apply(42, OWNER_A, 3, 10_000, 0);
    entanglement.apply(42, OWNER_B, 2, 10_000, 0);
    HuntingCocoonService service = new HuntingCocoonService(entanglement, venom,
        new HuntingCocoonRules(8.0, 3, 2, 8_000, 7_000));

    assertEquals(HuntingCocoonService.CastStatus.REJECTED_NOT_SERVER, service.cast(
        new HuntingCocoonService.CastRequest(OWNER_A, 42, 2.0, 0, false, VenomDamageCause.DIRECT_HIT, () -> false)).status());
    assertEquals(HuntingCocoonService.CastStatus.REJECTED_IMMUNE, service.cast(
        new HuntingCocoonService.CastRequest(OWNER_A, 42, 2.0, 0, true, VenomDamageCause.DIRECT_HIT, () -> true)).status());
    assertEquals(HuntingCocoonService.CastStatus.REJECTED_RECURSIVE_CAUSE, service.cast(
        new HuntingCocoonService.CastRequest(OWNER_A, 42, 2.0, 0, true, VenomDamageCause.REFLECTION, () -> false)).status());
    assertEquals(HuntingCocoonService.CastStatus.REJECTED_INSUFFICIENT_ENTANGLEMENT, service.cast(
        new HuntingCocoonService.CastRequest(OWNER_B, 42, 2.0, 0, true, VenomDamageCause.DIRECT_HIT, () -> false)).status());
    assertEquals(3, entanglement.stacks(42, OWNER_A, 1));
    assertEquals(2, entanglement.stacks(42, OWNER_B, 1));
    assertFalse(venom.hasState(42, OWNER_A, 1));
    assertFalse(venom.hasState(42, OWNER_B, 1));

    assertEquals(HuntingCocoonService.CastStatus.APPLIED, service.cast(
        new HuntingCocoonService.CastRequest(OWNER_A, 42, 2.0, 1, true, VenomDamageCause.DIRECT_HIT, () -> false)).status());
  }

  @Test void validCastConsumesOnlyOwnersEntanglementAndCreatesOwnerVenom() {
    EntanglementLedger entanglement = new EntanglementLedger(5, 3);
    VenomLedger venom = new VenomLedger(4, 1_000);
    entanglement.apply(42, OWNER_A, 4, 10_000, 0);
    entanglement.apply(42, OWNER_B, 3, 10_000, 0);
    HuntingCocoonService service = new HuntingCocoonService(entanglement, venom,
        new HuntingCocoonRules(8.0, 3, 2, 8_000, 7_000));

    HuntingCocoonService.CastOutcome outcome = service.cast(new HuntingCocoonService.CastRequest(
        OWNER_A, 42, 6.0, 0, true, VenomDamageCause.DIRECT_HIT, () -> false));

    assertEquals(HuntingCocoonService.CastStatus.APPLIED, outcome.status());
    assertEquals(4, outcome.consumedEntanglementStacks());
    assertEquals(2, outcome.venomStacks());
    assertEquals(2_000, outcome.immobilizeDurationMs());
    assertEquals(7_000, outcome.nextReadyAtMs());
    assertEquals(0, entanglement.stacks(42, OWNER_A, 1));
    assertEquals(3, entanglement.stacks(42, OWNER_B, 1));
    assertEquals(2, venom.consumeOneDueTick(42, OWNER_A, 1_000).stacks());
    assertTrue(venom.consumeOneDueTick(42, OWNER_B, 1_000).isEmpty());
  }
}
