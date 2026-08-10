package de.shadow.seuchenweber;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Passive Pesthauch: applies one owner-bound Nekrotoxin stack to every hostile in range per pulse. */
final class PesthauchAuraSystem extends EntityTickingSystem<EntityStore> {
  static final double DEFAULT_RADIUS_BLOCKS = 8.0;
  static final long DEFAULT_PULSE_INTERVAL_MS = 2_000L;
  private static final Query<EntityStore> PLAYERS = Query.and(Player.getComponentType());

  private final NekrotoxinDamageSystem nekrotoxin;
  private final long durationMs;
  private final double radiusBlocks;
  private final long pulseIntervalMs;
  private final StoreScopedState<Store<EntityStore>, OwnerIntervalBudget> pulseBudgets =
      new StoreScopedState<>();
  private volatile boolean active;

  PesthauchAuraSystem(NekrotoxinDamageSystem nekrotoxin, long durationMs,
      double radiusBlocks, long pulseIntervalMs) {
    this.nekrotoxin = nekrotoxin;
    this.durationMs = durationMs;
    this.radiusBlocks = Double.isFinite(radiusBlocks) && radiusBlocks > 0.0
        ? radiusBlocks : DEFAULT_RADIUS_BLOCKS;
    this.pulseIntervalMs = pulseIntervalMs > 0L ? pulseIntervalMs : DEFAULT_PULSE_INTERVAL_MS;
  }

  void register(ComponentRegistryProxy<EntityStore> registry) {
    if (active) return;
    active = true;
    registry.registerSystem((ISystem) this);
  }

  void release(Store<EntityStore> store) {
    pulseBudgets.remove(store, OwnerIntervalBudget::clear);
  }

  void clear() {
    active = false;
    pulseBudgets.clear(OwnerIntervalBudget::clear);
  }

  @Override public Query<EntityStore> getQuery() {
    return PLAYERS;
  }

  @Override public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    if (!active || store == null || durationMs <= 0L) return;
    Player player = chunk.getComponent(index, Player.getComponentType());
    PlayerRef playerRef = player == null ? null : player.getPlayerRef();
    UUID owner = playerRef == null ? null : playerRef.getUuid();
    Ref<EntityStore> caster = chunk.getReferenceTo(index);
    if (owner == null || !belongsToStore(caster, store)) return;
    if (!SeuchenweberClassDefinition.ID.equals(EndlessLevelingAPI.get().getPrimaryClassId(owner))
        || !MmoPassiveResolver.hasUnlocked(
            store, caster, MmoPassiveResolver.NECROTOXIC_MASTERY_ID)) return;

    TransformComponent transform = buffer.getComponent(caster, TransformComponent.getComponentType());
    if (transform == null) return;
    OwnerIntervalBudget budget = pulseBudgets.getOrCreate(
        store, ignored -> new OwnerIntervalBudget(pulseIntervalMs));
    synchronized (budget) {
      if (!budget.tryClaim(owner, System.currentTimeMillis())) return;
    }

    Vector3d center = new Vector3d((Vector3dc) transform.getPosition());
    Selector.selectNearbyEntities(store, center, radiusBlocks, target -> {
      if (!SeuchenweberTargeting.isHostileCombatMob(target, caster, store)) return;
      nekrotoxin.apply(store, target, caster, owner, 1, durationMs);
    }, target -> target != null && target.isValid() && target.getStore() == store
        && target.getIndex() != caster.getIndex());
  }

  private static boolean belongsToStore(Ref<EntityStore> reference, Store<EntityStore> store) {
    return reference != null && store != null && reference.isValid() && reference.getStore() == store;
  }
}
