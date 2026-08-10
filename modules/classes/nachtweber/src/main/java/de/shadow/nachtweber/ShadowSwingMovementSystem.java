package de.shadow.nachtweber;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.system.PlayerVelocityInstructionSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;
import org.joml.Vector3d;

/** Applies only reconciliation-core corrections on Hytale's player-synchronised instruction path. */
final class ShadowSwingMovementSystem extends EntityTickingSystem<EntityStore> {
  private final NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator;
  private final LongSupplier clock;
  private final Set<Dependency<EntityStore>> dependencies = Set.of(
      new SystemDependency<>(Order.BEFORE, PlayerVelocityInstructionSystem.class));
  private Query<EntityStore> query;

  ShadowSwingMovementSystem(
      NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator,
      LongSupplier clock) {
    this.coordinator = Objects.requireNonNull(coordinator, "coordinator");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public void tick(
      float deltaTime,
      int index,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    if (store == null || chunk == null || commandBuffer == null
        || store.isShutdown() || !store.isInThread() || commandBuffer.getStore() != store) return;
    try {
      Ref<EntityStore> ref = chunk.getReferenceTo(index);
      PlayerRef player = chunk.getComponent(index, PlayerRef.getComponentType());
      TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
      Velocity velocity = chunk.getComponent(index, Velocity.getComponentType());
      if (ref == null || player == null || transform == null || velocity == null
          || !ref.isValid() || ref.getStore() != store || player.getUuid() == null
          || player.getReference() != ref) return;
      Vector3d position = transform.getPosition();
      Vector3d currentVelocity = velocity.getVelocity();
      if (!finite(position) || !finite(currentVelocity)) return;
      long nowMs = clock.getAsLong();
      if (nowMs < 0L) return;

      coordinator.withGameplay(store, gameplay -> gameplay.stepShadowSwing(
          new ShadowSwingReconciliationController.StepRequest(
              player.getUuid(),
              position.x, position.y, position.z,
              currentVelocity.x, currentVelocity.y, currentVelocity.z,
              nowMs)).ifPresent(outcome -> {
                if (outcome.status()
                    != ShadowSwingReconciliationController.StepStatus.CORRECTION) return;
                Vector3d correction = new Vector3d(
                    outcome.correctionX(), outcome.correctionY(), outcome.correctionZ());
                if (!finite(correction)) return;
                velocity.addInstruction(correction, null, ChangeVelocityType.Add);
              }));
    } catch (RuntimeException | LinkageError invalidRuntimeState) {
      // Missing/stale components or runtime teardown remain fail-closed for this entity.
    }
  }

  @Override
  public synchronized Query<EntityStore> getQuery() {
    if (query == null) {
      query = Query.and(
          PlayerRef.getComponentType(),
          TransformComponent.getComponentType(),
          Velocity.getComponentType());
    }
    return query;
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return dependencies;
  }

  private static boolean finite(Vector3d value) {
    return value != null
        && Double.isFinite(value.x)
        && Double.isFinite(value.y)
        && Double.isFinite(value.z);
  }
}
