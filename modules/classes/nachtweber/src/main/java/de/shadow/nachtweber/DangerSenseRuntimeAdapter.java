package de.shadow.nachtweber;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.BlockRaystep;
import java.util.*;
import org.joml.Vector3d;
import org.joml.Vector3dc;
final class DangerSenseRuntimeAdapter{
 private DangerSenseRuntimeAdapter(){}
 static DangerSenseOutcome scan(Store<EntityStore> store,Ref<EntityStore> caster,UUID owner,boolean unlocked,long nowMs,DangerSenseService service,DangerSenseAlertPort alerts,double radius,int maximumCandidates){
  if(store==null||caster==null||owner==null||service==null||alerts==null||!caster.isValid()||caster.getStore()!=store||!Double.isFinite(radius)||radius<=0||radius>16.0||maximumCandidates<1||maximumCandidates>64)return DangerSenseOutcome.of(DangerSenseStatus.INVALID_REQUEST);
  DangerSenseOutcome gate=service.preflight(owner,true,unlocked,nowMs);
  if(gate.status()!=DangerSenseStatus.QUIET)return gate;
  TransformComponent from=store.getComponent(caster,TransformComponent.getComponentType());if(from==null)return DangerSenseOutcome.of(DangerSenseStatus.INVALID_REQUEST);Vector3d origin=new Vector3d((Vector3dc)from.getPosition());Vector3d eye=new Vector3d(origin).add(0,1.6,0);World world=((EntityStore)store.getExternalData()).getWorld();List<DangerCandidate> candidates=new ArrayList<>(Math.min(64,maximumCandidates));
  Selector.selectNearbyEntities(store,origin,radius,target->{if(candidates.size()>=maximumCandidates)return;TransformComponent to=store.getComponent(target,TransformComponent.getComponentType());if(to==null)return;Vector3d position=new Vector3d((Vector3dc)to.getPosition());Vector3d direction=new Vector3d(position).add(0,0.9,0).sub(eye);double distance=direction.length();boolean visible=distance>0.001&&BlockRaystep.clearDistance(world,eye,direction.normalize(),distance,0.3,0.3)+1.2>=distance;candidates.add(new DangerCandidate(target.getIndex(),distance,NachtweberTargeting.isHostile(target,caster,store),visible,target.getStore()==store));},target->target!=null&&target.isValid()&&target.getStore()==store&&target.getIndex()!=caster.getIndex());
  DangerSenseOutcome outcome=service.scan(owner,true,unlocked,nowMs,candidates);if(outcome.status()==DangerSenseStatus.ALERT)alerts.alert(owner,outcome.target(),outcome.distanceBlocks());return outcome;
 }
}