/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.server.core.command.system.AbstractCommand
 *  com.hypixel.hytale.server.core.permissions.PermissionsModule
 *  com.hypixel.hytale.server.core.plugin.JavaPlugin
 *  com.hypixel.hytale.server.core.plugin.JavaPluginInit
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  com.ziggfreed.mmoskilltree.config.ActiveAbilitiesConfig
 */
package de.shadow.hymann;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.ActiveAbilityService;
import com.ziggfreed.mmoskilltree.config.ActiveAbilitiesConfig;
import de.shadow.endlesselite.core.BestEffortCleanup;
import de.shadow.endlesselite.core.MmoOwnedEffectAbi;
import de.shadow.endlesselite.core.OwnedRegistryLifecycle;
import de.shadow.hymann.HymannArmamentMasterySystem;
import de.shadow.hymann.HymannBossBreakerDamageSystem;
import de.shadow.hymann.HymannBossBreakerSystem;
import de.shadow.hymann.HymannClaimCommand;
import de.shadow.hymann.HymannCombatPassiveSystem;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannCriticalAttributeSystem;
import de.shadow.hymann.HymannGuardWaveSystem;
import de.shadow.hymann.HymannMmoBridge;
import de.shadow.hymann.HymannPassiveAuraSystem;
import de.shadow.hymann.HymannProfileProgressSystem;
import de.shadow.hymann.HymannRegistrar;
import de.shadow.hymann.HymannSkillLifecycleSystem;
import de.shadow.hymann.HymannStaminaSystem;
import de.shadow.hymann.HymannStormFuryAbility;
import de.shadow.hymann.HymannStormFuryDamageSystem;
import de.shadow.hymann.HymannStormFurySystem;
import de.shadow.hymann.HymannThunderAegisAbility;
import de.shadow.hymann.HymannThunderStepAbility;
import de.shadow.hymann.HymannThunderStepArrivalSystem;
import de.shadow.hymann.HymannTreeManaRegenSystem;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HymannPlugin
extends JavaPlugin {
    private HymannMmoBridge mmoBridge;
    private HymannProfileProgressSystem profileProgressSystem;
    private HymannSkillLifecycleSystem skillLifecycleSystem;
    private HymannArmamentMasterySystem armamentMasterySystem;
    private HymannTreeManaRegenSystem treeManaRegenSystem;
    private HymannCombatPassiveSystem combatPassiveSystem;
    private HymannPassiveAuraSystem passiveAuraSystem;
    private HymannCriticalAttributeSystem criticalAttributeSystem;
    private HymannStaminaSystem staminaSystem;
    private HymannStormFurySystem stormFurySystem;
    private HymannStormFuryDamageSystem stormFuryDamageSystem;
    private HymannBossBreakerSystem bossBreakerSystem;
    private HymannBossBreakerDamageSystem bossBreakerDamageSystem;
    private HymannThunderStepArrivalSystem thunderStepArrivalSystem;
    private HymannGuardWaveSystem guardWaveSystem;
    private OwnedRegistryLifecycle<AbilityEffect> effectLifecycle;

    public HymannPlugin(JavaPluginInit init) {
        super(init);
    }

    protected void setup() {
        MmoOwnedEffectAbi.requireAvailable(HymannPlugin.class.getClassLoader());
        PermissionsModule.registerPermission((String)"shadow.hymann.worthy");
        this.getCommandRegistry().registerCommand((AbstractCommand)new HymannClaimCommand());
        try {
            Path modsDirectory = this.getFile().getParent();
            HymannConfig.load(modsDirectory.resolve("Hymann").resolve("config").resolve("hymann.properties"), this.getLogger());
            HymannConfig.applyAbilitySettings(modsDirectory.resolve("mmoskilltree").resolve("abilities.json"), this.getLogger());
            int registered = HymannRegistrar.registerAll();
            ((HytaleLogger.Api)this.getLogger().atInfo()).log("Registered %s Hymann ascension stages with Endless Leveling", registered);
            this.mmoBridge = new HymannMmoBridge(this.getLogger());
            this.mmoBridge.register();
            this.effectLifecycle = new OwnedRegistryLifecycle<>(new MmoEffectRegistry(), createEffects());
            if (!this.effectLifecycle.start()) {
                throw new IllegalStateException("Hymann effect discriminator collision");
            }
            ActiveAbilitiesConfig.getInstance().load();
            Path hymannDataDirectory = modsDirectory.resolve("Hymann").resolve("data");
            this.profileProgressSystem = new HymannProfileProgressSystem(this.getLogger(),
                    hymannDataDirectory.resolve("profile-progress.properties"));
            this.profileProgressSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.skillLifecycleSystem = new HymannSkillLifecycleSystem(this.getLogger());
            this.skillLifecycleSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.criticalAttributeSystem = new HymannCriticalAttributeSystem(this.getLogger());
            this.criticalAttributeSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.armamentMasterySystem = new HymannArmamentMasterySystem(this.getLogger());
            this.armamentMasterySystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.treeManaRegenSystem = new HymannTreeManaRegenSystem(this.getLogger());
            this.treeManaRegenSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.combatPassiveSystem = new HymannCombatPassiveSystem(this.getLogger());
            this.combatPassiveSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.passiveAuraSystem = new HymannPassiveAuraSystem(this.getLogger());
            this.passiveAuraSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.staminaSystem = new HymannStaminaSystem(this.getLogger());
            this.staminaSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.stormFurySystem = new HymannStormFurySystem(this.getLogger());
            this.stormFurySystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.stormFuryDamageSystem = new HymannStormFuryDamageSystem(this.getLogger());
            this.stormFuryDamageSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.bossBreakerSystem = new HymannBossBreakerSystem(this.getLogger());
            this.bossBreakerSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.bossBreakerDamageSystem = new HymannBossBreakerDamageSystem();
            this.bossBreakerDamageSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.thunderStepArrivalSystem = new HymannThunderStepArrivalSystem(this.getLogger());
            this.thunderStepArrivalSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
            this.guardWaveSystem = new HymannGuardWaveSystem(this.getLogger());
            this.guardWaveSystem.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
        }
        catch (Throwable error) {
            rollbackSetup(error);
            ((HytaleLogger.Api)((HytaleLogger.Api)this.getLogger().atSevere()).withCause(error)).log("Could not register Hymann with Endless Leveling");
            throw propagateSetupFailure(error);
        }
    }

    protected void shutdown() {
        BestEffortCleanup.run(
            () -> { var value = this.effectLifecycle; this.effectLifecycle = null; if (value != null) value.shutdown(); },
            () -> { var value = this.profileProgressSystem; this.profileProgressSystem = null; if (value != null) value.clear(); },
            () -> { var value = this.skillLifecycleSystem; this.skillLifecycleSystem = null; if (value != null) value.clear(); },
            () -> { var value = this.mmoBridge; this.mmoBridge = null; if (value != null) value.unregister(); },
            () -> { var value = this.criticalAttributeSystem; this.criticalAttributeSystem = null; if (value != null) value.clear(); },
            () -> this.armamentMasterySystem = null,
            () -> this.treeManaRegenSystem = null,
            () -> { var value = this.combatPassiveSystem; this.combatPassiveSystem = null; if (value != null) value.clear(); },
            () -> { var value = this.passiveAuraSystem; this.passiveAuraSystem = null; if (value != null) value.clear(); },
            () -> this.staminaSystem = null,
            () -> this.stormFurySystem = null,
            () -> this.stormFuryDamageSystem = null,
            () -> { var value = this.bossBreakerSystem; this.bossBreakerSystem = null; if (value != null) value.clear(); },
            () -> this.bossBreakerDamageSystem = null,
            () -> this.thunderStepArrivalSystem = null,
            () -> { var value = this.guardWaveSystem; this.guardWaveSystem = null; if (value != null) value.clear(); },
            HymannStormFurySystem::clear,
            HymannThunderStepArrivalSystem::clear,
            HymannRegistrar::unregisterAll);
    }

    private void rollbackSetup(Throwable error) {
        try {
            shutdown();
        } catch (Throwable cleanupFailure) {
            if (cleanupFailure != error) error.addSuppressed(cleanupFailure);
        }
    }

    private static RuntimeException propagateSetupFailure(Throwable error) {
        if (error instanceof RuntimeException runtime) return runtime;
        if (error instanceof Error fatal) throw fatal;
        return new IllegalStateException("Hymann setup failed", error);
    }

    private static Map<String, AbilityEffect> createEffects() {
        Map<String, AbilityEffect> effects = new LinkedHashMap<>();
        effects.put(HymannThunderAegisAbility.EFFECT_ID, HymannThunderAegisAbility.create());
        effects.put(HymannThunderStepAbility.EFFECT_ID, HymannThunderStepAbility.create());
        effects.put(HymannStormFuryAbility.EFFECT_ID, HymannStormFuryAbility.create());
        return effects;
    }

    private static final class MmoEffectRegistry implements OwnedRegistryLifecycle.Registry<AbilityEffect> {
        private final ActiveAbilityService service = ActiveAbilityService.getInstance();
        @Override public boolean registerIfAbsent(String id, AbilityEffect effect) {
            return service.registerIfAbsent(id, effect);
        }
        @Override public boolean unregister(String id, AbilityEffect expectedEffect) {
            return service.unregister(id, expectedEffect);
        }
    }
}
