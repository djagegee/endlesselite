/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.airijko.endlessleveling.api.EndlessLevelingAPI
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.dependency.Dependency
 *  com.hypixel.hytale.component.dependency.Order
 *  com.hypixel.hytale.component.dependency.SystemDependency
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.server.core.entity.InteractionManager
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent
 *  com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$EntitySource
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$Source
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems$ApplyDamage
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.modules.interaction.InteractionModule
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.ParticleUtil
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannStunService;
import de.shadow.hymann.HymannTargeting;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannGuardAuraSystem
extends DamageEventSystem {
    private static final String LIGHTNING_PARTICLE = "Mjolnir_Lightning_Strike";
    private static final Set<String> CAPTAIN_GUARD_INTERACTIONS = Set.of("ShieldCap_Secondary_Guard_Wield", "ShieldCapLeft_Secondary_Guard_Wield", "ShieldCapLeft_Mjolnir_Secondary_Guard_Wield");
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final Query<EntityStore> ENEMIES = Query.and((Query[])new Query[]{NPCMarkerComponent.getComponentType(), TransformComponent.getComponentType(), EntityStatMap.getComponentType()});
    private final HytaleLogger logger;
    private final Map<UUID, Long> lastTriggeredAt = new ConcurrentHashMap<UUID, Long>();

    HymannGuardAuraSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann shield-block thunder aura");
    }

    boolean activateOnGuardStart(PlayerRef playerRef, Ref<EntityStore> playerEntity, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        if (!this.consumeManaAndStartCooldown(playerRef, playerEntity, buffer)) {
            return false;
        }
        TransformComponent transform = (TransformComponent)buffer.getComponent(playerEntity, TransformComponent.getComponentType());
        if (transform == null) {
            return false;
        }
        Vector3d center = new Vector3d((Vector3dc)transform.getPosition());
        int ascension = HymannGuardAuraSystem.ascensionTier(playerRef.getUuid());
        HymannConfig.Values config = HymannConfig.values();
        float healing = config.guardAuraHealBase + (float)ascension * config.guardAuraHealPerAscension;
        ParticleUtil.spawnParticleEffect((String)LIGHTNING_PARTICLE, (Vector3d)new Vector3d((Vector3dc)center).add(0.0, 7.0, 0.0), (float)0.0f, (float)0.0f, (float)0.0f, (float)2.25f, (float)0.0f, buffer);
        HymannGuardAuraSystem.healNearbyPlayers(store, buffer, playerRef, center, config.guardAuraRadius, healing);
        HymannGuardAuraSystem.stunNearbyEnemies(store, playerEntity, center, config.guardAuraRadius, config.guardAuraStunMs);
        return true;
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public Set<Dependency<EntityStore>> getDependencies() {
        HashSet<Dependency<EntityStore>> dependencies = new HashSet<>();
        dependencies.add(new SystemDependency(Order.BEFORE, DamageSystems.ApplyDamage.class));
        try {
            Class<?> captainGuard = Class.forName("co.carrd.starkymods.interactions.ShieldCapGuardFallDamageReductionSystem");
            dependencies.add(new SystemDependency(Order.BEFORE, captainGuard));
            ((HytaleLogger.Api)this.logger.atInfo()).log("Ordered Hymann block aura before Captain shield damage handling");
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        return Set.copyOf(dependencies);
    }

    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Damage incomingDamage) {
        if (incomingDamage == null) {
            return;
        }
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getPlayerRef() == null) {
            return;
        }
        PlayerRef playerRef = player.getPlayerRef();
        Ref playerEntity = chunk.getReferenceTo(index);
        this.triggerFromGuard(playerRef, (Ref<EntityStore>)playerEntity, store, buffer);
    }

    boolean triggerFromGuard(PlayerRef playerRef, Ref<EntityStore> playerEntity, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        if (!HymannGuardAuraSystem.isCaptainShieldGuarding(playerEntity, buffer) || !this.consumeManaAndStartCooldown(playerRef, playerEntity, buffer)) {
            return false;
        }
        TransformComponent transform = (TransformComponent)buffer.getComponent(playerEntity, TransformComponent.getComponentType());
        if (transform == null) {
            return false;
        }
        Vector3d center = new Vector3d((Vector3dc)transform.getPosition());
        int ascension = HymannGuardAuraSystem.ascensionTier(playerRef.getUuid());
        HymannConfig.Values config = HymannConfig.values();
        float healing = config.guardAuraHealBase + (float)ascension * config.guardAuraHealPerAscension;
        float thunderDamage = config.guardAuraDamageBase + (float)ascension * config.guardAuraDamagePerAscension;
        ParticleUtil.spawnParticleEffect((String)LIGHTNING_PARTICLE, (Vector3d)new Vector3d((Vector3dc)center).add(0.0, 7.0, 0.0), (float)0.0f, (float)0.0f, (float)0.0f, (float)2.25f, (float)0.0f, buffer);
        HymannGuardAuraSystem.healNearbyPlayers(store, buffer, playerRef, center, config.guardAuraRadius, healing);
        HymannGuardAuraSystem.strikeNearbyEnemies(store, playerEntity, center, config.guardAuraRadius, thunderDamage, config.guardAuraStunMs);
        return true;
    }

    private boolean consumeManaAndStartCooldown(PlayerRef playerRef, Ref<EntityStore> playerEntity, CommandBuffer<EntityStore> buffer) {
        EntityStatValue mana;
        if (playerRef == null || playerRef.getUuid() == null || playerEntity == null || !playerEntity.isValid() || !HymannMmoBridge.isHymann(playerRef.getUuid())) {
            return false;
        }
        EntityStatMap stats = (EntityStatMap)buffer.getComponent(playerEntity, EntityStatMap.getComponentType());
        EntityStatValue entityStatValue = mana = stats == null ? null : stats.get(DefaultEntityStatTypes.getMana());
        if (mana == null) {
            return false;
        }
        HymannConfig.Values config = HymannConfig.values();
        float manaCost = Math.max(config.guardAuraMinManaCost, mana.getMax() * config.guardAuraManaCostPercent);
        if (mana.get() < manaCost || !this.tryConsumeCooldown(playerRef.getUuid())) {
            return false;
        }
        stats.subtractStatValue(DefaultEntityStatTypes.getMana(), manaCost);
        return true;
    }

    private boolean tryConsumeCooldown(UUID playerUuid) {
        long now = System.currentTimeMillis();
        Long previous = this.lastTriggeredAt.get(playerUuid);
        if (previous != null && now - previous < HymannConfig.values().guardAuraCooldownMs) {
            return false;
        }
        this.lastTriggeredAt.put(playerUuid, now);
        return true;
    }

    static boolean isCaptainShieldGuarding(Ref<EntityStore> entity, CommandBuffer<EntityStore> buffer) {
        InteractionManager manager = (InteractionManager)buffer.getComponent(entity, InteractionModule.get().getInteractionManagerComponent());
        if (manager == null) {
            return false;
        }
        return (Boolean)manager.forEachInteraction((chain, interaction, found) -> {
            if (Boolean.TRUE.equals(found)) {
                return Boolean.TRUE;
            }
            String id = interaction == null ? null : interaction.getId();
            return HymannGuardAuraSystem.isCaptainGuardInteraction(id) ? Boolean.TRUE : Boolean.FALSE;
        }, (Object)Boolean.FALSE);
    }

    private static boolean isCaptainGuardInteraction(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        if (CAPTAIN_GUARD_INTERACTIONS.contains(id)) {
            return true;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        return normalized.contains("shieldcap") && normalized.contains("guard");
    }

    private static void healNearbyPlayers(Store<EntityStore> store, CommandBuffer<EntityStore> buffer, PlayerRef defender, Vector3d center, double radius, float amount) {
        World world = ((EntityStore)store.getExternalData()).getWorld();
        double radiusSquared = radius * radius;
        for (PlayerRef ally : world.getPlayerRefs()) {
            EntityStatMap stats;
            TransformComponent transform;
            Ref allyEntity = ally.getReference();
            if (allyEntity == null || !allyEntity.isValid() || (transform = (TransformComponent)buffer.getComponent(allyEntity, TransformComponent.getComponentType())) == null || transform.getPosition().distanceSquared((Vector3dc)center) > radiusSquared || (stats = (EntityStatMap)buffer.getComponent(allyEntity, EntityStatMap.getComponentType())) == null) continue;
            stats.addStatValue(DefaultEntityStatTypes.getHealth(), amount);
        }
    }

    private static void stunNearbyEnemies(Store<EntityStore> store, Ref<EntityStore> defender, Vector3d center, double radius, long stunMs) {
        double radiusSquared = radius * radius;
        store.forEachChunk(ENEMIES, (enemyChunk, enemyBuffer) -> {
            for (int index = 0; index < enemyChunk.size(); ++index) {
                Ref enemy;
                TransformComponent transform = (TransformComponent)enemyChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null || transform.getPosition().distanceSquared((Vector3dc)center) > radiusSquared || !HymannTargeting.isHostileCombatMob((Ref<EntityStore>)(enemy = enemyChunk.getReferenceTo(index)), defender, store)) continue;
                HymannStunService.apply(store, (Ref<EntityStore>)enemy, stunMs, defender);
            }
        });
    }

    private static void strikeNearbyEnemies(Store<EntityStore> store, Ref<EntityStore> defender, Vector3d center, double radius, float amount, long stunMs) {
        double radiusSquared = radius * radius;
        store.forEachChunk(ENEMIES, (enemyChunk, enemyBuffer) -> {
            for (int index = 0; index < enemyChunk.size(); ++index) {
                Ref enemy;
                TransformComponent transform = (TransformComponent)enemyChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null || transform.getPosition().distanceSquared((Vector3dc)center) > radiusSquared || !HymannTargeting.isHostileCombatMob((Ref<EntityStore>)(enemy = enemyChunk.getReferenceTo(index)), defender, store)) continue;
                Vector3d position = new Vector3d((Vector3dc)transform.getPosition());
                ParticleUtil.spawnParticleEffect((String)LIGHTNING_PARTICLE, (Vector3d)new Vector3d((Vector3dc)position).add(0.0, 5.0, 0.0), (float)0.0f, (float)0.0f, (float)0.0f, (float)1.15f, (float)0.0f, (ComponentAccessor)enemyBuffer);
                HymannStunService.apply(store, (Ref<EntityStore>)enemy, stunMs, defender);
                Damage thunder = new Damage((Damage.Source)new Damage.EntitySource(defender), DamageCause.PHYSICAL, amount);
                thunder.putMetaObject(Damage.KNOCKBACK_COMPONENT, HymannGuardAuraSystem.knockback(center, position));
                DamageSystems.executeDamage((Ref)enemy, (CommandBuffer)enemyBuffer, (Damage)thunder);
            }
        });
    }

    private static KnockbackComponent knockback(Vector3d center, Vector3d target) {
        Vector3d direction = new Vector3d((Vector3dc)target).sub((Vector3dc)center);
        direction.y = 0.0;
        if (direction.lengthSquared() < 0.001) {
            direction.set(0.0, 0.0, 1.0);
        } else {
            direction.normalize();
        }
        direction.mul(9.0).add(0.0, 4.5, 0.0);
        KnockbackComponent knockback = new KnockbackComponent();
        knockback.setVelocity(direction);
        knockback.setDuration(0.45f);
        return knockback;
    }

    private static int ascensionTier(UUID playerUuid) {
        String classId;
        return switch (classId = EndlessClassId.get(playerUuid)) {
            case "hymann_thunderforged" -> 2;
            case "hymann_aegis_vanguard" -> 3;
            case "hymann_skybreaker" -> 4;
            case "hymann_eternal_paragon" -> 5;
            default -> 1;
        };
    }

    private static final class EndlessClassId {
        private EndlessClassId() {
        }

        private static String get(UUID playerUuid) {
            return EndlessLevelingAPI.get().getPrimaryClassId(playerUuid);
        }
    }
}

