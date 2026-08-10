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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/** Store-bound BLACK_THREAD effect; no ledger or service escapes the bound runtime. */
final class StoreBoundBlackThreadAbility implements AbilityEffect {
  private final NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator;
  private final LongSupplier clock;

  StoreBoundBlackThreadAbility(
      NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator, LongSupplier clock) {
    this.coordinator = coordinator;
    this.clock = clock;
  }

  @Override public ParamSpec getParamSpec() { return BlackThreadAbilityContracts.paramSpec(); }

  @Override public AbilityResult execute(CasterContext context, AbilityDefinition ability) {
    if (!valid(context, ability)) return AbilityResult.conditionFailed("Requires a bound player caster");
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    if (!store.isInThread()) return AbilityResult.error("BLACK_THREAD requires the world thread");
    double range = ability.getNumber("range", 12.0);
    Ref<EntityStore> target = NachtweberTargeting.closestVisibleHostile(store, caster, range);
    if (target == null) return AbilityResult.noTarget();
    TransformComponent from = store.getComponent(caster, TransformComponent.getComponentType());
    TransformComponent to = store.getComponent(target, TransformComponent.getComponentType());
    if (from == null || to == null) return AbilityResult.conditionFailed("Target position is unavailable");
    long now = clock.getAsLong();
    UUID owner = context.casterUuid();
    AtomicReference<Optional<BlackThreadService.CastOutcome>> result =
        new AtomicReference<>(Optional.empty());
    boolean dispatched = coordinator.withGameplay(store, gameplay -> result.set(
        gameplay.castBlackThread(new BlackThreadService.CastRequest(
            owner, target.getIndex(), from.getPosition().distance(to.getPosition()),
            ControlProfile.TargetKind.NORMAL, now, true))));
    if (!dispatched || result.get().isEmpty()) return AbilityResult.error("BLACK_THREAD store is unavailable");
    BlackThreadService.CastOutcome outcome = result.get().orElseThrow();
    return switch (outcome.status()) {
      case APPLIED -> {
        if (outcome.bound() && outcome.controlDurationMs() > 0L) {
          NachtweberControl.applyStun(store, target, outcome.controlDurationMs(), caster);
        }
        yield AbilityResult.success();
      }
      case REJECTED_COOLDOWN -> AbilityResult.onCooldown(
          Math.max(0L, outcome.nextReadyAtMs() - clock.getAsLong()));
      case REJECTED_OUT_OF_RANGE -> AbilityResult.conditionFailed("Target is outside server range");
      case REJECTED_NOT_SERVER, REJECTED_INVALID_TARGET ->
          AbilityResult.error("BLACK_THREAD validation rejected the cast");
    };
  }

  private static boolean valid(CasterContext context, AbilityDefinition ability) {
    if (context == null || ability == null || !context.isPlayer() || context.casterUuid() == null) return false;
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    return store != null && caster != null && caster.isValid() && caster.getStore() == store;
  }
}
