package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NekrotoxinRuntimeLedgerTest {
  private static final UUID OWNER_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
  private static final UUID OWNER_B = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

  @Test void keepsDueTicksIsolatedByTargetEntityIndexAndOwner() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 2, 3_000, 0);
    ledger.apply(42, OWNER_B, 1, 3_000, 0);

    assertEquals(2, ledger.consumeOneDueTick(42, OWNER_A, 1_000).stacks());
    assertEquals(1, ledger.consumeOneDueTick(42, OWNER_B, 1_000).stacks());
    assertTrue(ledger.consumeOneDueTick(42, OWNER_A, 1_000).isEmpty(), "same owner cannot receive duplicate tick in interval");
  }

  @Test void newlyAppliedMarkTicksImmediatelyThenRespectsConfiguredCadence() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 3, 10_000, 0);

    assertEquals(3, ledger.consumeOneDueTick(42, OWNER_A, 0).stacks());
    assertTrue(ledger.consumeOneDueTick(42, OWNER_A, 999).isEmpty());
    assertEquals(3, ledger.consumeOneDueTick(42, OWNER_A, 1_000).stacks());
  }

  @Test void advancesOverdueScheduleWithoutReplayingMissedTicks() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 3, 10_000, 0);

    assertEquals(3, ledger.consumeOneDueTick(42, OWNER_A, 5_500).stacks());
    assertTrue(ledger.consumeOneDueTick(42, OWNER_A, 5_500).isEmpty());
    assertEquals(3, ledger.consumeOneDueTick(42, OWNER_A, 6_500).stacks());
  }

  @Test void returnsAllDueOwnersForOneTargetWithoutCrossOwnerMutation() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 2, 3_000, 0);
    ledger.apply(42, OWNER_B, 1, 3_000, 0);

    assertEquals(Set.of(
        new NekrotoxinRuntimeLedger.DueTick(OWNER_A, 2),
        new NekrotoxinRuntimeLedger.DueTick(OWNER_B, 1)),
        Set.copyOf(ledger.consumeDueTicks(42, 1_000)));
    assertTrue(ledger.consumeDueTicks(42, 1_000).isEmpty());
  }

  @Test void marksEchoOriginSoItCannotRecursivelyCreateAnotherEcho() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 1, 3_000, 0, false);
    NekrotoxinRuntimeLedger.DueTick tick = ledger.consumeDueTicks(42, 1_000).getFirst();
    assertFalse(tick.allowAstralEcho());
  }

  @Test void emitsOnlyNaturallyExpiredFullMarksForRelicAttunement() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 5, 1_000, 0);
    ledger.apply(42, OWNER_B, 4, 1_000, 0);

    assertEquals(Set.of(new NekrotoxinRuntimeLedger.NaturalExpiration(OWNER_A, 5)),
        Set.copyOf(ledger.consumeNaturalExpirations(42, 1_000)));
    assertTrue(ledger.consumeNaturalExpirations(42, 1_000).isEmpty());
  }

  @Test void expirationEventsExposeFullAndPartialMarksForReferenceCleanup() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 5, 1_000, 0);
    ledger.apply(42, OWNER_B, 4, 1_000, 0);

    assertEquals(Set.of(
        new NekrotoxinRuntimeLedger.Expiration(OWNER_A, 5, true),
        new NekrotoxinRuntimeLedger.Expiration(OWNER_B, 4, false)),
        Set.copyOf(ledger.consumeExpirations(42, 1_000)));
    assertTrue(ledger.consumeExpirations(42, 1_000).isEmpty());
    assertFalse(ledger.hasOwner(OWNER_A));
    assertFalse(ledger.hasOwner(OWNER_B));
  }

  @Test void globalExpirationSweepCleansMarksWhoseTargetNoLongerTicks() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 5, 1_000, 0);
    ledger.apply(99, OWNER_B, 4, 1_000, 0);

    assertEquals(Set.of(
        new NekrotoxinRuntimeLedger.TargetExpiration(42, OWNER_A, 5, true),
        new NekrotoxinRuntimeLedger.TargetExpiration(99, OWNER_B, 4, false)),
        Set.copyOf(ledger.consumeAllExpirations(1_000)));
    assertFalse(ledger.hasOwner(OWNER_A));
    assertFalse(ledger.hasOwner(OWNER_B));
  }

  @Test void expiresAndCanClearAllEphemeralRuntimeStateOnReload() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 2, 1_000, 0);
    assertTrue(ledger.consumeOneDueTick(42, OWNER_A, 1_000).isEmpty());
    ledger.apply(43, OWNER_B, 2, 5_000, 0);
    ledger.clear();
    assertFalse(ledger.hasState(43, OWNER_B));
  }

  @Test void discardingReusedTargetIndexDoesNotCreateNaturalExpirationReward() {
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000);
    ledger.apply(42, OWNER_A, 5, 5_000, 0);
    ledger.apply(99, OWNER_A, 1, 5_000, 0);

    assertEquals(java.util.List.of(OWNER_A), ledger.discardTarget(42));
    assertFalse(ledger.hasState(42, OWNER_A));
    assertTrue(ledger.hasState(99, OWNER_A));
    assertEquals(java.util.List.of(), ledger.consumeAllExpirations(4_000));
  }
}
