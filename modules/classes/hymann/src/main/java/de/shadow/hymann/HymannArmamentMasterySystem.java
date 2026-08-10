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
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.server.core.inventory.InventoryComponent
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$EntitySource
 *  com.hypixel.hytale.server.core.modules.entity.damage.Damage$Source
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem
 *  com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems$ApplyDamage
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
 *  com.ziggfreed.mmoskilltree.event.CombatDamageEventSystem
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
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import com.ziggfreed.mmoskilltree.event.CombatDamageEventSystem;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannMmoCriticalFeedbackSystem;
import de.shadow.hymann.HymannTreeRewards;
import java.util.Set;

final class HymannArmamentMasterySystem
extends DamageEventSystem {
    private static final Query<EntityStore> ALL_ENTITIES = Query.any();
    private final HytaleLogger logger;

    HymannArmamentMasterySystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        registry.registerSystem((ISystem)new HymannMmoCriticalFeedbackSystem());
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann armament mastery damage and MMO critical feedback systems");
    }

    public Query<EntityStore> getQuery() {
        return ALL_ENTITIES;
    }

    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.AFTER, CombatDamageEventSystem.class), new SystemDependency(Order.BEFORE, DamageSystems.ApplyDamage.class));
    }

    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Damage damage) {
        if (damage == null || damage.isCancelled() || damage.getAmount() <= 0.0f) {
            return;
        }
        Ref<EntityStore> attacker = HymannArmamentMasterySystem.attackerRef(damage.getSource());
        if (attacker == null || !attacker.isValid()) {
            return;
        }
        PlayerRef player = (PlayerRef)store.getComponent(attacker, PlayerRef.getComponentType());
        if (player == null || player.getUuid() == null || !HymannMmoBridge.isHymann(player.getUuid())) {
            return;
        }
        ItemStack heldItem = InventoryComponent.getItemInHand(store, attacker);
        if (heldItem == null || !HymannAccess.isArmament(heldItem.getItemId())) {
            return;
        }
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, attacker);
        double bonus = HymannTreeRewards.flatDamage(skills);
        if (bonus > 0.0) {
            damage.setAmount((float)((double)damage.getAmount() + bonus));
        }
    }

    private static Ref<EntityStore> attackerRef(Damage.Source source) {
        Ref ref;
        if (source instanceof Damage.EntitySource) {
            Damage.EntitySource entitySource = (Damage.EntitySource)source;
            ref = entitySource.getRef();
        } else {
            ref = null;
        }
        return ref;
    }
}

