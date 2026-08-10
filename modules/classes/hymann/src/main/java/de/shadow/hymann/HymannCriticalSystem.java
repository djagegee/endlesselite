/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.airijko.endlessleveling.api.EndlessLevelingAPI
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
 *  com.ziggfreed.mmoskilltree.feedback.FeedbackContext
 *  com.ziggfreed.mmoskilltree.feedback.FeedbackEvent
 *  com.ziggfreed.mmoskilltree.feedback.FeedbackService
 *  com.ziggfreed.mmoskilltree.i18n.Messages
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
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
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import com.ziggfreed.mmoskilltree.event.CombatDamageEventSystem;
import com.ziggfreed.mmoskilltree.feedback.FeedbackContext;
import com.ziggfreed.mmoskilltree.feedback.FeedbackEvent;
import com.ziggfreed.mmoskilltree.feedback.FeedbackService;
import com.ziggfreed.mmoskilltree.i18n.Messages;
import de.shadow.hymann.HymannCriticalProfile;
import de.shadow.hymann.HymannMmoBridge;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

final class HymannCriticalSystem
extends DamageEventSystem {
    private static final Query<EntityStore> ALL_ENTITIES = Query.any();
    private final HytaleLogger logger;

    HymannCriticalSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann ascension critical system");
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
        Ref<EntityStore> attacker = HymannCriticalSystem.attackerRef(damage.getSource());
        if (attacker == null || !attacker.isValid()) {
            return;
        }
        PlayerRef player = (PlayerRef)store.getComponent(attacker, PlayerRef.getComponentType());
        if (player == null || player.getUuid() == null || !HymannMmoBridge.isHymann(player.getUuid())) {
            return;
        }
        HymannCriticalProfile.Values critical = HymannCriticalProfile.forClassId(EndlessLevelingAPI.get().getPrimaryClassId(player.getUuid()));
        if (critical.chance() <= 0.0 || critical.bonusDamage() <= 0.0 || ThreadLocalRandom.current().nextDouble() >= critical.chance()) {
            return;
        }
        damage.setAmount((float)((double)damage.getAmount() * (1.0 + critical.bonusDamage())));
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, attacker);
        FeedbackService.emit((FeedbackEvent)FeedbackEvent.CRIT, (FeedbackContext)FeedbackContext.builder(store).anchor(chunk.getReferenceTo(index)).viewer(attacker).playerRef(player).skills(skills).combatText(Messages.getForLang((String)"en", (String)"ui.combattext.crit"), -20.0f).build());
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

