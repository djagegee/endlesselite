package de.shadow.nachtweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Server-side BLACK_THREAD effect. Runtime registration remains staged until effect ownership can be unloaded safely. */
final class BlackThreadAbility implements AbilityEffect {
  private final BlackThreadService service;
  private final LongSupplier clock;
  BlackThreadAbility(BlackThreadService service,LongSupplier clock){this.service=Objects.requireNonNull(service);this.clock=Objects.requireNonNull(clock);}
  @Override public ParamSpec getParamSpec(){return BlackThreadAbilityContracts.paramSpec();}
  @Override public AbilityResult execute(CasterContext context,AbilityDefinition ability){
    if(context==null||!context.isPlayer()||context.casterUuid()==null) return AbilityResult.conditionFailed("Requires a player caster");
    Store<EntityStore> store=context.store(); Ref<EntityStore> caster=context.caster();
    if(store==null||caster==null||!caster.isValid()||caster.getStore()!=store||ability==null) return AbilityResult.error("BLACK_THREAD runtime is unavailable");
    Ref<EntityStore> target=NachtweberTargeting.closestVisibleHostile(store,caster,ability.getNumber("range",0.0));
    if(target==null) return AbilityResult.noTarget();
    TransformComponent from=store.getComponent(caster,TransformComponent.getComponentType());
    TransformComponent to=store.getComponent(target,TransformComponent.getComponentType());
    if(from==null||to==null) return AbilityResult.conditionFailed("Target position is unavailable");
    double distance=from.getPosition().distance(to.getPosition());
    BlackThreadService.CastOutcome outcome=service.cast(new BlackThreadService.CastRequest(
        context.casterUuid(),target.getIndex(),distance,ControlProfile.TargetKind.BOSS,clock.getAsLong(),true));
    return switch(outcome.status()) {
      case APPLIED -> { if(outcome.bound()&&outcome.controlDurationMs()>0L) NachtweberControl.applyStun(store,target,outcome.controlDurationMs(),caster); yield AbilityResult.success(); }
      case REJECTED_COOLDOWN -> AbilityResult.onCooldown(Math.max(0L,outcome.nextReadyAtMs()-clock.getAsLong()));
      case REJECTED_OUT_OF_RANGE -> AbilityResult.conditionFailed("Target is outside server range");
      case REJECTED_NOT_SERVER,REJECTED_INVALID_TARGET -> AbilityResult.error("BLACK_THREAD validation rejected the cast");
    };
  }
}
