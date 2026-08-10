package de.shadow.nachtweber;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/** Executes due owner-isolated venom ticks against real NPC refs in the exact world store. */
final class VenomDamageSystem extends EntityTickingSystem<EntityStore> {
  private final NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator;
  private final OwnerEntityBindingRegistry<Store<EntityStore>, Ref<EntityStore>> owners;
  private final LongSupplier clock;
  private Query<EntityStore> query;

  VenomDamageSystem(
      NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator,
      OwnerEntityBindingRegistry<Store<EntityStore>, Ref<EntityStore>> owners,
      LongSupplier clock) {
    this.coordinator = coordinator;
    this.owners = owners;
    this.clock = clock;
  }

  @Override public synchronized Query<EntityStore> getQuery() {
    if (query == null) query = Query.and(NPCEntity.getComponentType());
    return query;
  }

  @Override public void tick(
      float deltaSeconds,
      int index,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    if (store == null || commandBuffer == null || commandBuffer.getStore() != store
        || !store.isInThread()) return;
    Ref<EntityStore> target = chunk.getReferenceTo(index);
    if (target == null || !target.isValid() || target.getStore() != store) return;
    long now = clock.getAsLong();
    if (now < 0L) return;
    AtomicReference<List<UUID>> active = new AtomicReference<>(List.of());
    if (!coordinator.withGameplay(store,
        gameplay -> active.set(gameplay.activeVenomOwners(target.getIndex(), now)))) return;
    for (UUID owner : active.get()) {
      Ref<EntityStore> ownerRef = owners.lookup(store, owner);
      if (ownerRef == null || !ownerRef.isValid() || ownerRef.getStore() != store) {
        owners.removeOwner(store, owner);
        continue;
      }
      coordinator.withGameplay(store, gameplay -> gameplay.tickVenom(
          target.getIndex(), owner, now,
          new EndlessLevelingVenomDamageAdapter(
              owner, target.getIndex(), ownerRef, target, commandBuffer)));
    }
  }
}
