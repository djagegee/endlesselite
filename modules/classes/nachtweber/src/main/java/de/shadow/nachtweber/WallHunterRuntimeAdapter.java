package de.shadow.nachtweber;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.collision.*;
import com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
final class WallHunterRuntimeAdapter{
 private WallHunterRuntimeAdapter(){}
 static WallHunterOutcome apply(Store<EntityStore> store,Ref<EntityStore> player,UUID owner,boolean unlocked,WallHunterService service){
  if(store==null||player==null||owner==null||service==null||!player.isValid()||player.getStore()!=store)return WallHunterOutcome.of(WallHunterStatus.INVALID_REQUEST);
  CollisionResultComponent collision=store.getComponent(player,CollisionResultComponent.getComponentType());MovementStatesComponent movement=store.getComponent(player,MovementStatesComponent.getComponentType());Velocity velocity=store.getComponent(player,Velocity.getComponentType());if(collision==null||movement==null||movement.getMovementStates()==null||velocity==null)return WallHunterOutcome.of(WallHunterStatus.INACTIVE);
  boolean horizontal=hasReadyHorizontalWallContact(collision);
  WallHunterOutcome outcome=service.plan(new WallHunterRequest(owner,true,unlocked,horizontal,movement.getMovementStates().jumping));if(outcome.status()==WallHunterStatus.CLIMBING)velocity.setY(Math.max(velocity.getY(),outcome.verticalVelocity()));return outcome;
 }
 static boolean hasReadyHorizontalWallContact(CollisionResultComponent collision){
  if(collision==null||collision.isPendingCollisionCheck())return false;
  CollisionResult result=collision.getCollisionResult();
  if(result==null)return false;
  for(int i=0;i<result.getBlockCollisionCount();i++){
   BlockCollisionData block=result.getBlockCollision(i);
   if(block!=null&&(block.touching||block.overlapping)&&Math.abs(block.collisionNormal.y)<0.5)return true;
  }
  return false;
 }
}