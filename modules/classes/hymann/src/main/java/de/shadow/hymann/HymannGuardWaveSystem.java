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
 *  com.hypixel.hytale.server.core.entity.InteractionManager
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$EntitySource
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$Source
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.modules.interaction.InteractionModule
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil
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
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannDamageScaling;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannStunService;
import de.shadow.hymann.HymannTargeting;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannGuardWaveSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final String CAST_EFFECT = "Hymann_Thunder_Step_Arrival";
    private static final String FROST_CHILL_EFFECT = "Endgame_Frost_Chill";
    private static final float FROST_CHILL_SECONDS = 5.0f;
    private final HytaleLogger logger;
    private final Set<UUID> activePowerSwings = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastCastAt = new ConcurrentHashMap<UUID, Long>();

    HymannGuardWaveSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann Mjolnir + Captain Shield Guard Bash wave");
    }

    void clear() {
        this.activePowerSwings.clear();
        this.lastCastAt.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        Ref caster = chunk.getReferenceTo(index);
        if (playerRef == null || playerRef.getUuid() == null || caster == null || !caster.isValid()) {
            return;
        }
        UUID uuid = playerRef.getUuid();
        if (!HymannGuardWaveSystem.isMjolnirGuardAttackActive((Ref<EntityStore>)caster, buffer)) {
            this.activePowerSwings.remove(uuid);
            return;
        }
        if (!this.activePowerSwings.add(uuid)) {
            return;
        }
        this.castIfAllowed(uuid, (Ref<EntityStore>)caster, store, buffer);
    }

    private static boolean isMjolnirGuardAttackActive(Ref<EntityStore> caster, CommandBuffer<EntityStore> buffer) {
        InteractionManager manager = (InteractionManager)buffer.getComponent(caster, InteractionModule.get().getInteractionManagerComponent());
        if (manager == null) {
            return false;
        }
        return (Boolean)manager.forEachInteraction((chain, interaction, found) -> {
            String id;
            if (Boolean.TRUE.equals(found)) {
                return Boolean.TRUE;
            }
            String string = id = interaction == null ? null : interaction.getId();
            if (id == null) {
                return Boolean.FALSE;
            }
            String normalized = id.toLowerCase(Locale.ROOT);
            return normalized.contains("shieldcapleft_mjolnir_guard_bash") || normalized.contains("mjolnir_powerswing") ? Boolean.TRUE : Boolean.FALSE;
        }, (Object)Boolean.FALSE);
    }

    private void castIfAllowed(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        EntityStatValue mana;
        if (!HymannMmoBridge.isHymann(uuid) || !HymannAccess.inspectInventory(caster, store).hasBothRelics()) {
            return;
        }
        HymannConfig.Values config = HymannConfig.values();
        long now = System.currentTimeMillis();
        Long previous = this.lastCastAt.get(uuid);
        if (previous != null && now - previous < config.guardWaveCooldownMs) {
            return;
        }
        EntityStatMap stats = (EntityStatMap)buffer.getComponent(caster, EntityStatMap.getComponentType());
        EntityStatValue entityStatValue = mana = stats == null ? null : stats.get(DefaultEntityStatTypes.getMana());
        if (mana == null || mana.get() < (float)config.guardWaveManaCost) {
            return;
        }
        TransformComponent transform = (TransformComponent)buffer.getComponent(caster, TransformComponent.getComponentType());
        if (transform == null) {
            return;
        }
        this.lastCastAt.put(uuid, now);
        stats.subtractStatValue(DefaultEntityStatTypes.getMana(), (float)config.guardWaveManaCost);
        Vector3d center = new Vector3d((Vector3dc)transform.getPosition());
        AbilityStatusEffectUtil.tryAttach(store, caster, (String)CAST_EFFECT, (float)0.6f);
        HymannGuardWaveSystem.hitFrostExplosion(store, buffer, caster, center, config, HymannDamageScaling.scaleSkillDamage(uuid, store, caster, config.guardWaveDamage));
    }

    private static void hitFrostExplosion(Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Ref<EntityStore> caster, Vector3d center, HymannConfig.Values config, float damage) {
        long casterIndex = caster.getIndex();
        Selector.selectNearbyEntities(store, (Vector3d)center, (double)config.guardWaveRange, target -> {
            if (!HymannTargeting.isHostileCombatMob((Ref<EntityStore>)target, caster, store)) {
                return;
            }
            AbilityStatusEffectUtil.tryAttach((Store)store, (Ref)target, (String)FROST_CHILL_EFFECT, (float)5.0f);
            HymannStunService.apply(store, (Ref<EntityStore>)target, config.guardWaveStunMs, caster);
            Damage frostExplosion = new Damage((Damage.Source)new Damage.EntitySource(caster), DamageCause.PHYSICAL, damage);
            DamageSystems.executeDamage((Ref)target, (CommandBuffer)buffer, (Damage)frostExplosion);
        }, target -> target != null && (long)target.getIndex() != casterIndex);
    }
}
