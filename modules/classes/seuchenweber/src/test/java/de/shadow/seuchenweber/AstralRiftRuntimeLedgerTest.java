package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AstralRiftRuntimeLedgerTest {
  private static final UUID OWNER = UUID.fromString("e1ec7a1a-0000-4000-8000-000000000001");

  @Test void emitsAtMostOnePulseWhenSeveralIntervalsWereMissed() {
    AstralRiftRuntimeLedger ledger = new AstralRiftRuntimeLedger(1_000L);
    ledger.open(OWNER, 10.0, 20.0, 30.0, 0L, 5_000L);
    assertEquals(1, ledger.consumeDue(1_000L).size());
    assertEquals(1, ledger.consumeDue(4_900L).size());
    assertTrue(ledger.consumeDue(4_900L).isEmpty());
  }

  @Test void removesExpiredRiftsWithoutFuturePulses() {
    AstralRiftRuntimeLedger ledger = new AstralRiftRuntimeLedger(1_000L);
    ledger.open(OWNER, 10.0, 20.0, 30.0, 0L, 2_000L);
    assertTrue(ledger.consumeDue(2_000L).isEmpty());
    assertEquals(0, ledger.activeCount());
  }

  @Test void exposesExpiredOwnersExactlyOnceForCasterReferenceCleanup() {
    AstralRiftRuntimeLedger ledger = new AstralRiftRuntimeLedger(1_000L);
    ledger.open(OWNER, 10.0, 20.0, 30.0, 0L, 2_000L);

    ledger.consumeDue(2_000L);

    assertEquals(java.util.List.of(OWNER), ledger.drainExpiredOwners());
    assertTrue(ledger.drainExpiredOwners().isEmpty());
  }

  @Test void rejectsNonPositiveDurationWithoutRetainingOwnerState() {
    AstralRiftRuntimeLedger ledger = new AstralRiftRuntimeLedger(1_000L);
    ledger.open(OWNER, 1.0, 2.0, 3.0, 0L, 0L);
    assertEquals(0, ledger.activeCount());
    assertTrue(ledger.drainExpiredOwners().isEmpty());
  }

  @Test void expiresWithoutConsumingAOverduePulse() {
    AstralRiftRuntimeLedger ledger = new AstralRiftRuntimeLedger(1_000L);
    ledger.open(OWNER, 10.0, 20.0, 30.0, 0L, 2_000L);

    assertEquals(java.util.List.of(OWNER), ledger.expireOnly(2_000L));
    assertEquals(0, ledger.activeCount());
    assertTrue(ledger.consumeDue(2_000L).isEmpty());
  }

  @Test void exposesNextPulseOrExpirationForScanSuppression() {
    AstralRiftRuntimeLedger ledger = new AstralRiftRuntimeLedger(1_000L);
    assertEquals(Long.MAX_VALUE, ledger.nextEventAtMs());

    ledger.open(OWNER, 1.0, 2.0, 3.0, 100L, 5_000L);
    assertEquals(1_100L, ledger.nextEventAtMs());
    ledger.consumeDue(1_100L);
    assertEquals(2_100L, ledger.nextEventAtMs());
  }
}
