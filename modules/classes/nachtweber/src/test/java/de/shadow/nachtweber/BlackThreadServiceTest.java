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

final class BlackThreadServiceTest {
  private static final UUID OWNER = UUID.fromString("10000000-0000-0000-0000-000000000001");

  @Test void extremeServerClockSaturatesExpiryAndCooldownInsteadOfOverflowing() {
    EntanglementLedger ledger = new EntanglementLedger(5, 3);
    BlackThreadService service = new BlackThreadService(ledger,
        new BlackThreadRules(12.0, 3, 8_000, 3_000, 1_200), ControlProfile.defaults());
    long now = Long.MAX_VALUE - 10;
    BlackThreadService.CastOutcome outcome = service.cast(new BlackThreadService.CastRequest(
        OWNER, 42, 1.0, ControlProfile.TargetKind.NORMAL, now, true));
    assertEquals(BlackThreadService.CastStatus.APPLIED, outcome.status());
    assertTrue(outcome.bound());
    assertEquals(Long.MAX_VALUE, outcome.nextReadyAtMs());
    assertEquals(3, ledger.stacks(42, OWNER, now + 1));
  }

  @Test void bundledConfigUsesSecondsBlocksAndCountsForBlackThread() throws Exception {
    try (var stream = getClass().getClassLoader().getResourceAsStream("config/nachtweber.json")) {
      assertNotNull(stream);
      var blackThread = JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))
          .getAsJsonObject().getAsJsonObject("abilities").getAsJsonObject("blackThread");
      assertEquals(12.0, blackThread.get("rangeBlocks").getAsDouble());
      assertEquals(3.0, blackThread.get("cooldownSeconds").getAsDouble());
      assertEquals(2, blackThread.get("stacksPerCast").getAsInt());
      assertEquals(1.2, blackThread.get("boundControlSeconds").getAsDouble());
    }
  }

  @Test void registryExposesOnlyVerifiedBlackThreadEffectAndFailsClosedWithoutCaster() {
    BlackThreadService service = new BlackThreadService(new EntanglementLedger(5, 3),
        new BlackThreadRules(12.0, 2, 8_000, 3_000, 1_200), ControlProfile.defaults());
    Map<String, AbilityEffect> effects = NachtweberAbilityRegistry.createRuntimeEffects(service);
    assertEquals(Set.of("NACHTWEBER_BLACK_THREAD"), effects.keySet());
    AbilityEffect effect = effects.get("NACHTWEBER_BLACK_THREAD");
    assertEquals(Set.of("range", "stacks", "stateDurationMs", "boundControlDurationMs"),
        effect.getParamSpec().entries().stream().map(entry -> entry.key()).collect(java.util.stream.Collectors.toSet()));
    AbilityDefinition definition = new AbilityDefinition("nachtweber_black_thread", 3_000,
        "NACHTWEBER_BLACK_THREAD", "Schwarzer Faden", "", "", 0,
        Map.of("range", 12.0, "stacks", 2.0, "stateDurationMs", 8_000.0, "boundControlDurationMs", 1_200.0));
    AbilityResult result = effect.execute(null, definition);
    assertFalse(result.isSuccess());
    assertEquals("Requires a player caster", result.getErrorDetail());
  }

  @Test void invalidOrUntrustedCastsFailClosedWithoutMutatingState() {
    UUID other = UUID.fromString("10000000-0000-0000-0000-000000000002");
    EntanglementLedger ledger = new EntanglementLedger(5, 3);
    BlackThreadService service = new BlackThreadService(ledger,
        new BlackThreadRules(12.0, 2, 8_000, 3_000, 1_200), ControlProfile.defaults());

    assertEquals(BlackThreadService.CastStatus.REJECTED_NOT_SERVER, service.cast(
        new BlackThreadService.CastRequest(OWNER, 42, 4.0, ControlProfile.TargetKind.NORMAL, 0, false)).status());
    assertEquals(BlackThreadService.CastStatus.REJECTED_OUT_OF_RANGE, service.cast(
        new BlackThreadService.CastRequest(OWNER, 42, 12.01, ControlProfile.TargetKind.NORMAL, 0, true)).status());
    assertEquals(BlackThreadService.CastStatus.REJECTED_INVALID_TARGET, service.cast(
        new BlackThreadService.CastRequest(other, -1, 4.0, ControlProfile.TargetKind.NORMAL, 0, true)).status());
    assertEquals(0, ledger.stacks(42, OWNER, 1));

    assertEquals(BlackThreadService.CastStatus.APPLIED, service.cast(
        new BlackThreadService.CastRequest(OWNER, 42, 4.0, ControlProfile.TargetKind.NORMAL, 10, true)).status());
    BlackThreadService.CastOutcome cooldown = service.cast(
        new BlackThreadService.CastRequest(OWNER, 42, 4.0, ControlProfile.TargetKind.NORMAL, 11, true));
    assertEquals(BlackThreadService.CastStatus.REJECTED_COOLDOWN, cooldown.status());
    assertEquals(3_010, cooldown.nextReadyAtMs());
    assertEquals(2, ledger.stacks(42, OWNER, 12));
  }

  @Test void verifiedServerCastStacksUntilBoundAndReturnsControlDuration() {
    EntanglementLedger ledger = new EntanglementLedger(5, 3);
    BlackThreadRules rules = new BlackThreadRules(12.0, 2, 8_000, 3_000, 1_200);
    BlackThreadService service = new BlackThreadService(ledger, rules, ControlProfile.defaults());

    BlackThreadService.CastOutcome first = service.cast(new BlackThreadService.CastRequest(
        OWNER, 42, 10.0, ControlProfile.TargetKind.NORMAL, 0, true));
    assertEquals(BlackThreadService.CastStatus.APPLIED, first.status());
    assertEquals(2, first.stacks());
    assertFalse(first.bound());
    assertEquals(0, first.controlDurationMs());
    assertEquals(3_000, first.nextReadyAtMs());

    BlackThreadService.CastOutcome second = service.cast(new BlackThreadService.CastRequest(
        OWNER, 42, 10.0, ControlProfile.TargetKind.NORMAL, 3_001, true));
    assertEquals(4, second.stacks());
    assertTrue(second.bound());
    assertEquals(1_200, second.controlDurationMs());
  }
}
