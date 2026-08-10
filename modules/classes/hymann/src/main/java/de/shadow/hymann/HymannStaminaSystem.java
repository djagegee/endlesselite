/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.component.system.tick.EntityTickingSystem
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.inventory.InventoryComponent
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannTreeRewards;

final class HymannStaminaSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private final HytaleLogger logger;

    HymannStaminaSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann high-speed stamina regeneration");
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        EntityStatValue stamina;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getPlayerRef() == null) {
            return;
        }
        PlayerRef playerRef = player.getPlayerRef();
        if (playerRef.getUuid() == null || !HymannMmoBridge.isHymann(playerRef.getUuid())) {
            return;
        }
        Ref entity = chunk.getReferenceTo(index);
        ItemStack heldItem = InventoryComponent.getItemInHand(store, (Ref)entity);
        if (heldItem == null || !HymannAccess.isArmament(heldItem.getItemId())) {
            return;
        }
        EntityStatMap stats = (EntityStatMap)buffer.getComponent(entity, EntityStatMap.getComponentType());
        EntityStatValue entityStatValue = stamina = stats == null ? null : stats.get(DefaultEntityStatTypes.getStamina());
        if (stamina == null || stamina.get() >= stamina.getMax()) {
            return;
        }
        float elapsed = Math.min(0.25f, Math.max(0.0f, deltaTime));
        double treeRegen = HymannTreeRewards.staminaRegenPercent(MMOSkillTreeAPI.getSkillComponent(store, (Ref)entity)) / 100.0;
        stats.addStatValue(DefaultEntityStatTypes.getStamina(), (float)((double)stamina.getMax() * ((double)HymannConfig.values().extraStaminaRegenPerSecond + treeRegen) * (double)elapsed));
    }
}

