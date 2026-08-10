package de.shadow.nachtweber;
import java.util.*;
final class ShadowSwingService{
 private final ShadowSwingRules rules;private final Map<UUID,Long> readyAt=new HashMap<>();
 ShadowSwingService(ShadowSwingRules rules){this.rules=Objects.requireNonNull(rules);}
 synchronized ShadowSwingOutcome plan(ShadowSwingRequest r){
  if(r==null||r.owner()==null||r.nowMs()<0||!Double.isFinite(r.anchorDistanceBlocks())||!Double.isFinite(r.directionX())||!Double.isFinite(r.directionY())||!Double.isFinite(r.directionZ()))return ShadowSwingOutcome.rejected(ShadowSwingStatus.INVALID_REQUEST);
  if(!r.serverVerified())return ShadowSwingOutcome.rejected(ShadowSwingStatus.NOT_AUTHORIZED);
  if(r.anchorDistanceBlocks()<rules.minimumAnchorDistanceBlocks()||r.anchorDistanceBlocks()>=rules.rangeBlocks())return ShadowSwingOutcome.rejected(ShadowSwingStatus.NO_ANCHOR);
  long current=readyAt.getOrDefault(r.owner(),0L);if(r.nowMs()<current)return ShadowSwingOutcome.rejected(ShadowSwingStatus.COOLDOWN);
  double length=Math.sqrt(r.directionX()*r.directionX()+r.directionY()*r.directionY()+r.directionZ()*r.directionZ());if(!Double.isFinite(length)||length<1e-6)return ShadowSwingOutcome.rejected(ShadowSwingStatus.INVALID_REQUEST);
  double x=r.directionX()/length*rules.horizontalImpulse();double y=r.directionY()/length*rules.horizontalImpulse()+rules.verticalImpulse();double z=r.directionZ()/length*rules.horizontalImpulse();long next=TimeMath.saturatedAdd(r.nowMs(),rules.cooldownMs());readyAt.put(r.owner(),next);return new ShadowSwingOutcome(ShadowSwingStatus.APPLIED,x,y,z,next);
 }
 synchronized void cleanseOwner(UUID owner){if(owner!=null)readyAt.remove(owner);}
}