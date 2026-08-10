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
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.ParticleUtil
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannMmoBridge;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannStormFurySystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final Map<UUID, ActiveBuff> ACTIVE = new ConcurrentHashMap<UUID, ActiveBuff>();
    private static final float EPSILON = 1.0E-4f;
    private static final long VISUAL_INTERVAL_MS = 450L;

    HymannStormFurySystem(HytaleLogger logger) {
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
    }

    static void activate(UUID playerUuid, Store<EntityStore> store, Ref<EntityStore> entity, long durationMs) {
        EntityStatMap stats = (EntityStatMap)store.getComponent(entity, EntityStatMap.getComponentType());
        long now = System.currentTimeMillis();
        ACTIVE.put(playerUuid, new ActiveBuff(now + durationMs, HymannStormFurySystem.readStat(stats, DefaultEntityStatTypes.getMana()), HymannStormFurySystem.readStat(stats, DefaultEntityStatTypes.getSignatureEnergy()), now));
    }

    static boolean isActive(UUID playerUuid) {
        ActiveBuff buff = ACTIVE.get(playerUuid);
        if (buff == null) {
            return false;
        }
        if (buff.expiresAtMs <= System.currentTimeMillis()) {
            ACTIVE.remove(playerUuid, buff);
            return false;
        }
        return true;
    }

    static void clear() {
        ACTIVE.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getPlayerRef() == null || player.getPlayerRef().getUuid() == null) {
            return;
        }
        PlayerRef playerRef = player.getPlayerRef();
        UUID playerUuid = playerRef.getUuid();
        ActiveBuff buff = ACTIVE.get(playerUuid);
        if (buff == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (buff.expiresAtMs <= now || !HymannMmoBridge.isHymann(playerUuid)) {
            ACTIVE.remove(playerUuid, buff);
            return;
        }
        Ref entity = chunk.getReferenceTo(index);
        EntityStatMap stats = (EntityStatMap)buffer.getComponent(entity, EntityStatMap.getComponentType());
        if (stats != null) {
            buff.lastMana = HymannStormFurySystem.amplifyPositiveGain(stats, DefaultEntityStatTypes.getMana(), buff.lastMana);
            buff.lastSignatureEnergy = HymannStormFurySystem.amplifyPositiveGain(stats, DefaultEntityStatTypes.getSignatureEnergy(), buff.lastSignatureEnergy);
        }
        if (now >= buff.nextVisualAtMs) {
            TransformComponent transform = (TransformComponent)buffer.getComponent(entity, TransformComponent.getComponentType());
            if (transform != null) {
                Vector3d ground = new Vector3d((Vector3dc)transform.getPosition()).add(0.0, -0.85, 0.0);
                ParticleUtil.spawnParticleEffect((String)"Mjolnir_Overcharged_Guard_Impact", (Vector3d)ground, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.65f, (float)0.0f, buffer);
                ParticleUtil.spawnParticleEffect((String)"Mjolnir_Ground_Big_Hit", (Vector3d)ground, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.42f, (float)0.0f, buffer);
            }
            buff.nextVisualAtMs = now + 450L;
        }
    }

    private static float amplifyPositiveGain(EntityStatMap stats, int statType, float previous) {
        if (statType < 0) {
            return Float.NaN;
        }
        EntityStatValue value = stats.get(statType);
        if (value == null) {
            return Float.NaN;
        }
        float current = value.get();
        if (Float.isFinite(previous) && current > previous + 1.0E-4f) {
            stats.addStatValue(statType, (current - previous) * HymannConfig.values().stormFuryExtraRegenMultiplier);
            EntityStatValue boosted = stats.get(statType);
            return boosted == null ? current : boosted.get();
        }
        return current;
    }

    private static float readStat(EntityStatMap stats, int statType) {
        if (stats == null || statType < 0) {
            return Float.NaN;
        }
        EntityStatValue value = stats.get(statType);
        return value == null ? Float.NaN : value.get();
    }

    private static final class ActiveBuff {
        private final long expiresAtMs;
        private float lastMana;
        private float lastSignatureEnergy;
        private long nextVisualAtMs;

        private ActiveBuff(long expiresAtMs, float lastMana, float lastSignatureEnergy, long nextVisualAtMs) {
            this.expiresAtMs = expiresAtMs;
            this.lastMana = lastMana;
            this.lastSignatureEnergy = lastSignatureEnergy;
            this.nextVisualAtMs = nextVisualAtMs;
        }
    }
}

