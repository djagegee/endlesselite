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

/** Server-side HUNTING_COCOON effect; remains staged until effect ownership can be unloaded safely. */
final class HuntingCocoonAbility implements AbilityEffect {
  private final HuntingCocoonService service; private final VenomImmunityResolver immunity; private final LongSupplier clock;
  HuntingCocoonAbility(HuntingCocoonService service,VenomImmunityResolver immunity,LongSupplier clock){this.service=Objects.requireNonNull(service);this.immunity=Objects.requireNonNull(immunity);this.clock=Objects.requireNonNull(clock);}
  @Override public ParamSpec getParamSpec(){return HuntingCocoonAbilityContracts.paramSpec();}
  @Override public AbilityResult execute(CasterContext context,AbilityDefinition ability){
    if(context==null||!context.isPlayer()||context.casterUuid()==null)return AbilityResult.conditionFailed("Requires a player caster");
    Store<EntityStore> store=context.store();Ref<EntityStore> caster=context.caster();
    if(store==null||caster==null||!caster.isValid()||caster.getStore()!=store||ability==null)return AbilityResult.error("HUNTING_COCOON runtime is unavailable");
    Ref<EntityStore> target=NachtweberTargeting.closestVisibleHostile(store,caster,ability.getNumber("range",0.0));
    if(target==null)return AbilityResult.noTarget();
    TransformComponent from=store.getComponent(caster,TransformComponent.getComponentType());TransformComponent to=store.getComponent(target,TransformComponent.getComponentType());
    if(from==null||to==null)return AbilityResult.conditionFailed("Target position is unavailable");
    long now=clock.getAsLong();double distance=from.getPosition().distance(to.getPosition());
    HuntingCocoonService.CastOutcome outcome=service.cast(new HuntingCocoonService.CastRequest(
        context.casterUuid(),target.getIndex(),distance,now,true,VenomDamageCause.DIRECT_HIT,()->immunity.isImmune(store,target)));
    return switch(outcome.status()){
      case APPLIED->AbilityResult.success();
      case REJECTED_COOLDOWN->AbilityResult.onCooldown(Math.max(0L,outcome.nextReadyAtMs()-now));
      case REJECTED_OUT_OF_RANGE->AbilityResult.conditionFailed("Target is outside server range");
      case REJECTED_IMMUNE->AbilityResult.conditionFailed("Target is immune to Fanggift");
      case REJECTED_INSUFFICIENT_ENTANGLEMENT->AbilityResult.conditionFailed("Requires owner-bound entanglement");
      case REJECTED_NOT_SERVER,REJECTED_INVALID_TARGET,REJECTED_RECURSIVE_CAUSE->AbilityResult.error("HUNTING_COCOON validation rejected the cast");
    };
  }
}
