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
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Keeps the visual poison aura aligned with the selected primary class. */
final class SeuchenweberClassAuraSystem extends EntityTickingSystem<EntityStore> {
  private static final Query<EntityStore> PLAYERS = Query.and(Player.getComponentType());
  private static final long SYNC_INTERVAL_MS = 1_000L;
  private static final long CLIENT_READY_DELAY_MS = 2_000L;
  private static final long EXTERNAL_BONUS_TTL_MS = 1_500L;
  private static final String CRIT_DAMAGE_SOURCE = "seuchenweber_tree_crit_damage";
  private final Map<UUID, AuraSession> sessions = new HashMap<>();

  void register(ComponentRegistryProxy<EntityStore> registry) {
    registry.registerSystem((ISystem) this);
  }

  void clear() {
    sessions.clear();
  }

  @Override public Query<EntityStore> getQuery() { return PLAYERS; }

  @Override public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    Player player = chunk.getComponent(index, Player.getComponentType());
    PlayerRef playerRef = player == null ? null : player.getPlayerRef();
    UUID owner = playerRef == null ? null : playerRef.getUuid();
    if (owner == null) return;
    long nowMs = System.currentTimeMillis();
    AuraSession session = sessions.get(owner);
    if (session == null || session.playerRef() != playerRef) {
      sessions.put(owner, new AuraSession(playerRef, nowMs + CLIENT_READY_DELAY_MS, false));
      return;
    }
    if (nowMs < session.nextSyncAtMs()) return;
    boolean restartForClient = !session.clientSynchronized();
    sessions.put(owner, new AuraSession(playerRef, nowMs + SYNC_INTERVAL_MS, true));
    Ref<EntityStore> entity = chunk.getReferenceTo(index);
    boolean active = SeuchenweberClassDefinition.ID.equals(EndlessLevelingAPI.get().getPrimaryClassId(owner));
    SeuchenweberClassAuraVisuals.synchronize(store, entity, active, restartForClient);
    synchronizeClassBonuses(store, entity, owner, active, nowMs);
  }

  private static void synchronizeClassBonuses(Store<EntityStore> store, Ref<EntityStore> entity,
      UUID owner, boolean active, long nowMs) {
    double critDamage = active ? SeuchenweberTreeBonusResolver.claimedPercent(
        store, entity, SeuchenweberTreeBonusResolver.CRIT_DAMAGE) : 0.0;
    EndlessLevelingAPI.get().setExternalAttributeBonus(owner, "ferocity", CRIT_DAMAGE_SOURCE,
        critDamage, nowMs + (active ? EXTERNAL_BONUS_TTL_MS : 1L));
    if (!active) return;
    double staminaPercent = SeuchenweberTreeBonusResolver.claimedPercent(
        store, entity, SeuchenweberTreeBonusResolver.STAMINA_REGEN);
    if (staminaPercent <= 0.0) return;
    EntityStatMap stats = store.getComponent(entity, EntityStatMap.getComponentType());
    int staminaIndex = DefaultEntityStatTypes.getStamina();
    EntityStatValue stamina = stats == null || staminaIndex < 0 ? null : stats.get(staminaIndex);
    if (stamina == null || stamina.getMax() <= 0.0f) return;
    stats.addStatValue(staminaIndex, (float) (stamina.getMax() * staminaPercent / 100.0));
  }

  private record AuraSession(PlayerRef playerRef, long nextSyncAtMs, boolean clientSynchronized) { }
}
