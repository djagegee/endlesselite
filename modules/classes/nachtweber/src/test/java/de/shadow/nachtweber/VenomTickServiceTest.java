package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.airijko.endlessleveling.systems.PlayerCombatSystem;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;

import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class VenomTickServiceTest {
  private static final UUID OWNER_A=UUID.fromString("30000000-0000-0000-0000-000000000001");
  private static final UUID OWNER_B=UUID.fromString("30000000-0000-0000-0000-000000000002");

  @Test void venomLedgerEnumeratesOnlyActiveOwnersForTheExactTarget() {
    VenomLedger ledger = new VenomLedger(4, 1_000L);
    ledger.apply(7, OWNER_A, 2, 5_000L, 0L, () -> false);
    ledger.apply(7, OWNER_B, 1, 500L, 0L, () -> false);
    ledger.apply(8, OWNER_B, 1, 5_000L, 0L, () -> false);

    assertEquals(java.util.Set.of(OWNER_A, OWNER_B),
        java.util.Set.copyOf(ledger.activeOwners(7, 100L)));
    assertEquals(List.of(OWNER_A), ledger.activeOwners(7, 501L));
    assertEquals(List.of(OWNER_B), ledger.activeOwners(8, 501L));
    assertTrue(ledger.activeOwners(9, 501L).isEmpty());
  }

  @Test void damageCauseReadinessProbeFailsClosedBeforeServerBoot() {
    DamageCauseReadinessProbe.Result result =
        assertDoesNotThrow(DamageCauseReadinessProbe::inspectRuntime);

    assertFalse(result.ready());
    assertEquals(DamageCauseReadinessProbe.Status.UNAVAILABLE, result.status());
    assertNull(result.causeId());
    assertEquals(-1, result.assetIndex());
  }

  @Test void damageCauseReadinessReportsTheFirstReadyResultOnly() {
    var results = new java.util.ArrayDeque<DamageCauseReadinessProbe.Result>();
    results.add(new DamageCauseReadinessProbe.Result(
        DamageCauseReadinessProbe.Status.UNAVAILABLE, null, -1));
    results.add(new DamageCauseReadinessProbe.Result(
        DamageCauseReadinessProbe.Status.READY, "Physical", 3));
    results.add(new DamageCauseReadinessProbe.Result(
        DamageCauseReadinessProbe.Status.READY, "Physical", 3));
    DamageCauseReadinessProbe probe = new DamageCauseReadinessProbe(results::remove);
    List<DamageCauseReadinessProbe.Result> observed = new ArrayList<>();

    assertFalse(probe.reportReadyOnce(observed::add));
    assertTrue(probe.reportReadyOnce(observed::add));
    assertFalse(probe.reportReadyOnce(observed::add));
    assertEquals(1, observed.size());
    assertEquals("Physical", observed.get(0).causeId());
    assertEquals(3, observed.get(0).assetIndex());
  }

  @Test void installedAbilityDotAbiIsPresentAndFactoryFailsClosedBeforeServerBoot() throws Exception {
    assertNotNull(PlayerCombatSystem.class.getDeclaredMethod(
        "createAbilityDotDamage",Ref.class,float.class,String.class));
    assertNotNull(PlayerCombatSystem.class.getDeclaredMethod(
        "isAbilityOriginProcDamage",com.hypixel.hytale.server.core.modules.entity.damage.Damage.class));
    assertNotNull(PlayerCombatSystem.class.getDeclaredMethod(
        "shouldBypassOutgoingAugmentMath",com.hypixel.hytale.server.core.modules.entity.damage.Damage.class));
    assertNotNull(com.airijko.endlessleveling.api.EndlessLevelingAPI.class.getDeclaredMethod(
        "getDisplayedAttributeTotal",UUID.class,com.airijko.endlessleveling.enums.SkillAttributeType.class,double.class));
    assertTrue(Double.isNaN(new EndlessLevelingVenomPowerProvider().sorcery(OWNER_A)));
    Ref<EntityStore> ownerRef=new Ref<>(null,7);
    assertNull(EndlessLevelingVenomDamageAdapter.createVerifiedDamage(ownerRef,3.5f));
  }

  @Test void damagePortFailureIsContainedAndDoesNotCreateReplayBurst() {
    VenomLedger ledger=new VenomLedger(4,1_000);
    ledger.apply(42,OWNER_A,4,10_000,0,()->false);
    VenomTickService service=new VenomTickService(ledger,new VenomTickRules(2.0,100.0,100.0),owner->0.0);
    int[] calls={0};

    VenomTickService.TickOutcome failed=assertDoesNotThrow(()->service.tick(42,OWNER_A,5_000,
        (owner,target,damage,cause)->{calls[0]++;throw new IllegalStateException("runtime unavailable");}));

    assertEquals(VenomTickService.TickStatus.DAMAGE_REJECTED,failed.status());
    assertEquals(1,calls[0]);
    assertEquals(VenomTickService.TickStatus.NOT_DUE,
        service.tick(42,OWNER_A,5_000,(owner,target,damage,cause)->true).status());
  }

  @Test void oneOverdueTickUsesOwnersStacksAndSorceryExactlyOnce() {
    VenomLedger ledger=new VenomLedger(4,1_000);
    ledger.apply(42,OWNER_A,3,10_000,0,()->false);
    ledger.apply(42,OWNER_B,2,10_000,0,()->false);
    List<String> emissions=new ArrayList<>();
    VenomTickService service=new VenomTickService(ledger,new VenomTickRules(1.5,100.0,100.0),
        owner->owner.equals(OWNER_A)?20.0:0.0);

    VenomTickService.TickOutcome outcome=service.tick(42,OWNER_A,5_000,
        (owner,target,damage,cause)->{emissions.add(owner+":"+target+":"+damage+":"+cause);return true;});

    assertEquals(VenomTickService.TickStatus.EMITTED,outcome.status());
    assertEquals(3,outcome.stacks());
    assertEquals(5.4f,outcome.damage(),0.0001f);
    assertEquals(6_000,outcome.nextTickAtMs());
    assertEquals(List.of(OWNER_A+":42:5.4:VENOM_TICK"),emissions);
    assertEquals(VenomTickService.TickStatus.NOT_DUE,service.tick(42,OWNER_A,5_000,(a,t,d,c)->true).status());
    assertEquals(2,ledger.consumeOneDueTick(42,OWNER_B,5_000).stacks());
  }

  @Test void bundledConfigUsesSecondsCountsAndExplicitSorceryScaling() throws Exception {
    try(var stream=getClass().getClassLoader().getResourceAsStream("config/nachtweber.json")){
      assertNotNull(stream);
      var root=JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();
      assertEquals(9,root.get("schemaVersion").getAsInt());
      var config=root.getAsJsonObject("venomTick");
      assertEquals(1.0,config.get("intervalSeconds").getAsDouble());
      assertEquals(1.5,config.get("baseDamagePerStack").getAsDouble());
      assertEquals(100.0,config.get("sorceryPointsForDoubleDamage").getAsDouble());
      assertEquals(100.0,config.get("maximumDamagePerTick").getAsDouble());
      assertEquals(1,config.get("maximumOverdueTicksPerUpdate").getAsInt());
      assertEquals("Poison",config.get("damageCause").getAsString());
    }
  }
}
