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
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$EntitySource
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$Source
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.ParticleUtil
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
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
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannDamageScaling;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannStunService;
import de.shadow.hymann.HymannTargeting;
import de.shadow.hymann.HymannTreeRewards;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannPassiveAuraSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final String HIT_EFFECT = "Hymann_Stormbreaker_Hit";
    private static final String SOUND_EFFECT = "Hymann_Stormbreaker_Sound";
    private static final double RADIUS = 8.0;
    private static final long SHOCK_MS = 2000L;
    private final HytaleLogger logger;
    private final Map<UUID, Long> nextPulseAt = new ConcurrentHashMap<UUID, Long>();

    HymannPassiveAuraSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann Stormbreaker passive pulses");
    }

    void clear() {
        this.nextPulseAt.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        UUID uuid;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        Ref caster = chunk.getReferenceTo(index);
        UUID uUID = uuid = playerRef == null ? null : playerRef.getUuid();
        if (uuid == null || caster == null || !caster.isValid() || !HymannMmoBridge.isHymann(uuid)) {
            return;
        }
        ItemStack heldItem = InventoryComponent.getItemInHand(store, (Ref)caster);
        if (heldItem == null || !HymannAccess.isArmament(heldItem.getItemId())) {
            return;
        }
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, (Ref)caster);
        PulseRank rank = PulseRank.selected(skills);
        if (rank == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextPulseAt.getOrDefault(uuid, 0L)) {
            return;
        }
        this.nextPulseAt.put(uuid, now + rank.cooldownMs());
        TransformComponent transform = (TransformComponent)buffer.getComponent(caster, TransformComponent.getComponentType());
        if (transform == null) {
            return;
        }
        float damage = HymannDamageScaling.scaleSkillDamage(uuid, store, (Ref<EntityStore>)caster, rank.baseDamage());
        HymannPassiveAuraSystem.pulse(store, buffer, (Ref<EntityStore>)caster, new Vector3d((Vector3dc)transform.getPosition()), damage);
    }

    private static void pulse(Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Ref<EntityStore> caster, Vector3d center, float damage) {
        long casterIndex = caster.getIndex();
        ArrayList<Ref<EntityStore>> targets = new ArrayList<>();
        HashSet<Integer> seenTargets = new HashSet<>();
        Selector.selectNearbyEntities(store, (Vector3d)center, (double)8.0, target -> {
            if (HymannPassiveAuraSystem.isEnemyTarget((Ref<EntityStore>)target, caster, store) && seenTargets.add(target.getIndex())) {
                targets.add(target);
            }
        }, target -> target != null && (long)target.getIndex() != casterIndex);
        if (!targets.isEmpty()) {
            AbilityStatusEffectUtil.tryAttach(store, caster, (String)SOUND_EFFECT, (float)0.1f);
        }
        for (Ref<EntityStore> target2 : targets) {
            if (!HymannPassiveAuraSystem.isEnemyTarget((Ref<EntityStore>)target2, caster, store)) continue;
            HymannPassiveAuraSystem.spawnLightningHit((Ref<EntityStore>)target2, store, buffer);
            HymannStunService.apply(store, (Ref<EntityStore>)target2, 2000L, caster);
            DamageSystems.executeDamage((Ref)target2, buffer, (Damage)new Damage((Damage.Source)new Damage.EntitySource(caster), DamageCause.PHYSICAL, damage));
        }
    }

    private static boolean isEnemyTarget(Ref<EntityStore> target, Ref<EntityStore> caster, Store<EntityStore> store) {
        return HymannTargeting.isHostileCombatMob(target, caster, store);
    }

    static void spawnLightningHit(Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        TransformComponent transform = (TransformComponent)buffer.getComponent(target, TransformComponent.getComponentType());
        if (transform != null) {
            ParticleUtil.spawnParticleEffect((String)"Mjolnir_Lightning_Strike", (Vector3d)new Vector3d((Vector3dc)transform.getPosition()).add(0.0, 4.5, 0.0), (float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f, (float)0.0f, buffer);
        }
    }

    private record PulseRank(long cooldownMs, float baseDamage) {
        static PulseRank selected(SkillComponent skills) {
            if (HymannTreeRewards.hasPassive(skills, "hymann_passive_stormbreaker_pulse_3")) {
                return new PulseRank(5000L, 400.0f);
            }
            if (HymannTreeRewards.hasPassive(skills, "hymann_passive_stormbreaker_pulse_2")) {
                return new PulseRank(8000L, 250.0f);
            }
            if (HymannTreeRewards.hasPassive(skills, "hymann_passive_stormbreaker_pulse_1")) {
                return new PulseRank(10000L, 150.0f);
            }
            return null;
        }
    }
}

