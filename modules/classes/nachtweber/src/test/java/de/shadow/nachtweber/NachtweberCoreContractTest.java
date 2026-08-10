package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class NachtweberCoreContractTest {
  private static final UUID OWNER_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  private static final UUID OWNER_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
  private static final int TARGET = 42;

  @Test void catalogContainsExactlyThreeActivesAndFourPassives() {
    assertEquals(Set.of("BLACK_THREAD", "SHADOW_SWING", "HUNTING_COCOON"), NachtweberAbilityCatalog.activeIds());
    assertEquals(Set.of("DANGER_SENSE", "WALL_HUNTER", "TOXIC_GLANDS", "HUNTING_INSTINCT"), NachtweberAbilityCatalog.passiveIds());
  }

  @Test void entanglementStacksAreAdditiveBoundedAndOwnerIsolated() {
    EntanglementLedger ledger = new EntanglementLedger(5, 3);
    assertEquals(2, ledger.apply(TARGET, OWNER_A, 2, 10_000, 0));
    assertEquals(5, ledger.apply(TARGET, OWNER_A, 9, 10_000, 100));
    assertEquals(1, ledger.apply(TARGET, OWNER_B, 1, 10_000, 100));
    assertEquals(5, ledger.stacks(TARGET, OWNER_A, 101));
    assertEquals(1, ledger.stacks(TARGET, OWNER_B, 101));
    assertTrue(ledger.isBound(TARGET, OWNER_A, 101));
    assertFalse(ledger.isBound(TARGET, OWNER_B, 101));
  }

  @Test void entanglementRefreshesExpiryAndCleanseIsOwnerScoped() {
    EntanglementLedger ledger = new EntanglementLedger(5, 3);
    ledger.apply(TARGET, OWNER_A, 2, 1_000, 0);
    ledger.apply(TARGET, OWNER_A, 1, 1_000, 900);
    ledger.apply(TARGET, OWNER_B, 1, 5_000, 0);
    assertEquals(3, ledger.stacks(TARGET, OWNER_A, 1_500));
    assertEquals(0, ledger.stacks(TARGET, OWNER_A, 1_901));
    ledger.cleanse(TARGET, OWNER_A);
    assertEquals(1, ledger.stacks(TARGET, OWNER_B, 2_000));
  }

  @Test void venomRejectsImmunityAndKeepsOwnerStateSeparate() {
    VenomLedger ledger = new VenomLedger(4, 1_000);
    assertEquals(0, ledger.apply(TARGET, OWNER_A, 2, 5_000, 0, () -> true));
    assertEquals(2, ledger.apply(TARGET, OWNER_A, 2, 5_000, 0, () -> false));
    assertEquals(1, ledger.apply(TARGET, OWNER_B, 1, 5_000, 0, () -> false));
    ledger.cleanse(TARGET, OWNER_A);
    assertFalse(ledger.hasState(TARGET, OWNER_A, 1));
    assertTrue(ledger.hasState(TARGET, OWNER_B, 1));
  }

  @Test void overdueVenomProducesOneTickAndSchedulesFromNowWithoutReplay() {
    VenomLedger ledger = new VenomLedger(4, 1_000);
    ledger.apply(TARGET, OWNER_A, 3, 20_000, 0, () -> false);
    VenomLedger.DueTick tick = ledger.consumeOneDueTick(TARGET, OWNER_A, 10_000);
    assertEquals(3, tick.stacks());
    assertEquals(11_000, tick.nextTickAtMs());
    assertTrue(ledger.consumeOneDueTick(TARGET, OWNER_A, 10_001).isEmpty());
  }

  @Test void venomDamageCauseFirewallRejectsDotAndReflectionRecursion() {
    assertTrue(VenomDamageCause.DIRECT_HIT.mayApplyVenom());
    assertFalse(VenomDamageCause.VENOM_TICK.mayApplyVenom());
    assertFalse(VenomDamageCause.REFLECTION.mayApplyVenom());
  }

  @Test void controlMultipliersReduceBossEliteAndPvpControl() {
    ControlProfile profile = ControlProfile.defaults();
    assertEquals(1.0, profile.factor(ControlProfile.TargetKind.NORMAL));
    assertEquals(0.35, profile.factor(ControlProfile.TargetKind.BOSS));
    assertEquals(0.65, profile.factor(ControlProfile.TargetKind.ELITE));
    assertEquals(0.50, profile.factor(ControlProfile.TargetKind.PVP_PLAYER));
  }

  @Test void configUsesExplicitUnitsAndRejectsNonFiniteOrOutOfRangeValues() {
    NachtweberConfig valid = NachtweberConfig.defaults();
    assertEquals(12.0, valid.blackThreadRangeBlocks());
    assertEquals(8.0, valid.entanglementDurationSeconds());
    assertThrows(IllegalArgumentException.class, () -> valid.withBlackThreadRangeBlocks(Double.NaN));
    assertThrows(IllegalArgumentException.class, () -> valid.withVenomTickIntervalSeconds(Double.POSITIVE_INFINITY));
    assertThrows(IllegalArgumentException.class, () -> valid.withBossControlFactor(1.1));
  }
}
