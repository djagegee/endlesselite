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
import com.ziggfreed.mmoskilltree.config.ActiveAbilitiesConfig;
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

    public HymannPlugin(JavaPluginInit init) {
        super(init);
    }

    protected void setup() {
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
            HymannThunderAegisAbility.register(this.getLogger());
            HymannThunderStepAbility.register(this.getLogger());
            HymannStormFuryAbility.register(this.getLogger());
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
            ((HytaleLogger.Api)((HytaleLogger.Api)this.getLogger().atSevere()).withCause(error)).log("Could not register Hymann with Endless Leveling");
        }
    }

    protected void shutdown() {
        if (this.profileProgressSystem != null) {
            this.profileProgressSystem.clear();
            this.profileProgressSystem = null;
        }
        if (this.skillLifecycleSystem != null) {
            this.skillLifecycleSystem.clear();
            this.skillLifecycleSystem = null;
        }
        if (this.mmoBridge != null) {
            this.mmoBridge.unregister();
            this.mmoBridge = null;
        }
        if (this.criticalAttributeSystem != null) {
            this.criticalAttributeSystem.clear();
            this.criticalAttributeSystem = null;
        }
        this.armamentMasterySystem = null;
        this.treeManaRegenSystem = null;
        if (this.combatPassiveSystem != null) {
            this.combatPassiveSystem.clear();
            this.combatPassiveSystem = null;
        }
        if (this.passiveAuraSystem != null) {
            this.passiveAuraSystem.clear();
            this.passiveAuraSystem = null;
        }
        this.staminaSystem = null;
        this.stormFurySystem = null;
        this.stormFuryDamageSystem = null;
        if (this.bossBreakerSystem != null) {
            this.bossBreakerSystem.clear();
            this.bossBreakerSystem = null;
        }
        this.bossBreakerDamageSystem = null;
        this.thunderStepArrivalSystem = null;
        if (this.guardWaveSystem != null) {
            this.guardWaveSystem.clear();
            this.guardWaveSystem = null;
        }
        HymannStormFurySystem.clear();
        HymannThunderStepArrivalSystem.clear();
        HymannRegistrar.unregisterAll();
    }
}

