/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.airijko.endlessleveling.systems.PlayerCombatSystem
 *  com.airijko.endlessleveling.systems.PveMeterCommitSystem
 *  com.airijko.endlessleveling.systems.PveMeterCommitSystem$PendingMeterRecord
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.dependency.Dependency
 *  com.hypixel.hytale.component.dependency.Order
 *  com.hypixel.hytale.component.dependency.SystemDependency
 *  com.hypixel.hytale.component.query.Query
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
 *  com.ziggfreed.mmoskilltree.event.CombatDamageEventSystem
 *  com.ziggfreed.mmoskilltree.feedback.FeedbackContext
 *  com.ziggfreed.mmoskilltree.feedback.FeedbackEvent
 *  com.ziggfreed.mmoskilltree.feedback.FeedbackService
 *  com.ziggfreed.mmoskilltree.i18n.Messages
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.systems.PlayerCombatSystem;
import com.airijko.endlessleveling.systems.PveMeterCommitSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.event.CombatDamageEventSystem;
import com.ziggfreed.mmoskilltree.feedback.FeedbackContext;
import com.ziggfreed.mmoskilltree.feedback.FeedbackEvent;
import com.ziggfreed.mmoskilltree.feedback.FeedbackService;
import com.ziggfreed.mmoskilltree.i18n.Messages;
import de.shadow.hymann.HymannAccess;
import de.shadow.hymann.HymannMmoBridge;
import java.util.Set;

final class HymannMmoCriticalFeedbackSystem
extends DamageEventSystem {
    private static final Query<EntityStore> ALL_ENTITIES = Query.any();

    HymannMmoCriticalFeedbackSystem() {
    }

    public Query<EntityStore> getQuery() {
        return ALL_ENTITIES;
    }

    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.AFTER, CombatDamageEventSystem.class), new SystemDependency(Order.AFTER, PlayerCombatSystem.class), new SystemDependency(Order.BEFORE, DamageSystems.ApplyDamage.class));
    }

    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, Damage damage) {
        if (damage == null || damage.isCancelled() || damage.getAmount() <= 0.0f || !HymannMmoCriticalFeedbackSystem.isEndlessCritical(damage)) {
            return;
        }
        Ref<EntityStore> attacker = HymannMmoCriticalFeedbackSystem.attackerRef(damage.getSource());
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
        FeedbackService.emit((FeedbackEvent)FeedbackEvent.CRIT, (FeedbackContext)FeedbackContext.builder(store).anchor(chunk.getReferenceTo(index)).viewer(attacker).playerRef(player).skills(MMOSkillTreeAPI.getSkillComponent(store, attacker)).combatText(Messages.getForLang((String)"en", (String)"ui.combattext.crit"), -20.0f).build());
    }

    private static boolean isEndlessCritical(Damage damage) {
        if (Boolean.TRUE.equals(damage.getIfPresentMetaObject(PlayerCombatSystem.ATTACKER_CRITICAL))) {
            return true;
        }
        PveMeterCommitSystem.PendingMeterRecord record = (PveMeterCommitSystem.PendingMeterRecord)damage.getIfPresentMetaObject(PveMeterCommitSystem.PENDING_METER_RECORD);
        return record != null && record.critical();
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
