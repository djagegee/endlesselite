package de.shadow.nachtweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;

import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.ziggfreed.mmoskilltree.ability.BlockRaystep;
import java.util.Locale;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Bounded server-side hostile NPC selection; PvP targeting is deliberately excluded. */
final class NachtweberTargeting {
  private static final String[] NON_COMBAT={"banker","blacksmith","citizen","guide","merchant","quest","shop","trader","trainer","vendor","villager"};
  private NachtweberTargeting() { }
  static Ref<EntityStore> closestVisibleHostile(Store<EntityStore> store,Ref<EntityStore> caster,double range){
    if(store==null||caster==null||!caster.isValid()||caster.getStore()!=store||!Double.isFinite(range)||range<=0.0)return null;
    TransformComponent casterTransform=store.getComponent(caster,TransformComponent.getComponentType()); HeadRotation head=store.getComponent(caster,HeadRotation.getComponentType()); if(casterTransform==null||head==null)return null;
    Vector3d origin=new Vector3d((Vector3dc)casterTransform.getPosition()); Vector3d eye=new Vector3d(origin).add(0,1.6,0); Vector3d forward=new Vector3d((Vector3dc)head.getDirection()); if(forward.lengthSquared()<1e-4)forward.set(0,0,1);else forward.normalize();
    World world=((EntityStore)store.getExternalData()).getWorld(); Candidate[] closest=new Candidate[1];
    Selector.selectNearbyEntities(store,origin,range,target->{if(!isHostile(target,caster,store))return;TransformComponent transform=store.getComponent(target,TransformComponent.getComponentType());if(transform==null)return;Vector3d position=new Vector3d((Vector3dc)transform.getPosition());Vector3d toward=new Vector3d(position).add(0,0.9,0).sub(eye);double distance=toward.length();if(distance<0.001)return;toward.div(distance);if(forward.dot(toward)<0.55)return;if(BlockRaystep.clearDistance(world,eye,toward,distance,0.3,0.3)+1.2<distance)return;double squared=position.distanceSquared(origin);if(closest[0]==null||squared<closest[0].distanceSquared)closest[0]=new Candidate(target,squared);},target->target!=null&&target.isValid()&&target.getStore()==store&&target.getIndex()!=caster.getIndex());
    return closest[0]==null?null:closest[0].target;
  }
  static boolean isHostile(Ref<EntityStore> target,Ref<EntityStore> caster,Store<EntityStore> store){
    if(target==null||caster==null||store==null||target.getStore()!=store||caster.getStore()!=store||!target.isValid())return false;
    NPCEntity npc=store.getComponent(target,NPCEntity.getComponentType()); Role role=npc==null?null:npc.getRole(); return role!=null&&!role.isInvulnerable()&&!role.isFriendly(caster,store)&&!service(npc.getRoleName())&&!service(role.getRoleName())&&!service(role.getNameTranslationKey())&&npc.getCanCauseDamage(caster,store);
  }
  private static boolean service(String value){if(value==null||value.isBlank())return false;String normalized=value.toLowerCase(Locale.ROOT);for(String hint:NON_COMBAT)if(normalized.contains(hint))return true;return false;}
  private record Candidate(Ref<EntityStore> target,double distanceSquared) { }
}
