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

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class HymannProfileProgressSystem
extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> PLAYERS = Query.and((Query[])new Query[]{Player.getComponentType()});
    private static final String[] CUSTOM_ABILITIES = new String[]{"hymann_thunder_step", "hymann_storm_fury", "hymann_thunder_aegis", "hymann_passive_stormbreaker_aura", "hymann_passive_tempest_rebirth", "hymann_passive_stormbreaker_pulse_1", "hymann_passive_stormbreaker_pulse_2", "hymann_passive_stormbreaker_pulse_3", "hymann_passive_thunder_cadence"};
    private static final String[] REMOVED_LEGACY_ABILITIES = new String[]{"shield_bash", "shield_slam"};
    private final HytaleLogger logger;
    private final Path storageFile;
    private final Properties properties = new Properties();
    private final Map<UUID, Context> contexts = new ConcurrentHashMap<UUID, Context>();

    HymannProfileProgressSystem(HytaleLogger logger, Path storageFile) {
        this.logger = logger;
        this.storageFile = storageFile;
        this.load();
    }

    void register(ComponentRegistryProxy<EntityStore> registry) {
        registry.registerSystem((ISystem)this);
        ((HytaleLogger.Api)this.logger.atInfo()).log("Registered Hymann profile-scoped skill progression");
    }

    void clear() {
        this.contexts.clear();
    }

    public Query<EntityStore> getQuery() {
        return PLAYERS;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick(float deltaTime, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        Context context;
        UUID uuid;
        Player player = (Player)chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : player.getPlayerRef();
        UUID uUID = uuid = playerRef == null ? null : playerRef.getUuid();
        if (uuid == null) {
            return;
        }
        Ref entity = chunk.getReferenceTo(index);
        SkillComponent skills = MMOSkillTreeAPI.getSkillComponent(store, (Ref)entity);
        if (skills == null) {
            return;
        }
        int slot = EndlessLevelingAPI.get().getActiveProfileSlot(uuid);
        long now = System.currentTimeMillis();
        Context context2 = context = this.contexts.computeIfAbsent(uuid, ignored -> new Context());
        synchronized (context2) {
            boolean componentChanged;
            boolean bl = componentChanged = context.component != skills || context.storeIdentity != store;
            if (!context.initialized) {
                if (!this.hasProfile(uuid, slot)) {
                    Progress initialProgress = HymannProfileProgressSystem.capture(skills);
                    if (this.loadLegacy(uuid) == null) {
                        this.saveLegacyIfMissing(uuid, initialProgress);
                        this.saveProfile(uuid, slot, initialProgress);
                        this.save();
                        this.apply(uuid, slot, store, (Ref<EntityStore>)entity, skills);
                        ((HytaleLogger.Api)this.logger.atInfo()).log("Migrated legacy account-wide Hymann progression to Endless profile %s of %s", slot, (Object)uuid);
                    } else {
                        HymannProfileProgressSystem.clearRuntime(skills);
                        this.saveProfile(uuid, slot, HymannProfileProgressSystem.capture(skills));
                        this.save();
                        ((HytaleLogger.Api)this.logger.atInfo()).log("Initialized empty Hymann progression for new Endless profile %s of %s", slot, (Object)uuid);
                    }
                    HymannProfileProgressSystem.markDirty(store, (Ref<EntityStore>)entity);
                } else {
                    this.apply(uuid, slot, store, (Ref<EntityStore>)entity, skills);
                    HymannProfileProgressSystem.markDirty(store, (Ref<EntityStore>)entity);
                }
                context.initialized = true;
                context.slot = slot;
                context.component = skills;
                context.storeIdentity = store;
                return;
            }
            if (componentChanged) {
                this.apply(uuid, slot, store, (Ref<EntityStore>)entity, skills);
                HymannProfileProgressSystem.markDirty(store, (Ref<EntityStore>)entity);
                context.component = skills;
                context.storeIdentity = store;
                context.slot = slot;
                return;
            }
            if (context.slot != slot) {
                this.saveProfile(uuid, context.slot, HymannProfileProgressSystem.capture(skills));
                this.apply(uuid, slot, store, (Ref<EntityStore>)entity, skills);
                this.save();
                HymannProfileProgressSystem.markDirty(store, (Ref<EntityStore>)entity);
                ((HytaleLogger.Api)this.logger.atInfo()).log("Switched Hymann MMOSkillTree progress for %s from Endless profile %s to %s", (Object)uuid, (Object)context.slot, (Object)slot);
                context.slot = slot;
            }
            if (now - context.lastPersistAtMs >= 5000L) {
                this.saveProfile(uuid, context.slot, HymannProfileProgressSystem.capture(skills));
                this.save();
                context.lastPersistAtMs = now;
            }
        }
    }

    private void apply(UUID uuid, int slot, Store<EntityStore> store, Ref<EntityStore> entity, SkillComponent skills) {
        Progress progress = this.loadProfile(uuid, slot);
        if (progress == null && this.shouldRestoreLegacy(uuid) && (progress = this.loadLegacy(uuid)) != null) {
            ((HytaleLogger.Api)this.logger.atInfo()).log("Restored legacy Hymann progression for profile %s of %s", slot, (Object)uuid);
        }
        if (progress == null) {
            HymannProfileProgressSystem.clearRuntime(skills);
            return;
        }
        skills.xpMap.put("HYMANN_ARMAMENT", progress.xp);
        HymannProfileProgressSystem.putOrRemove(skills.fractionalXpMap, "HYMANN_ARMAMENT", progress.fractionalXp);
        HymannProfileProgressSystem.putOrRemove(skills.restedXpMap, "HYMANN_ARMAMENT", progress.restedXp);
        HymannProfileProgressSystem.putOrRemoveList(skills.claimedRewards, "HYMANN_ARMAMENT", progress.claimedRewards);
        HymannProfileProgressSystem.putOrRemoveList(skills.pendingRewards, "HYMANN_ARMAMENT", progress.pendingRewards);
        HymannProfileProgressSystem.removeLegacyShieldCards(skills);
        for (String ability : CUSTOM_ABILITIES) {
            skills.lockAbility(ability);
        }
        HymannMmoBridge.syncCustomAbilityUnlocks(skills, MMOSkillTreeAPI.getLevel(store, entity, (String)"HYMANN_ARMAMENT"));
        HymannProfileProgressSystem.removeCustomBindings(skills);
        for (Map.Entry entry : progress.bindings.entrySet()) {
            String ability = (String)entry.getValue();
            if (!skills.hasUnlockedAbility(ability)) continue;
            skills.bindAbilityToSlot((MmoSlot)entry.getKey(), ability);
            TriggerKey input = progress.inputs.get(entry.getKey());
            if (input == null) continue;
            skills.setInputForSlot((MmoSlot)entry.getKey(), input);
        }
    }

    private static void clearRuntime(SkillComponent skills) {
        skills.xpMap.put("HYMANN_ARMAMENT", 0L);
        skills.fractionalXpMap.remove("HYMANN_ARMAMENT");
        skills.restedXpMap.remove("HYMANN_ARMAMENT");
        skills.claimedRewards.remove("HYMANN_ARMAMENT");
        skills.pendingRewards.remove("HYMANN_ARMAMENT");
        for (String ability : CUSTOM_ABILITIES) {
            skills.lockAbility(ability);
        }
        HymannProfileProgressSystem.removeLegacyShieldCards(skills);
        HymannProfileProgressSystem.removeCustomBindings(skills);
    }

    private static void removeLegacyShieldCards(SkillComponent skills) {
        List<?> claimed = skills.claimedRewards.getOrDefault("HYMANN_ARMAMENT", List.of());
        List<String> cleaned = claimed.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(id -> !HymannProfileProgressSystem.isRemovedLegacyAbility(id))
                .toList();
        if (cleaned.size() != claimed.size()) {
            HymannProfileProgressSystem.putOrRemoveList(skills.claimedRewards, "HYMANN_ARMAMENT", cleaned);
        }
        for (String ability : REMOVED_LEGACY_ABILITIES) {
            skills.lockAbility(ability);
        }
    }

    private static void removeCustomBindings(SkillComponent skills) {
        for (Object rawEntry : new ArrayList<>(skills.getMmoSlotBindings().entrySet())) {
            Map.Entry entry = (Map.Entry)rawEntry;
            if (!HymannProfileProgressSystem.isCustomAbility((String)entry.getValue()) && !HymannProfileProgressSystem.isRemovedLegacyAbility((String)entry.getValue())) continue;
            skills.unbindSlot((MmoSlot)entry.getKey());
            skills.getMmoSlotInputs().remove(entry.getKey());
        }
    }

    private static Progress capture(SkillComponent skills) {
        Progress progress = new Progress();
        progress.xp = skills.xpMap.getOrDefault("HYMANN_ARMAMENT", 0L);
        progress.fractionalXp = skills.fractionalXpMap.getOrDefault("HYMANN_ARMAMENT", 0.0);
        progress.restedXp = skills.restedXpMap.getOrDefault("HYMANN_ARMAMENT", 0L);
        for (String string : skills.claimedRewards.getOrDefault("HYMANN_ARMAMENT", List.of())) {
            if (HymannProfileProgressSystem.isRemovedLegacyAbility(string)) continue;
            progress.claimedRewards.add(string);
        }
        progress.pendingRewards.addAll(skills.pendingRewards.getOrDefault("HYMANN_ARMAMENT", List.of()));
        for (Map.Entry entry : skills.getMmoSlotBindings().entrySet()) {
            if (!HymannProfileProgressSystem.isCustomAbility((String)entry.getValue())) continue;
            progress.bindings.put((MmoSlot)entry.getKey(), (String)entry.getValue());
            TriggerKey input = (TriggerKey)skills.getMmoSlotInputs().get(entry.getKey());
            if (input == null) continue;
            progress.inputs.put((MmoSlot)entry.getKey(), input);
        }
        return progress;
    }

    private static boolean isCustomAbility(String id) {
        if (id == null) {
            return false;
        }
        for (String ability : CUSTOM_ABILITIES) {
            if (!ability.equals(id)) continue;
            return true;
        }
        return false;
    }

    private static boolean isRemovedLegacyAbility(String id) {
        if (id == null) {
            return false;
        }
        for (String ability : REMOVED_LEGACY_ABILITIES) {
            if (!ability.equals(id)) continue;
            return true;
        }
        return false;
    }

    private boolean shouldRestoreLegacy(UUID uuid) {
        String classId = EndlessLevelingAPI.get().getPrimaryClassId(uuid);
        return classId != null && classId.startsWith("hymann") && this.loadLegacy(uuid) != null;
    }

    private boolean hasProfile(UUID uuid, int slot) {
        return this.properties.containsKey(HymannProfileProgressSystem.key(uuid, slot, "xp"));
    }

    private Progress loadProfile(UUID uuid, int slot) {
        if (!this.hasProfile(uuid, slot)) {
            return null;
        }
        return this.read(HymannProfileProgressSystem.key(uuid, slot, ""));
    }

    private Progress loadLegacy(UUID uuid) {
        String prefix = "legacy." + String.valueOf(uuid) + ".";
        return this.properties.containsKey(prefix + "xp") ? this.read(prefix) : null;
    }

    private void saveLegacyIfMissing(UUID uuid, Progress progress) {
        String prefix = "legacy." + String.valueOf(uuid) + ".";
        if (!this.properties.containsKey(prefix + "xp") && !progress.isEmpty()) {
            this.write(prefix, progress);
        }
    }

    private void saveProfile(UUID uuid, int slot, Progress progress) {
        this.write(HymannProfileProgressSystem.key(uuid, slot, ""), progress);
    }

    private Progress read(String prefix) {
        Progress progress = new Progress();
        progress.xp = this.longValue(prefix + "xp");
        progress.fractionalXp = this.doubleValue(prefix + "fractionalXp");
        progress.restedXp = this.longValue(prefix + "restedXp");
        progress.claimedRewards.addAll(HymannProfileProgressSystem.decodeStrings(this.properties.getProperty(prefix + "claimed", "")));
        for (String value : HymannProfileProgressSystem.decodeStrings(this.properties.getProperty(prefix + "pending", ""))) {
            try {
                progress.pendingRewards.add(Integer.parseInt(value));
            }
            catch (NumberFormatException numberFormatException) {}
        }
        for (String value : HymannProfileProgressSystem.decodeStrings(this.properties.getProperty(prefix + "bindings", ""))) {
            int split = value.indexOf(61);
            if (split <= 0) continue;
            try {
                progress.bindings.put(MmoSlot.valueOf((String)value.substring(0, split)), value.substring(split + 1));
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        }
        for (String value : HymannProfileProgressSystem.decodeStrings(this.properties.getProperty(prefix + "inputs", ""))) {
            int split = value.indexOf(61);
            if (split <= 0) continue;
            try {
                TriggerKey trigger = TriggerKey.parse((String)value.substring(split + 1));
                if (trigger == null) continue;
                progress.inputs.put(MmoSlot.valueOf((String)value.substring(0, split)), trigger);
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        }
        return progress;
    }

    private void write(String prefix, Progress progress) {
        this.properties.setProperty(prefix + "xp", Long.toString(progress.xp));
        this.properties.setProperty(prefix + "fractionalXp", Double.toString(progress.fractionalXp));
        this.properties.setProperty(prefix + "restedXp", Long.toString(progress.restedXp));
        this.properties.setProperty(prefix + "claimed", HymannProfileProgressSystem.encodeStrings(progress.claimedRewards));
        this.properties.setProperty(prefix + "pending", HymannProfileProgressSystem.encodeStrings(progress.pendingRewards.stream().map(String::valueOf).toList()));
        this.properties.setProperty(prefix + "bindings", HymannProfileProgressSystem.encodeStrings(progress.bindings.entrySet().stream().map(entry -> ((MmoSlot)entry.getKey()).name() + "=" + (String)entry.getValue()).toList()));
        this.properties.setProperty(prefix + "inputs", HymannProfileProgressSystem.encodeStrings(progress.inputs.entrySet().stream().map(entry -> ((MmoSlot)entry.getKey()).name() + "=" + ((TriggerKey)entry.getValue()).serialize()).toList()));
    }

    private void load() {
        if (this.storageFile == null || !Files.isRegularFile(this.storageFile, new LinkOption[0])) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(this.storageFile);){
            this.properties.load(reader);
        }
        catch (IOException error) {
            ((HytaleLogger.Api)((HytaleLogger.Api)this.logger.atWarning()).withCause((Throwable)error)).log("Could not read Hymann profile progression: %s", (Object)this.storageFile);
        }
    }

    private synchronized void save() {
        if (this.storageFile == null) {
            return;
        }
        try {
            Path parent = this.storageFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(this.storageFile, new OpenOption[0]);){
                this.properties.store(writer, "Hymann progress per Endless Leveling profile");
            }
        }
        catch (IOException error) {
            ((HytaleLogger.Api)((HytaleLogger.Api)this.logger.atWarning()).withCause((Throwable)error)).log("Could not save Hymann profile progression: %s", (Object)this.storageFile);
        }
    }

    private static void markDirty(Store<EntityStore> store, Ref<EntityStore> entity) {
        MMOSkillTreePlugin.getInstance().players().markDirty(store, entity, PlayerDataDomain.SKILLS);
    }

    private static String key(UUID uuid, int slot, String suffix) {
        return "profile." + String.valueOf(uuid) + "." + slot + "." + suffix;
    }

    private long longValue(String key) {
        try {
            return Long.parseLong(this.properties.getProperty(key, "0"));
        }
        catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private double doubleValue(String key) {
        try {
            return Double.parseDouble(this.properties.getProperty(key, "0"));
        }
        catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private static String encodeStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(String.join((CharSequence)"\u001f", values).getBytes(StandardCharsets.UTF_8));
    }

    private static List<String> decodeStrings(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return List.of();
        }
        try {
            String value = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            return value.isEmpty() ? List.of() : List.of(value.split("\u001f", -1));
        }
        catch (IllegalArgumentException ignored) {
            return List.of();
        }
    }

    private static <T> void putOrRemove(Map<String, T> map, String key, T value) {
        Number number;
        if (value instanceof Number && (number = (Number)value).doubleValue() == 0.0) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    private static <T> void putOrRemoveList(Map<String, List<T>> map, String key, List<T> value) {
        if (value == null || value.isEmpty()) {
            map.remove(key);
        } else {
            map.put(key, new ArrayList<T>(value));
        }
    }

    private static final class Context {
        private SkillComponent component;
        private Store<EntityStore> storeIdentity;
        private int slot;
        private boolean initialized;
        private long lastPersistAtMs;

        private Context() {
        }
    }

    private static final class Progress {
        private long xp;
        private double fractionalXp;
        private long restedXp;
        private final List<String> claimedRewards = new ArrayList<String>();
        private final List<Integer> pendingRewards = new ArrayList<Integer>();
        private final Map<MmoSlot, String> bindings = new ConcurrentHashMap<MmoSlot, String>();
        private final Map<MmoSlot, TriggerKey> inputs = new ConcurrentHashMap<MmoSlot, TriggerKey>();

        private Progress() {
        }

        private boolean isEmpty() {
            return this.xp <= 0L && this.claimedRewards.isEmpty() && this.pendingRewards.isEmpty() && this.bindings.isEmpty();
        }
    }
}
