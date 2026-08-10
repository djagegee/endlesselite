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
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.MMOSkillTreePlugin
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
 *  com.ziggfreed.mmoskilltree.player.PlayerDataDomain
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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.MMOSkillTreePlugin;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import com.ziggfreed.mmoskilltree.player.PlayerDataDomain;
import de.shadow.hymann.HymannMmoBridge;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class HymannSkillLifecycleSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final long HYDRATION_GRACE_MS = 3000L;
    private final HytaleLogger logger;
    private final Map<UUID, State> states = new ConcurrentHashMap<UUID, State>();

    HymannSkillLifecycleSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann MMO skill lifecycle validation");
    }

    void clear() {
        this.states.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        State state;
        UUID playerUuid;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        UUID uUID = playerUuid = playerRef == null ? null : playerRef.getUuid();
        if (playerUuid == null) {
            return;
        }
        if (!HymannMmoBridge.isHymann(playerUuid)) {
            this.states.remove(playerUuid);
            return;
        }
        Ref entity = chunk.getReferenceTo(index);
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, (Ref)entity);
        if (skills == null) {
            return;
        }
        long now = System.currentTimeMillis();
        State state2 = state = this.states.computeIfAbsent(playerUuid, ignored -> new State());
        synchronized (state2) {
            if (state.component != skills || state.storeIdentity != store) {
                state.component = skills;
                state.storeIdentity = store;
                state.validateAtMs = now + 3000L;
                state.validatedComponent = null;
                return;
            }
            if (state.validatedComponent == skills || now < state.validateAtMs) {
                return;
            }
            int restored = HymannMmoBridge.ensureMilestoneAbilities(store, (Ref<EntityStore>)entity);
            state.validatedComponent = skills;
            if (restored > 0) {
                MMOSkillTreePlugin.getInstance().players().markDirty(store, entity, PlayerDataDomain.SKILLS);
                ((HytaleLogger.Api)this.logger.atInfo()).log("Reconciled %s earned Hymann milestone(s) for %s after profile hydration", restored, (Object)playerUuid);
            }
        }
    }

    private static final class State {
        private SkillComponent component;
        private SkillComponent validatedComponent;
        private Store<EntityStore> storeIdentity;
        private long validateAtMs;

        private State() {
        }
    }
}

