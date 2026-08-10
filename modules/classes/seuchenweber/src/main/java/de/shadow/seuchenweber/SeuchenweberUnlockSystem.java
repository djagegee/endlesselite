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
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Persistently grants Seuchenweber access after the item + prestige contract is met. */
final class SeuchenweberUnlockSystem extends EntityTickingSystem<EntityStore> {
  private static final Query<EntityStore> PLAYERS = Query.and(Player.getComponentType());
  private static final long CHECK_INTERVAL_MS = 2_000L;
  private final Map<UUID, Long> nextCheckAtMs = new HashMap<>();
  private final HytaleLogger logger;

  SeuchenweberUnlockSystem(HytaleLogger logger) {
    this.logger = logger;
  }

  void register(ComponentRegistryProxy<EntityStore> registry) {
    registry.registerSystem((ISystem) this);
  }

  void clear() {
    nextCheckAtMs.clear();
  }

  @Override public Query<EntityStore> getQuery() {
    return PLAYERS;
  }

  @Override public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    Player player = chunk.getComponent(index, Player.getComponentType());
    PlayerRef playerRef = player == null ? null : player.getPlayerRef();
    UUID owner = playerRef == null ? null : playerRef.getUuid();
    if (owner == null || playerRef.hasPermission(SeuchenweberPlugin.PERMISSION)) return;
    long nowMs = System.currentTimeMillis();
    if (nowMs < nextCheckAtMs.getOrDefault(owner, 0L)) return;
    nextCheckAtMs.put(owner, nowMs + CHECK_INTERVAL_MS);

    int prestige = EndlessLevelingAPI.get().getPlayerPrestigeLevel(owner);
    if (prestige < SeuchenweberUnlockRules.REQUIRED_PRESTIGE) return;
    Ref<EntityStore> playerEntity = chunk.getReferenceTo(index);
    if (playerEntity == null || !playerEntity.isValid() || playerEntity.getStore() != store
        || !hasUnlockItem(playerEntity, store)) return;
    PermissionsModule permissions = PermissionsModule.get();
    if (permissions == null) return;
    permissions.addUserPermission(owner, Set.of(SeuchenweberPlugin.PERMISSION));
    nextCheckAtMs.remove(owner);
    playerRef.sendMessage(Message.raw(
        "Seuchenweber freigeschaltet: Prestige 30 und das Cosmic Ruin Spellbook wurden bestätigt.")
        .color("#9f7aea"));
    ((HytaleLogger.Api) logger.atInfo()).log(
        "Granted Seuchenweber access after Prestige 30 + Cosmic Ruin Spellbook validation");
  }

  static boolean hasUnlockItem(Ref<EntityStore> playerEntity, Store<EntityStore> store) {
    if (playerEntity == null || store == null || !playerEntity.isValid() || playerEntity.getStore() != store) return false;
    CombinedItemContainer inventory = InventoryComponent.getCombined(
        store, playerEntity, InventoryComponent.EVERYTHING);
    if (inventory == null) return false;
    for (short slot = 0; slot < inventory.getCapacity(); slot++) {
      ItemStack stack = inventory.getItemStack(slot);
      if (stack != null && !stack.isEmpty()
          && SeuchenweberUnlockRules.isUnlockItem(stack.getItemId())) return true;
    }
    return false;
  }
}
