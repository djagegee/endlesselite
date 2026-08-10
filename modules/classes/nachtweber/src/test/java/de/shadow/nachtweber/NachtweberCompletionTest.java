package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class NachtweberCompletionTest {
  private static final UUID OWNER=UUID.fromString("50000000-0000-0000-0000-000000000001");

  @Test void shadowSwingRequiresVerifiedAnchorAndProducesBoundedOwnerCooldownImpulse() {
    ShadowSwingService service=new ShadowSwingService(new ShadowSwingRules(18.0,2.0,12.0,4.0,5_000));
    assertEquals(ShadowSwingStatus.NO_ANCHOR,service.plan(new ShadowSwingRequest(OWNER,true,18.0,1,0,0,0)).status());
    ShadowSwingOutcome applied=service.plan(new ShadowSwingRequest(OWNER,true,10.0,1,0,0,0));
    assertEquals(ShadowSwingStatus.APPLIED,applied.status());
    assertEquals(12.0,applied.velocityX(),0.0001);
    assertEquals(4.0,applied.velocityY(),0.0001);
    assertEquals(0.0,applied.velocityZ(),0.0001);
    assertEquals(5_000,applied.nextReadyAtMs());
    assertEquals(ShadowSwingStatus.COOLDOWN,service.plan(new ShadowSwingRequest(OWNER,true,8.0,0,0,1,4_999)).status());
  }

  @Test void wallHunterOnlyClimbsOnVerifiedHorizontalWallContact() {
    WallHunterService service=new WallHunterService(new WallHunterRules(4.0));
    assertEquals(WallHunterStatus.INACTIVE,service.plan(new WallHunterRequest(OWNER,true,true,false,true)).status());
    assertEquals(WallHunterStatus.INACTIVE,service.plan(new WallHunterRequest(OWNER,true,true,true,false)).status());
    WallHunterOutcome active=service.plan(new WallHunterRequest(OWNER,true,true,true,true));
    assertEquals(WallHunterStatus.CLIMBING,active.status());
    assertEquals(4.0,active.verticalVelocity(),0.0001);

    CollisionResultComponent collision=new CollisionResultComponent();
    BlockCollisionData block=collision.getCollisionResult().newCollision();
    block.touching=true;
    block.collisionNormal.set(1,0,0);
    assertTrue(WallHunterRuntimeAdapter.hasReadyHorizontalWallContact(collision));
    collision.markPendingCollisionCheck();
    assertFalse(WallHunterRuntimeAdapter.hasReadyHorizontalWallContact(collision));
  }

  @Test void dangerSenseSelectsNearestVisibleHostileWithinBoundedScanAndOwnerCooldown() {
    DangerSenseService service=new DangerSenseService(new DangerSenseRules(16.0,2_000,64));
    List<DangerCandidate> candidates=List.of(
        new DangerCandidate(1,8.0,true,true,true),new DangerCandidate(2,3.0,true,true,true),
        new DangerCandidate(3,1.0,false,true,true),new DangerCandidate(4,2.0,true,false,true));
    DangerSenseOutcome alert=service.scan(OWNER,true,true,0,candidates);
    assertEquals(DangerSenseStatus.ALERT,alert.status());
    assertEquals(2,alert.target());
    assertEquals(3.0,alert.distanceBlocks(),0.0001);
    assertEquals(DangerSenseStatus.COOLDOWN,service.preflight(OWNER,true,true,1_999).status());
    assertEquals(DangerSenseStatus.COOLDOWN,service.scan(OWNER,true,true,1_999,candidates).status());
    assertThrows(IllegalArgumentException.class,()->new DangerSenseRules(16.01,2_000,64));
    assertThrows(IllegalArgumentException.class,()->new DangerSenseRules(16.0,2_000,65));
  }

  @Test void completeEffectRegistryContainsAllThreeActivesAndFailsClosedWithoutCaster() {
    var black=new BlackThreadService(new EntanglementLedger(5,3),new BlackThreadRules(12,2,8_000,3_000,1_200),ControlProfile.defaults());
    var cocoon=new HuntingCocoonService(new EntanglementLedger(5,3),new VenomLedger(4,1_000),new HuntingCocoonRules(8,3,2,8_000,7_000));
    var shadow=new ShadowSwingService(new ShadowSwingRules(18,2,12,4,5_000));
    var effects=NachtweberAbilityRegistry.createCompleteRuntimeEffects(black,shadow,cocoon,VenomImmunityResolver.none());
    assertEquals(3,effects.size());
    assertTrue(effects.containsKey("NACHTWEBER_SHADOW_SWING"));
    assertFalse(effects.get("NACHTWEBER_SHADOW_SWING").getParamSpec().entries().isEmpty());
    assertEquals(AbilityResult.Status.CONDITION_FAILED,effects.get("NACHTWEBER_SHADOW_SWING").execute(null,null).getStatus());
  }

  @Test void finalBundledConfigContainsAllRemainingAbilityAndPassiveUnits() throws Exception {
    try(var stream=getClass().getClassLoader().getResourceAsStream("config/nachtweber.json")){
      assertNotNull(stream);
      var root=JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();
      assertEquals(9,root.get("schemaVersion").getAsInt());
      var shadow=root.getAsJsonObject("abilities").getAsJsonObject("shadowSwing");
      assertEquals(18.0,shadow.get("rangeBlocks").getAsDouble());
      assertEquals(5.0,shadow.get("cooldownSeconds").getAsDouble());
      assertEquals(12.0,shadow.get("horizontalImpulseBlocksPerSecond").getAsDouble());
      assertEquals(4.0,shadow.get("verticalImpulseBlocksPerSecond").getAsDouble());
      var movement=shadow.getAsJsonObject("movementSystem");
      assertTrue(movement.get("serverAuthoritative").getAsBoolean());
      assertEquals("Add",movement.get("instructionType").getAsString());
      assertEquals("PlayerVelocityInstructionSystem",movement.get("producerRunsBefore").getAsString());
      assertTrue(movement.get("failClosedOutsideStoreThread").getAsBoolean());
      var danger=root.getAsJsonObject("dangerSense");
      assertEquals(16.0,danger.get("radiusBlocks").getAsDouble());
      assertEquals(64,danger.get("maximumCandidatesPerScan").getAsInt());
      var wall=root.getAsJsonObject("wallHunter");
      assertEquals(4.0,wall.get("climbSpeedBlocksPerSecond").getAsDouble());
      assertTrue(wall.get("requiresHorizontalWallContact").getAsBoolean());
    }
  }
}
