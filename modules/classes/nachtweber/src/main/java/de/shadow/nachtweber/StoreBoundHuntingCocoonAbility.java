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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/** Store-bound HUNTING_COCOON effect sharing the exact Black Thread ledger in that store. */
final class StoreBoundHuntingCocoonAbility implements AbilityEffect {
  private final NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator;
  private final VenomImmunityResolver immunity;
  private final LongSupplier clock;
  private final OwnerEntityBindingPort ownerBindings;

  StoreBoundHuntingCocoonAbility(
      NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator,
      VenomImmunityResolver immunity,
      LongSupplier clock,
      OwnerEntityBindingPort ownerBindings) {
    this.coordinator = coordinator;
    this.immunity = immunity;
    this.clock = clock;
    this.ownerBindings = ownerBindings;
  }

  @Override public ParamSpec getParamSpec() { return HuntingCocoonAbilityContracts.paramSpec(); }

  @Override public AbilityResult execute(CasterContext context, AbilityDefinition ability) {
    if (!valid(context, ability)) return AbilityResult.conditionFailed("Requires a bound player caster");
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    if (!store.isInThread()) return AbilityResult.error("HUNTING_COCOON requires the world thread");
    if (!ownerBindings.bind(store, context.casterUuid(), caster)) {
      return AbilityResult.error("HUNTING_COCOON owner binding failed");
    }
    Ref<EntityStore> target = NachtweberTargeting.closestVisibleHostile(
        store, caster, ability.getNumber("range", 8.0));
    if (target == null) return AbilityResult.noTarget();
    TransformComponent from = store.getComponent(caster, TransformComponent.getComponentType());
    TransformComponent to = store.getComponent(target, TransformComponent.getComponentType());
    if (from == null || to == null) return AbilityResult.conditionFailed("Target position is unavailable");
    long now = clock.getAsLong();
    AtomicReference<Optional<HuntingCocoonService.CastOutcome>> result =
        new AtomicReference<>(Optional.empty());
    boolean dispatched = coordinator.withGameplay(store, gameplay -> result.set(
        gameplay.castHuntingCocoon(new HuntingCocoonService.CastRequest(
            context.casterUuid(), target.getIndex(), from.getPosition().distance(to.getPosition()),
            now, true, VenomDamageCause.DIRECT_HIT, () -> immunity.isImmune(store, target)))));
    if (!dispatched || result.get().isEmpty()) return AbilityResult.error("HUNTING_COCOON store is unavailable");
    HuntingCocoonService.CastOutcome outcome = result.get().orElseThrow();
    return switch (outcome.status()) {
      case APPLIED -> {
        NachtweberControl.applyStun(
            store, target, outcome.immobilizeDurationMs(), caster);
        yield AbilityResult.success();
      }
      case REJECTED_COOLDOWN -> AbilityResult.onCooldown(
          Math.max(0L, outcome.nextReadyAtMs() - clock.getAsLong()));
      case REJECTED_OUT_OF_RANGE -> AbilityResult.conditionFailed("Target is outside server range");
      case REJECTED_IMMUNE -> AbilityResult.conditionFailed("Target is immune to Fanggift");
      case REJECTED_INSUFFICIENT_ENTANGLEMENT ->
          AbilityResult.conditionFailed("Requires owner-bound entanglement");
      case REJECTED_NOT_SERVER, REJECTED_INVALID_TARGET, REJECTED_RECURSIVE_CAUSE ->
          AbilityResult.error("HUNTING_COCOON validation rejected the cast");
    };
  }

  private static boolean valid(CasterContext context, AbilityDefinition ability) {
    if (context == null || ability == null || !context.isPlayer() || context.casterUuid() == null) return false;
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    return store != null && caster != null && caster.isValid() && caster.getStore() == store;
  }
}
