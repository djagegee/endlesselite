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
 *  com.hypixel.hytale.protocol.InteractionType
 *  com.hypixel.hytale.server.core.entity.InteractionContext
 *  com.hypixel.hytale.server.core.entity.InteractionManager
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.modules.interaction.InteractionModule
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
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
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.shadow.hymann.HymannGuardAuraSystem;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class HymannGuardAuraTickSystem
extends EntityTickingSystem<EntityStore> {
    private static final String ULTIMATE_ROOT_ID = "Root_Hymann_Block_Ultimate";
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private final HytaleLogger logger;
    private final HymannGuardAuraSystem aura;
    private final Set<UUID> guardingPlayers = ConcurrentHashMap.newKeySet();

    HymannGuardAuraTickSystem(HytaleLogger logger, HymannGuardAuraSystem aura) {
        this.logger = logger;
        this.aura = aura;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann guard-start ultimate trigger");
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        Ref playerEntity = chunk.getReferenceTo(index);
        if (playerRef == null || playerRef.getUuid() == null || playerEntity == null || !playerEntity.isValid()) {
            return;
        }
        UUID uuid = playerRef.getUuid();
        if (!HymannGuardAuraSystem.isCaptainShieldGuarding((Ref<EntityStore>)playerEntity, buffer)) {
            this.guardingPlayers.remove(uuid);
            return;
        }
        if (!this.guardingPlayers.add(uuid)) {
            return;
        }
        RootInteraction ultimate = (RootInteraction)RootInteraction.getAssetMap().getAsset(ULTIMATE_ROOT_ID);
        InteractionManager manager = (InteractionManager)buffer.getComponent(playerEntity, InteractionModule.get().getInteractionManagerComponent());
        if (ultimate == null || manager == null) {
            ((HytaleLogger.Api)this.logger.atWarning()).log("Hymann block ultimate root interaction was unavailable");
            return;
        }
        if (!this.aura.activateOnGuardStart(playerRef, (Ref<EntityStore>)playerEntity, store, buffer)) {
            return;
        }
        InteractionContext context = InteractionContext.forInteraction((InteractionManager)manager, (Ref)playerEntity, (InteractionType)InteractionType.EntityStatEffect, buffer);
        manager.startChain(playerEntity, buffer, InteractionType.EntityStatEffect, context, ultimate);
    }
}

