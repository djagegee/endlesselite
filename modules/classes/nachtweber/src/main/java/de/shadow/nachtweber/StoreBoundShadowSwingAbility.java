package de.shadow.nachtweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityDefinition;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.AbilityResult;
import com.ziggfreed.mmoskilltree.ability.BlockRaystep;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import com.ziggfreed.mmoskilltree.ability.ParamSpec;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Starts a server-validated store-local reconciliation session; the ECS system owns movement. */
final class StoreBoundShadowSwingAbility implements AbilityEffect {
  private static final double MAX_RANGE = 18.0;
  private static final double MIN_ANCHOR_DISTANCE = 2.0;
  private final NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator;
  private final LongSupplier clock;

  StoreBoundShadowSwingAbility(
      NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator, LongSupplier clock) {
    this.coordinator = coordinator;
    this.clock = clock;
  }

  @Override public ParamSpec getParamSpec() { return ShadowSwingAbilityContracts.paramSpec(); }

  @Override public AbilityResult execute(CasterContext context, AbilityDefinition ability) {
    if (!valid(context, ability)) return AbilityResult.conditionFailed("Requires a bound player caster");
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    if (!store.isInThread()) return AbilityResult.error("SHADOW_SWING requires the world thread");
    TransformComponent transform = store.getComponent(caster, TransformComponent.getComponentType());
    HeadRotation head = store.getComponent(caster, HeadRotation.getComponentType());
    if (transform == null || head == null) {
      return AbilityResult.conditionFailed("Movement components are unavailable");
    }
    double requestedRange = ability.getNumber("range", MAX_RANGE);
    double range = Math.min(MAX_RANGE, requestedRange);
    Vector3d direction = new Vector3d((Vector3dc) head.getDirection());
    if (!Double.isFinite(range) || range <= MIN_ANCHOR_DISTANCE
        || !finite(direction) || direction.lengthSquared() < 1e-6) {
      return AbilityResult.conditionFailed("No valid swing direction");
    }
    direction.normalize();
    Vector3d player = new Vector3d((Vector3dc) transform.getPosition());
    Vector3d eye = new Vector3d(player).add(0, 1.6, 0);
    World world = ((EntityStore) store.getExternalData()).getWorld();
    double distance = BlockRaystep.clearDistance(world, eye, direction, range, 0.3, 0.3);
    if (!Double.isFinite(distance) || distance < MIN_ANCHOR_DISTANCE || distance >= range) {
      return AbilityResult.noTarget();
    }
    Vector3d anchor = new Vector3d(direction).mul(distance).add(eye);
    long now = clock.getAsLong();
    AtomicReference<Optional<ShadowSwingReconciliationController.StartOutcome>> result =
        new AtomicReference<>(Optional.empty());
    boolean dispatched = coordinator.withGameplay(store, gameplay -> result.set(
        gameplay.startShadowSwing(new ShadowSwingReconciliationController.StartRequest(
            context.casterUuid(), true,
            player.x, player.y, player.z,
            anchor.x, anchor.y, anchor.z,
            now))));
    if (!dispatched || result.get().isEmpty()) return AbilityResult.error("SHADOW_SWING store is unavailable");
    return switch (result.get().orElseThrow().status()) {
      case STARTED -> AbilityResult.success();
      case REATTACH_RATE_LIMIT -> AbilityResult.onCooldown(250L);
      case ANCHOR_OUT_OF_RANGE -> AbilityResult.noTarget();
      case INVALID_REQUEST, NOT_AUTHORIZED, CLOSED ->
          AbilityResult.error("SHADOW_SWING validation rejected the cast");
    };
  }

  private static boolean valid(CasterContext context, AbilityDefinition ability) {
    if (context == null || ability == null || !context.isPlayer() || context.casterUuid() == null) return false;
    Store<EntityStore> store = context.store();
    Ref<EntityStore> caster = context.caster();
    return store != null && caster != null && caster.isValid() && caster.getStore() == store;
  }

  private static boolean finite(Vector3d vector) {
    return vector != null && Double.isFinite(vector.x) && Double.isFinite(vector.y)
        && Double.isFinite(vector.z);
  }
}
