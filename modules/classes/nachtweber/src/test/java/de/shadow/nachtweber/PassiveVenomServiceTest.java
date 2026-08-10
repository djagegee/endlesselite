package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class PassiveVenomServiceTest {
  private static final UUID OWNER_A=UUID.fromString("40000000-0000-0000-0000-000000000001");
  private static final UUID OWNER_B=UUID.fromString("40000000-0000-0000-0000-000000000002");

  @Test void ownerCleanupRemovesOnlyThatOwnersPassiveCooldown() {
    EntanglementLedger entanglement=new EntanglementLedger(5,3);
    VenomLedger venom=new VenomLedger(4,1_000);
    PassiveVenomService service=new PassiveVenomService(entanglement,venom,
        new PassiveVenomRules(1,1,6_000,1_000));
    assertEquals(PassiveVenomStatus.APPLIED,service.onDirectHit(new PassiveVenomRequest(
        OWNER_A,42,true,true,false,VenomDamageCause.DIRECT_HIT,0,()->false)).status());
    assertEquals(PassiveVenomStatus.APPLIED,service.onDirectHit(new PassiveVenomRequest(
        OWNER_B,42,true,true,false,VenomDamageCause.DIRECT_HIT,0,()->false)).status());

    service.cleanseOwner(OWNER_A);

    assertEquals(PassiveVenomStatus.APPLIED,service.onDirectHit(new PassiveVenomRequest(
        OWNER_A,42,true,true,false,VenomDamageCause.DIRECT_HIT,1,()->false)).status());
    assertEquals(PassiveVenomStatus.COOLDOWN,service.onDirectHit(new PassiveVenomRequest(
        OWNER_B,42,true,true,false,VenomDamageCause.DIRECT_HIT,1,()->false)).status());
  }

  @Test void bundledConfigUsesSecondsCountsAndExplicitDirectHitGate() throws Exception {
    try(var stream=getClass().getClassLoader().getResourceAsStream("config/nachtweber.json")){
      assertNotNull(stream);
      var root=JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();
      assertEquals(9,root.get("schemaVersion").getAsInt());
      var config=root.getAsJsonObject("passiveVenom");
      assertTrue(config.get("verifiedDirectHitOnly").getAsBoolean());
      var toxic=config.getAsJsonObject("toxicGlands");
      assertEquals(1,toxic.get("baseVenomStacks").getAsInt());
      assertEquals(6.0,toxic.get("venomDurationSeconds").getAsDouble());
      assertEquals(1.0,toxic.get("internalCooldownSeconds").getAsDouble());
      var hunting=config.getAsJsonObject("huntingInstinct");
      assertTrue(hunting.get("requiresOwnEntanglement").getAsBoolean());
      assertEquals(1,hunting.get("bonusVenomStacks").getAsInt());
    }
  }

  @Test void toxicGlandsAppliesOwnerVenomAndHuntingInstinctRewardsOnlyOwnEntanglement() {
    EntanglementLedger entanglement=new EntanglementLedger(5,3);
    VenomLedger venom=new VenomLedger(4,1_000);
    entanglement.apply(42,OWNER_A,3,8_000,0);
    entanglement.apply(42,OWNER_B,3,8_000,0);
    PassiveVenomService service=new PassiveVenomService(entanglement,venom,
        new PassiveVenomRules(1,1,6_000,1_000));

    PassiveVenomOutcome outcome=service.onDirectHit(new PassiveVenomRequest(
        OWNER_A,42,true,true,true,VenomDamageCause.DIRECT_HIT,0,()->false));

    assertEquals(PassiveVenomStatus.APPLIED,outcome.status());
    assertEquals(2,outcome.appliedStacks());
    assertEquals(2,venom.consumeOneDueTick(42,OWNER_A,1_000).stacks());
    assertTrue(venom.consumeOneDueTick(42,OWNER_B,1_000).isEmpty());
  }

  @Test void recursiveImmuneAndCooldownHitsDoNotMutateVenomOrOtherOwners() {
    EntanglementLedger entanglement=new EntanglementLedger(5,3);
    VenomLedger venom=new VenomLedger(4,1_000);
    entanglement.apply(42,OWNER_B,3,8_000,0);
    PassiveVenomService service=new PassiveVenomService(entanglement,venom,
        new PassiveVenomRules(1,1,6_000,1_000));

    assertEquals(PassiveVenomStatus.RECURSIVE_CAUSE,service.onDirectHit(new PassiveVenomRequest(
        OWNER_A,42,true,true,true,VenomDamageCause.VENOM_TICK,0,()->false)).status());
    assertEquals(PassiveVenomStatus.IMMUNE,service.onDirectHit(new PassiveVenomRequest(
        OWNER_A,42,true,true,true,VenomDamageCause.DIRECT_HIT,0,()->true)).status());
    assertFalse(venom.hasState(42,OWNER_A,0));

    assertEquals(PassiveVenomStatus.APPLIED,service.onDirectHit(new PassiveVenomRequest(
        OWNER_A,42,true,true,true,VenomDamageCause.DIRECT_HIT,0,()->false)).status());
    assertEquals(PassiveVenomStatus.COOLDOWN,service.onDirectHit(new PassiveVenomRequest(
        OWNER_A,42,true,true,true,VenomDamageCause.DIRECT_HIT,999,()->false)).status());
    assertEquals(1,venom.consumeOneDueTick(42,OWNER_A,1_000).stacks());

    assertEquals(PassiveVenomStatus.APPLIED,service.onDirectHit(new PassiveVenomRequest(
        OWNER_B,42,true,true,false,VenomDamageCause.DIRECT_HIT,0,()->false)).status());
    assertEquals(1,venom.consumeOneDueTick(42,OWNER_B,1_000).stacks());
  }
}
