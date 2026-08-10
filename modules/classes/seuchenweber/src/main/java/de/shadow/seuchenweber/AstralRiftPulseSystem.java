package de.shadow.seuchenweber;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.joml.Vector3d;

/** Applies bounded Nekrotoxin pulses at server-authoritative Astral Rift positions. */
final class AstralRiftPulseSystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> npcQuery;
  private final StoreScopedState<Store<EntityStore>, RuntimeState> states = new StoreScopedState<>();
  private final long intervalMs;
  private final NekrotoxinDamageSystem nekrotoxin;
  private final double radius;
  private final int maxTargets;
  private final int stacksPerPulse;
  private final long toxinDurationMs;

  AstralRiftPulseSystem(long intervalMs, NekrotoxinDamageSystem nekrotoxin, double radius,
      int maxTargets, int stacksPerPulse, long toxinDurationMs) {
    this.npcQuery = Query.and(NPCMarkerComponent.getComponentType());
    this.intervalMs = intervalMs;
    this.nekrotoxin = nekrotoxin;
    this.radius = radius;
    this.maxTargets = RiftPulseBudget.boundedTargetLimit(maxTargets);
    this.stacksPerPulse = stacksPerPulse;
    this.toxinDurationMs = toxinDurationMs;
  }

  void register(ComponentRegistryProxy<EntityStore> registry) {
    registry.registerSystem((ISystem) this);
  }

  void open(Store<EntityStore> store, UUID owner, Ref<EntityStore> caster,
      Vector3d position, long durationMs) {
    if (store == null || owner == null || caster == null || !caster.isValid()
        || caster.getStore() != store || position == null || durationMs <= 0L) return;
    RuntimeState state = stateFor(store);
    synchronized (state) {
      state.casterByOwner.put(owner, caster);
      state.ledger.open(owner, position.x, position.y, position.z, System.currentTimeMillis(), durationMs);
      state.nextLedgerCheckAtMs = state.ledger.nextEventAtMs();
    }
  }

  void release(Store<EntityStore> store) {
    states.remove(store, RuntimeState::clear);
  }

  void expireExpired(Store<EntityStore> store, long nowMs) {
    RuntimeState state = stateFor(store);
    synchronized (state) {
      for (UUID expiredOwner : state.ledger.expireOnly(nowMs)) {
        if (!state.ledger.hasOwner(expiredOwner)) state.casterByOwner.remove(expiredOwner);
      }
      state.nextLedgerCheckAtMs = state.ledger.nextEventAtMs();
    }
  }

  void clear() {
    states.clear(RuntimeState::clear);
  }

  @Override public Query<EntityStore> getQuery() {
    return npcQuery;
  }

  @Override public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    RuntimeState state = stateFor(store);
    synchronized (state) {
      long nowMs = System.currentTimeMillis();
      if (nowMs < state.nextLedgerCheckAtMs) return;
      var duePulses = state.ledger.consumeDue(nowMs);
      state.nextLedgerCheckAtMs = state.ledger.nextEventAtMs();
      for (UUID expiredOwner : state.ledger.drainExpiredOwners()) {
        if (!state.ledger.hasOwner(expiredOwner)) state.casterByOwner.remove(expiredOwner);
      }
      for (AstralRiftRuntimeLedger.RiftPulse pulse : duePulses) {
        Ref<EntityStore> caster = state.casterByOwner.get(pulse.owner());
        if (caster == null || !caster.isValid() || caster.getStore() != store) continue;
        int[] applied = {0};
        Selector.selectNearbyEntities(store, new Vector3d(pulse.x(), pulse.y(), pulse.z()), radius, target -> {
          if (applied[0] >= maxTargets || !SeuchenweberTargeting.isHostileCombatMob(target, caster, store)) return;
          nekrotoxin.apply(store, target, caster, pulse.owner(), stacksPerPulse, toxinDurationMs);
          applied[0]++;
        }, target -> target != null && target.isValid() && target.getStore() == store);
      }
    }
  }

  private RuntimeState stateFor(Store<EntityStore> store) {
    return states.getOrCreate(store, ignored -> new RuntimeState(intervalMs));
  }

  private static final class RuntimeState {
    private final AstralRiftRuntimeLedger ledger;
    private final Map<UUID, Ref<EntityStore>> casterByOwner = new HashMap<>();
    private long nextLedgerCheckAtMs = Long.MAX_VALUE;

    private RuntimeState(long intervalMs) {
      ledger = new AstralRiftRuntimeLedger(intervalMs);
    }

    private void clear() {
      synchronized (this) {
        ledger.clear();
        casterByOwner.clear();
        nextLedgerCheckAtMs = Long.MAX_VALUE;
      }
    }
  }
}
