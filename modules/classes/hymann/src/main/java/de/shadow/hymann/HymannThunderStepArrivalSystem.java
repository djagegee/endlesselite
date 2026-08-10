/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.dependency.Dependency
 *  com.hypixel.hytale.component.dependency.Order
 *  com.hypixel.hytale.component.dependency.SystemDependency
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.component.system.tick.EntityTickingSystem
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$EntitySource
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$Source
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
 *  com.hypixel.hytale.server.core.modules.entity.teleport.TeleportSystems$PlayerMoveSystem
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
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportSystems;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityStatusEffectUtil;
import de.shadow.hymann.HymannStunService;
import de.shadow.hymann.HymannTargeting;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class HymannThunderStepArrivalSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final Map<UUID, PendingImpact> PENDING = new ConcurrentHashMap<UUID, PendingImpact>();
    private static final String ARRIVAL_EFFECT = "Hymann_Thunder_Step_Arrival";
    private static final long ARRIVAL_TIMEOUT_MS = 3000L;

    HymannThunderStepArrivalSystem(HytaleLogger logger) {
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
    }

    static void queue(UUID playerUuid, Vector3d landing, double radius, float damage, long stunMs) {
        PENDING.put(playerUuid, new PendingImpact(new Vector3d((Vector3dc)landing), radius, damage, stunMs, System.currentTimeMillis() + 3000L));
    }

    static void clear() {
        PENDING.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.AFTER, TeleportSystems.PlayerMoveSystem.class));
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        PlayerRef playerRef;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef2 = playerRef = player == null ? null : player.getPlayerRef();
        if (playerRef == null || playerRef.getUuid() == null) {
            return;
        }
        PendingImpact impact = PENDING.get(playerRef.getUuid());
        if (impact == null) {
            return;
        }
        if (impact.expiresAtMs < System.currentTimeMillis()) {
            PENDING.remove(playerRef.getUuid(), impact);
            return;
        }
        Ref caster = chunk.getReferenceTo(index);
        TransformComponent transform = (TransformComponent)buffer.getComponent(caster, TransformComponent.getComponentType());
        if (transform == null || transform.getPosition().distanceSquared((Vector3dc)impact.landing) > 9.0) {
            return;
        }
        if (!PENDING.remove(playerRef.getUuid(), impact)) {
            return;
        }
        AbilityStatusEffectUtil.tryAttach(store, (Ref)caster, (String)ARRIVAL_EFFECT, (float)0.65f);
        HymannThunderStepArrivalSystem.strikeArrivalArea(store, buffer, (Ref<EntityStore>)caster, new Vector3d((Vector3dc)transform.getPosition()), impact);
    }

    private static void strikeArrivalArea(Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Ref<EntityStore> caster, Vector3d center, PendingImpact impact) {
        long casterIndex = caster.getIndex();
        Selector.selectNearbyEntities(store, (Vector3d)center, (double)impact.radius, target -> {
            if (!HymannTargeting.isHostileCombatMob((Ref<EntityStore>)target, caster, store)) {
                return;
            }
            HymannStunService.apply(store, (Ref<EntityStore>)target, impact.stunMs, caster);
            DamageSystems.executeDamage((Ref)target, (CommandBuffer)buffer, (Damage)new Damage((Damage.Source)new Damage.EntitySource(caster), DamageCause.PHYSICAL, impact.damage));
        }, entity -> entity != null && (long)entity.getIndex() != casterIndex);
    }

    private record PendingImpact(Vector3d landing, double radius, float damage, long stunMs, long expiresAtMs) {
    }
}

