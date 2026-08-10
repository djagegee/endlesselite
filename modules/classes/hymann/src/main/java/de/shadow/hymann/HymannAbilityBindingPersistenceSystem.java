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
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.MMOSkillTreePlugin
 *  com.ziggfreed.mmoskilltree.ability.MmoSlot
 *  com.ziggfreed.mmoskilltree.ability.TriggerKey
 *  com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI
 *  com.ziggfreed.mmoskilltree.data.SkillComponent
 *  com.ziggfreed.mmoskilltree.player.PlayerDataDomain
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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.MMOSkillTreePlugin;
import com.ziggfreed.mmoskilltree.ability.MmoSlot;
import com.ziggfreed.mmoskilltree.ability.TriggerKey;
import com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI;
import com.ziggfreed.mmoskilltree.data.SkillComponent;
import com.ziggfreed.mmoskilltree.player.PlayerDataDomain;
import de.shadow.hymann.HymannMmoBridge;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class HymannAbilityBindingPersistenceSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final long RESTORE_DELAY_MS = 5000L;
    private final HytaleLogger logger;
    private final Path storageFile;
    private final Map<UUID, SavedBindings> savedByPlayer = new ConcurrentHashMap<UUID, SavedBindings>();
    private final Map<UUID, State> states = new ConcurrentHashMap<UUID, State>();

    HymannAbilityBindingPersistenceSystem(HytaleLogger logger, Path storageFile) {
        this.logger = logger;
        this.storageFile = storageFile;
        this.loadSavedBindings();
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered persistent Hymann MMO key-binding recovery (%s saved player profile(s))", this.savedByPlayer.size());
    }

    void clear() {
        this.states.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        State state;
        UUID playerUuid;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        UUID uUID = playerUuid = playerRef == null ? null : playerRef.getUuid();
        if (playerUuid == null || !HymannMmoBridge.isHymann(playerUuid)) {
            return;
        }
        Ref entity = chunk.getReferenceTo(index);
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, (Ref)entity);
        if (skills == null) {
            return;
        }
        long now = System.currentTimeMillis();
        State state2 = state = this.states.computeIfAbsent(playerUuid, ignored -> new State());
        synchronized (state2) {
            boolean componentChanged;
            ConcurrentHashMap currentBindings = skills.getMmoSlotBindings();
            ConcurrentHashMap currentInputs = skills.getMmoSlotInputs();
            if (!currentBindings.isEmpty()) {
                this.capture(playerUuid, currentBindings, currentInputs);
            }
            boolean bl = componentChanged = state.component != skills || state.storeIdentity != System.identityHashCode(store);
            if (componentChanged) {
                state.component = skills;
                state.storeIdentity = System.identityHashCode(store);
                state.restoreAtMs = currentBindings.isEmpty() && this.hasSavedBindings(playerUuid) ? now + 5000L : 0L;
                return;
            }
            if (currentBindings.isEmpty() && state.hadBindings && this.hasSavedBindings(playerUuid) && state.restoreAtMs == 0L) {
                state.restoreAtMs = now + 5000L;
            }
            boolean bl2 = state.hadBindings = !currentBindings.isEmpty();
            if (state.restoreAtMs == 0L || now < state.restoreAtMs) {
                return;
            }
            state.restoreAtMs = 0L;
            if (!currentBindings.isEmpty()) {
                this.capture(playerUuid, currentBindings, currentInputs);
                return;
            }
            int restored = this.restore(playerUuid, skills);
            if (restored > 0) {
                MMOSkillTreePlugin.getInstance().players().markDirty(store, entity, PlayerDataDomain.SKILLS);
                ((HytaleLogger.Api)this.logger.atInfo()).log("Restored %s persistent Hymann active-skill binding(s) for %s", restored, (Object)playerUuid);
            }
        }
    }

    private boolean hasSavedBindings(UUID playerUuid) {
        SavedBindings saved = this.savedByPlayer.get(playerUuid);
        return saved != null && !saved.abilities.isEmpty();
    }

    private int restore(UUID playerUuid, SkillComponent skills) {
        SavedBindings saved = this.savedByPlayer.get(playerUuid);
        if (saved == null) {
            return 0;
        }
        int restored = 0;
        for (Map.Entry<MmoSlot, String> entry : saved.abilities.entrySet()) {
            MmoSlot slot = entry.getKey();
            String abilityId = entry.getValue();
            if (slot == null || abilityId == null || abilityId.isBlank() || !skills.hasUnlockedAbility(abilityId)) continue;
            skills.bindAbilityToSlot(slot, abilityId);
            TriggerKey input = saved.inputs.get(slot);
            if (input != null) {
                skills.setInputForSlot(slot, input);
            }
            ++restored;
        }
        return restored;
    }

    private void capture(UUID playerUuid, Map<MmoSlot, String> abilities, Map<MmoSlot, TriggerKey> inputs) {
        SavedBindings next = SavedBindings.copyOf(abilities, inputs);
        if (next.abilities.isEmpty()) {
            return;
        }
        SavedBindings previous = this.savedByPlayer.put(playerUuid, next);
        if (!next.equals(previous)) {
            this.saveSavedBindings();
            ((HytaleLogger.Api)this.logger.atInfo()).log("Saved %s Hymann active-skill binding(s) for %s", next.abilities.size(), (Object)playerUuid);
        }
    }

    private void loadSavedBindings() {
        if (this.storageFile == null || !Files.isRegularFile(this.storageFile, new LinkOption[0])) {
            return;
        }
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(this.storageFile);){
            properties.load(reader);
        }
        catch (IOException error) {
            ((HytaleLogger.Api)((HytaleLogger.Api)this.logger.atWarning()).withCause((Throwable)error)).log("Could not read Hymann binding storage: %s", (Object)this.storageFile);
            return;
        }
        for (String key : properties.stringPropertyNames()) {
            MmoSlot slot;
            UUID playerUuid;
            String[] parts = key.split("\\.");
            if (parts.length != 3) continue;
            try {
                playerUuid = UUID.fromString(parts[0]);
                slot = MmoSlot.valueOf((String)parts[1]);
            }
            catch (IllegalArgumentException ignored2) {
                continue;
            }
            SavedBindings saved = this.savedByPlayer.computeIfAbsent(playerUuid, ignored -> new SavedBindings());
            String value = properties.getProperty(key);
            if ("ability".equals(parts[2]) && value != null && !value.isBlank()) {
                saved.abilities.put(slot, value);
                continue;
            }
            if (!"input".equals(parts[2]) || value == null || value.isBlank()) continue;
            try {
                TriggerKey input = TriggerKey.parse((String)value);
                if (input == null) continue;
                saved.inputs.put(slot, input);
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        }
    }

    private synchronized void saveSavedBindings() {
        if (this.storageFile == null) {
            return;
        }
        Properties properties = new Properties();
        for (Map.Entry<UUID, SavedBindings> player : this.savedByPlayer.entrySet()) {
            for (Map.Entry<MmoSlot, String> ability : player.getValue().abilities.entrySet()) {
                String prefix = String.valueOf(player.getKey()) + "." + ability.getKey().name() + ".";
                properties.setProperty(prefix + "ability", ability.getValue());
                TriggerKey input = player.getValue().inputs.get(ability.getKey());
                if (input == null) continue;
                properties.setProperty(prefix + "input", input.serialize());
            }
        }
        try {
            Path parent = this.storageFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(this.storageFile, new OpenOption[0]);){
                properties.store(writer, "Hymann MMO active-skill bindings");
            }
        }
        catch (IOException error) {
            ((HytaleLogger.Api)((HytaleLogger.Api)this.logger.atWarning()).withCause((Throwable)error)).log("Could not save Hymann binding storage: %s", (Object)this.storageFile);
        }
    }

    private static final class State {
        private SkillComponent component;
        private int storeIdentity;
        private long restoreAtMs;
        private boolean hadBindings;

        private State() {
        }
    }

    private static final class SavedBindings {
        private final Map<MmoSlot, String> abilities = new EnumMap<MmoSlot, String>(MmoSlot.class);
        private final Map<MmoSlot, TriggerKey> inputs = new EnumMap<MmoSlot, TriggerKey>(MmoSlot.class);

        private SavedBindings() {
        }

        private static SavedBindings copyOf(Map<MmoSlot, String> abilities, Map<MmoSlot, TriggerKey> inputs) {
            SavedBindings copy = new SavedBindings();
            for (Map.Entry<MmoSlot, String> entry : abilities.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isBlank()) continue;
                copy.abilities.put(entry.getKey(), entry.getValue());
                TriggerKey input = inputs.get(entry.getKey());
                if (input == null) continue;
                copy.inputs.put(entry.getKey(), input);
            }
            return copy;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object other) {
            if (!(other instanceof SavedBindings)) return false;
            SavedBindings that = (SavedBindings)other;
            if (!this.abilities.equals(that.abilities)) return false;
            if (!this.inputs.equals(that.inputs)) return false;
            return true;
        }

        public int hashCode() {
            return 31 * this.abilities.hashCode() + this.inputs.hashCode();
        }
    }
}
