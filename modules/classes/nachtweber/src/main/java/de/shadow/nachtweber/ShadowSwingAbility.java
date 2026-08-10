package de.shadow.nachtweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.BlockRaystep;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import java.util.Objects;
import java.util.function.LongSupplier;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Server-validated anchor and staged Velocity prototype; player reconciliation remains runtime-unverified. */
final class ShadowSwingAbility implements AbilityEffect {
  private final ShadowSwingService service;
  private final LongSupplier clock;
  ShadowSwingAbility(ShadowSwingService service,LongSupplier clock){this.service=Objects.requireNonNull(service);this.clock=Objects.requireNonNull(clock);}
  @Override public ParamSpec getParamSpec(){return ShadowSwingAbilityContracts.paramSpec();}
  @Override public AbilityResult execute(CasterContext context,AbilityDefinition ability){
    if(context==null||!context.isPlayer()||context.casterUuid()==null)return AbilityResult.conditionFailed("Requires a player caster");
    Store<EntityStore> store=context.store();Ref<EntityStore> caster=context.caster();
    if(store==null||caster==null||!caster.isValid()||caster.getStore()!=store||ability==null)return AbilityResult.error("SHADOW_SWING runtime is unavailable");
    TransformComponent transform=store.getComponent(caster,TransformComponent.getComponentType());
    HeadRotation head=store.getComponent(caster,HeadRotation.getComponentType());
    Velocity velocity=store.getComponent(caster,Velocity.getComponentType());
    if(transform==null||head==null||velocity==null)return AbilityResult.conditionFailed("Movement components are unavailable");
    double range=ability.getNumber("range",18.0);Vector3d direction=new Vector3d((Vector3dc)head.getDirection());
    if(!Double.isFinite(range)||range<=0||direction.lengthSquared()<1e-6)return AbilityResult.conditionFailed("No valid swing direction");
    direction.normalize();Vector3d eye=new Vector3d((Vector3dc)transform.getPosition()).add(0,1.6,0);
    World world=((EntityStore)store.getExternalData()).getWorld();double anchorDistance=BlockRaystep.clearDistance(world,eye,direction,range,0.3,0.3);
    long now=clock.getAsLong();ShadowSwingOutcome outcome=service.plan(new ShadowSwingRequest(context.casterUuid(),true,anchorDistance,direction.x,direction.y,direction.z,now));
    return switch(outcome.status()){
      case APPLIED->{velocity.addVelocity(outcome.velocityX(),outcome.velocityY(),outcome.velocityZ());yield AbilityResult.success();}
      case COOLDOWN->AbilityResult.onCooldown(Math.max(0L,outcome.nextReadyAtMs()-clock.getAsLong()));
      case NO_ANCHOR->AbilityResult.noTarget();
      case INVALID_REQUEST,NOT_AUTHORIZED->AbilityResult.error("SHADOW_SWING validation rejected the cast");
    };
  }
}
