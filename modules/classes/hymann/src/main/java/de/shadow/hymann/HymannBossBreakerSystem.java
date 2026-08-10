/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.airijko.endlessleveling.bossframework.BossBarRegistry
 *  com.airijko.endlessleveling.bossframework.BossBarRegistry$ActiveBoss
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
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.ParticleUtil
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.bossframework.BossBarRegistry;
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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannTreeRewards;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannBossBreakerSystem
extends EntityTickingSystem<EntityStore> {
    private static final String LIGHTNING_PARTICLE = "Mjolnir_Lightning_Strike";
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final long SCAN_INTERVAL_MS = 500L;
    private static final long DEBUFF_GRACE_MS = 1250L;
    private static final long VISUAL_RENEWAL_MS = 5000L;
    private static final Map<Ref<EntityStore>, Long> ACTIVE_BOSSES = new ConcurrentHashMap<Ref<EntityStore>, Long>();
    private static final Map<Ref<EntityStore>, Long> NEXT_VISUAL_AT = new ConcurrentHashMap<Ref<EntityStore>, Long>();
    private final HytaleLogger logger;
    private final Map<UUID, Long> nextScanAt = new ConcurrentHashMap<UUID, Long>();

    HymannBossBreakerSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann boss-defense aura");
    }

    void clear() {
        this.nextScanAt.clear();
        ACTIVE_BOSSES.clear();
        NEXT_VISUAL_AT.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        UUID playerUuid;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        UUID uUID = playerUuid = playerRef == null ? null : playerRef.getUuid();
        if (playerUuid == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long next = this.nextScanAt.get(playerUuid);
        if (next != null && now < next) {
            return;
        }
        this.nextScanAt.put(playerUuid, now + 500L);
        ACTIVE_BOSSES.entrySet().removeIf(entry -> (Long)entry.getValue() < now || !((Ref)entry.getKey()).isValid());
        NEXT_VISUAL_AT.entrySet().removeIf(entry -> !((Ref)entry.getKey()).isValid());
        Ref hymann = chunk.getReferenceTo(index);
        if (!HymannBossBreakerSystem.isAuraEquipped(playerUuid, (Ref<EntityStore>)hymann, store)) {
            return;
        }
        TransformComponent hymannTransform = (TransformComponent)buffer.getComponent(hymann, TransformComponent.getComponentType());
        if (hymannTransform == null) {
            return;
        }
        Vector3d center = hymannTransform.getPosition();
        double radiusSquared = Math.pow(HymannConfig.values().bossAuraRadiusBlocks, 2.0);
        for (BossBarRegistry.ActiveBoss activeBoss : BossBarRegistry.get().all()) {
            TransformComponent bossTransform;
            Ref boss = activeBoss == null ? null : activeBoss.ref;
            if (boss == null || !boss.isValid() || boss.getStore() != store || (bossTransform = (TransformComponent)buffer.getComponent(boss, TransformComponent.getComponentType())) == null || bossTransform.getPosition().distanceSquared((Vector3dc)center) > radiusSquared) continue;
            ACTIVE_BOSSES.put((Ref<EntityStore>)boss, now + 1250L);
            long nextVisual = NEXT_VISUAL_AT.getOrDefault(boss, 0L);
            if (now < nextVisual) continue;
            NEXT_VISUAL_AT.put((Ref<EntityStore>)boss, now + 5000L);
            Vector3d strike = new Vector3d((Vector3dc)bossTransform.getPosition()).add(0.0, 5.0, 0.0);
            ParticleUtil.spawnParticleEffect((String)LIGHTNING_PARTICLE, (Vector3d)strike, (float)0.0f, (float)0.0f, (float)0.0f, (float)1.15f, (float)0.0f, buffer);
        }
    }

    static boolean isDefenseBroken(Ref<EntityStore> target) {
        if (target == null) {
            return false;
        }
        Long expiry = ACTIVE_BOSSES.get(target);
        if (expiry == null) {
            return false;
        }
        if (expiry >= System.currentTimeMillis() && target.isValid()) {
            return true;
        }
        ACTIVE_BOSSES.remove(target, expiry);
        return false;
    }

    static float bossDamageTakenMultiplier() {
        return HymannConfig.values().bossAuraDamageTakenMultiplier;
    }

    private static boolean isAuraEquipped(UUID playerUuid, Ref<EntityStore> entity, Store<EntityStore> store) {
        if (!(HymannMmoBridge.isHymann(playerUuid) && entity != null && entity.isValid() && HymannTreeRewards.hasPassive(MMOSkillTreeAPI.getSkillComponent(store, entity), "hymann_passive_stormbreaker_aura"))) {
            return false;
        }
        ItemStack mainHand = InventoryComponent.getItemInHand(store, entity);
        if (mainHand == null || !HymannAccess.isMjolnir(mainHand.getItemId())) {
            return false;
        }
        return HymannAccess.inspectInventory(entity, store).hasShield();
    }
}

