/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.airijko.endlessleveling.api.EndlessLevelingAPI
 *  com.airijko.endlessleveling.enums.SkillAttributeType
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
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.airijko.endlessleveling.enums.SkillAttributeType;
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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import de.shadow.hymann.HymannCriticalProfile;
import de.shadow.hymann.HymannTreeRewards;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class HymannCriticalAttributeSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final long REFRESH_MS = 1000L;
    private static final long TTL_MS = 2500L;
    private static final String PRECISION_SOURCE = "hymann_class_crit_chance";
    private static final String FEROCITY_SOURCE = "hymann_class_crit_damage";
    private static final String SORCERY_SOURCE = "sorcflat_hymann_tree";
    private final HytaleLogger logger;
    private final Map<UUID, Long> refreshedAt = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, PublishedAttributes> published = new ConcurrentHashMap<UUID, PublishedAttributes>();

    HymannCriticalAttributeSystem(HytaleLogger logger) {
        this.logger = logger;
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann native critical attribute bridge");
    }

    void clear() {
        this.refreshedAt.clear();
        this.published.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        UUID uuid;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        UUID uUID = uuid = playerRef == null ? null : playerRef.getUuid();
        if (uuid == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long previous = this.refreshedAt.get(uuid);
        if (previous != null && now - previous < 1000L) {
            return;
        }
        this.refreshedAt.put(uuid, now);
        EndlessLevelingAPI api = EndlessLevelingAPI.get();
        String classId = api.getPrimaryClassId(uuid);
        long expiresAtMs = now + 2500L;
        if (classId == null || !classId.toLowerCase(Locale.ROOT).startsWith("hymann")) {
            this.publish(api, uuid, 0.0, 0.0, 0.0, expiresAtMs);
            return;
        }
        Ref entity = chunk.getReferenceTo(index);
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, (Ref)entity);
        HymannCriticalProfile.Values crit = HymannCriticalProfile.forClassId(classId.toLowerCase(Locale.ROOT));
        this.publish(api, uuid, crit.chance() * 100.0, crit.bonusDamage() * 100.0 + HymannTreeRewards.critDamagePercent(skills), HymannTreeRewards.sorceryPercent(skills), expiresAtMs);
    }

    private void publish(EndlessLevelingAPI api, UUID uuid, double precision, double ferocity, double sorcery, long expiresAtMs) {
        api.setExternalAttributeBonus(uuid, "precision", PRECISION_SOURCE, precision, expiresAtMs);
        api.setExternalAttributeBonus(uuid, "ferocity", FEROCITY_SOURCE, ferocity, expiresAtMs);
        api.setExternalAttributeBonus(uuid, "sorcery", SORCERY_SOURCE, sorcery, expiresAtMs);
        PublishedAttributes current = PublishedAttributes.from(precision, ferocity, sorcery);
        PublishedAttributes previous = this.published.put(uuid, current);
        if (current.equals(previous)) {
            return;
        }
        HymannCriticalAttributeSystem.publishChange(api, uuid, SkillAttributeType.PRECISION, previous == null ? 0 : previous.precision(), current.precision());
        HymannCriticalAttributeSystem.publishChange(api, uuid, SkillAttributeType.FEROCITY, previous == null ? 0 : previous.ferocity(), current.ferocity());
        HymannCriticalAttributeSystem.publishChange(api, uuid, SkillAttributeType.SORCERY, previous == null ? 0 : previous.sorcery(), current.sorcery());
        ((HytaleLogger.Api)this.logger.atInfo()).log("Synced Hymann Endless attributes for %s: precision=%s%% ferocity=%s%% sorcery=+%s magic damage", (Object)uuid, (Object)current.precision(), (Object)current.ferocity(), (Object)current.sorcery());
    }

    private static void publishChange(EndlessLevelingAPI api, UUID uuid, SkillAttributeType attribute, int previous, int current) {
        if (previous != current) {
            api.notifySkillAttributeChanged(uuid, attribute, previous, current);
        }
    }

    private record PublishedAttributes(int precision, int ferocity, int sorcery) {
        private static PublishedAttributes from(double precision, double ferocity, double sorcery) {
            return new PublishedAttributes((int)Math.round(precision), (int)Math.round(ferocity), (int)Math.round(sorcery));
        }
    }
}
