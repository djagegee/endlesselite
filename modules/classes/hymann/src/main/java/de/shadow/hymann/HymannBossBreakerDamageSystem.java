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
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$EntitySource
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$Source
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems$ApplyDamage
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
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
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.shadow.hymann.HymannBossBreakerSystem;
import java.util.Set;

final class HymannBossBreakerDamageSystem
extends DamageEventSystem {
    private static final Query<EntityStore> ALL_ENTITIES = Query.any();

    HymannBossBreakerDamageSystem() {
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
    }

    public Query<EntityStore> getQuery() {
        return ALL_ENTITIES;
    }

    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.BEFORE, DamageSystems.ApplyDamage.class));
    }

    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Damage damage) {
        Ref attacker;
        if (damage == null || damage.isCancelled() || damage.getAmount() <= 0.0f) {
            return;
        }
        Ref target = chunk.getReferenceTo(index);
        if (!HymannBossBreakerSystem.isDefenseBroken((Ref<EntityStore>)target)) {
            return;
        }
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource source2) {
            attacker = source2.getRef();
        } else {
            attacker = null;
        }
        if (attacker == null || !attacker.isValid() || store.getComponent(attacker, PlayerRef.getComponentType()) == null) {
            return;
        }
        damage.setAmount(damage.getAmount() * HymannBossBreakerSystem.bossDamageTakenMultiplier());
    }
}
