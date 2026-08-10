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
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
 *  com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
 *  com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
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
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannArmamentMasterySystem;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannPassiveAuraSystem;
import de.shadow.hymann.HymannTargeting;
import de.shadow.hymann.HymannTreeRewards;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class HymannCombatPassiveSystem
extends DamageEventSystem {
    private static final Query<EntityStore> ALL = Query.any();
    private static final long COMBO_TIMEOUT_MS = 3000L;
    private final HytaleLogger logger;
    private final Set<UUID> tempestRebirthArmed = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ComboState> combos = new ConcurrentHashMap<UUID, ComboState>();

    HymannCombatPassiveSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann tree combat passives");
    }

    void clear() {
        this.tempestRebirthArmed.clear();
        this.combos.clear();
    }

    public Query<EntityStore> getQuery() {
        return ALL;
    }

    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.AFTER, HymannArmamentMasterySystem.class), new SystemDependency(Order.BEFORE, DamageSystems.ApplyDamage.class));
    }

    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Damage damage) {
        PlayerRef player;
        if (damage == null || damage.isCancelled() || damage.getAmount() <= 0.0f) {
            return;
        }
        Ref target = chunk.getReferenceTo(index);
        Ref<EntityStore> attacker = HymannCombatPassiveSystem.attackerRef(damage.getSource());
        PlayerRef playerRef = player = attacker == null ? null : (PlayerRef)store.getComponent(attacker, PlayerRef.getComponentType());
        if (player == null || player.getUuid() == null || !HymannCombatPassiveSystem.isArmamentUser(player.getUuid(), attacker, store)) {
            return;
        }
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, attacker);
        if (skills == null || !HymannTargeting.isHostileCombatMob((Ref<EntityStore>)target, attacker, store)) {
            return;
        }
        UUID uuid = player.getUuid();
        if (this.tempestRebirthArmed.remove(uuid)) {
            damage.setAmount(damage.getAmount() * 1.5f);
        }
        this.applyComboAndCadence(uuid, (Ref<EntityStore>)target, store, buffer, damage, skills);
        double lifestealPercent = HymannTreeRewards.lifestealPercent(skills);
        if (lifestealPercent > 0.0) {
            float healing = (float)((double)Math.max(0.0f, damage.getAmount()) * lifestealPercent / 100.0);
            HymannCombatPassiveSystem.heal(attacker, store, healing);
        }
        if (HymannTreeRewards.hasPassive(skills, "hymann_passive_tempest_rebirth") && HymannCombatPassiveSystem.isLethal((Ref<EntityStore>)target, store, damage.getAmount())) {
            HymannCombatPassiveSystem.healPercent(attacker, store, 0.2f);
            this.tempestRebirthArmed.add(uuid);
        }
    }

    private void applyComboAndCadence(UUID uuid, Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Damage damage, SkillComponent skills) {
        long now = System.currentTimeMillis();
        ComboState previous = this.combos.get(uuid);
        int hits = previous == null || now - previous.lastHitAtMs() > 3000L ? 1 : previous.hits() + 1;
        this.combos.put(uuid, new ComboState(hits, now));
        double comboDamage = HymannTreeRewards.comboDamage(skills);
        if (comboDamage > 0.0 && hits % 3 == 0) {
            damage.setAmount((float)((double)damage.getAmount() + comboDamage));
        }
        if (HymannTreeRewards.hasPassive(skills, "hymann_passive_thunder_cadence") && hits % 2 == 0) {
            damage.setAmount(damage.getAmount() * 1.5f + 10.0f);
            HymannPassiveAuraSystem.spawnLightningHit(target, store, buffer);
        }
    }

    private static boolean isArmamentUser(UUID uuid, Ref<EntityStore> entity, Store<EntityStore> store) {
        if (uuid == null || entity == null || !entity.isValid() || !HymannMmoBridge.isHymann(uuid)) {
            return false;
        }
        ItemStack heldItem = InventoryComponent.getItemInHand(store, entity);
        return heldItem != null && HymannAccess.isArmament(heldItem.getItemId());
    }

    private static boolean isLethal(Ref<EntityStore> target, Store<EntityStore> store, float incomingDamage) {
        EntityStatMap stats = (EntityStatMap)store.getComponent(target, EntityStatMap.getComponentType());
        EntityStatValue health = stats == null ? null : stats.get(DefaultEntityStatTypes.getHealth());
        return health != null && incomingDamage >= health.get();
    }

    private static void heal(Ref<EntityStore> entity, Store<EntityStore> store, float amount) {
        EntityStatMap stats = (EntityStatMap)store.getComponent(entity, EntityStatMap.getComponentType());
        if (stats != null && amount > 0.0f) {
            stats.addStatValue(DefaultEntityStatTypes.getHealth(), amount);
        }
    }

    private static void healPercent(Ref<EntityStore> entity, Store<EntityStore> store, float percent) {
        EntityStatValue health;
        EntityStatMap stats = (EntityStatMap)store.getComponent(entity, EntityStatMap.getComponentType());
        EntityStatValue entityStatValue = health = stats == null ? null : stats.get(DefaultEntityStatTypes.getHealth());
        if (stats != null && health != null && percent > 0.0f) {
            stats.addStatValue(DefaultEntityStatTypes.getHealth(), health.getMax() * percent);
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

    private record ComboState(int hits, long lastHitAtMs) {
    }
}
