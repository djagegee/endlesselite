package de.shadow.seuchenweber;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.ziggfreed.mmoskilltree.ability.ActiveAbilityService;
import com.ziggfreed.mmoskilltree.config.ActiveAbilitiesConfig;
import com.ziggfreed.mmoskilltree.i18n.LocalizationConfig;
import java.nio.file.Path;
import java.util.Map;

/** Hytale entry point. Runtime systems are registered only after their dedicated contracts pass. */
public final class SeuchenweberPlugin extends JavaPlugin {
  static final String PERMISSION = "seuchenweber.use";
  private NekrotoxinDamageSystem nekrotoxinDamageSystem;
  private AstralRiftPulseSystem astralRiftPulseSystem;
  private SeuchenweberMaintenanceSystem maintenanceSystem;
  private SeuchenweberUnlockSystem unlockSystem;
  private PesthauchAuraSystem pesthauchAuraSystem;
  private SeuchenweberClassAuraSystem classAuraSystem;
  private SeuchenweberMmoBridge mmoBridge;
  private CharacterClassDefinition classDefinition;

  public SeuchenweberPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    PermissionsModule.registerPermission(PERMISSION);
    try {
      SeuchenweberRuntimeConfig config = SeuchenweberRuntimeConfig.load(getDataDirectory());
      Path modsDirectory = getFile().getParent();
      int installedAbilities = SeuchenweberAbilityConfigInstaller.install(
          getDataDirectory().resolve("seuchenweber.json"),
          modsDirectory.resolve("mmoskilltree").resolve("abilities.json"));
      int installedPresentationKeys = SeuchenweberPresentationInstaller.install(
          modsDirectory.resolve("mmoskilltree"));
      LocalizationConfig.getInstance().reload();
      classDefinition = SeuchenweberClassDefinition.create();
      EndlessLevelingAPI.get().registerClass(classDefinition, true);
      mmoBridge = new SeuchenweberMmoBridge(getLogger());
      mmoBridge.register();
      nekrotoxinDamageSystem = new NekrotoxinDamageSystem(
          config.maxStacksPerOwner(), config.tickIntervalMs(), config.baseTickDamage(),
          config.masteryTickDamageMultiplier(), config.astralEchoMaxTargetsPerTick(),
          config.astralEchoRadiusBlocks(), config.astralEchoStacksApplied(), config.toxinDurationMs(),
          config.soulDiagnosisStackThreshold(), config.relicManaRefundAmount(), config.relicCooldownRefundMs());
      nekrotoxinDamageSystem.register((ComponentRegistryProxy<EntityStore>) getEntityStoreRegistry());
      pesthauchAuraSystem = new PesthauchAuraSystem(nekrotoxinDamageSystem, config.toxinDurationMs(),
          config.pesthauchRadiusBlocks(), config.pesthauchPulseIntervalMs());
      pesthauchAuraSystem.register((ComponentRegistryProxy<EntityStore>) getEntityStoreRegistry());
      classAuraSystem = new SeuchenweberClassAuraSystem();
      classAuraSystem.register((ComponentRegistryProxy<EntityStore>) getEntityStoreRegistry());
      astralRiftPulseSystem = new AstralRiftPulseSystem(config.riftPulseIntervalMs(), nekrotoxinDamageSystem,
          config.riftRadiusBlocks(), config.riftMaxTargetsPerPulse(), config.riftPulseStacksApplied(),
          config.toxinDurationMs());
      astralRiftPulseSystem.register((ComponentRegistryProxy<EntityStore>) getEntityStoreRegistry());
      maintenanceSystem = new SeuchenweberMaintenanceSystem(nekrotoxinDamageSystem, astralRiftPulseSystem);
      maintenanceSystem.register((ComponentRegistryProxy<EntityStore>) getEntityStoreRegistry());
      unlockSystem = new SeuchenweberUnlockSystem(getLogger());
      unlockSystem.register((ComponentRegistryProxy<EntityStore>) getEntityStoreRegistry());
      getEventRegistry().registerGlobal(RemoveWorldEvent.class, event -> {
        if (event.getWorld() == null || event.getWorld().getEntityStore() == null) return;
        var entityStore = event.getWorld().getEntityStore().getStore();
        pesthauchAuraSystem.release(entityStore);
        astralRiftPulseSystem.release(entityStore);
        nekrotoxinDamageSystem.release(entityStore);
      });
      for (Map.Entry<String, AbilityEffect> entry : SeuchenweberAbilityRegistry
          .createRuntimeEffects(nekrotoxinDamageSystem, config.toxinDurationMs(), astralRiftPulseSystem,
              config.riftDurationMs()).entrySet()) {
        ActiveAbilityService.getInstance().register(entry.getKey(), entry.getValue());
      }
      ActiveAbilitiesConfig.getInstance().load();
      ((HytaleLogger.Api) getLogger().atInfo()).log(
          "Shadow:Seuchenweber runtime initialized: class elite_plagueweaver, Cosmic Ruin unlock at Prestige 30, %s MMO ability definitions, %s presentation keys per locale, 3 active abilities, 4 passive hooks",
          installedAbilities, installedPresentationKeys);
    } catch (Throwable error) {
      ((HytaleLogger.Api) ((HytaleLogger.Api) getLogger().atSevere()).withCause(error))
          .log("Shadow:Seuchenweber runtime registration failed; no deployment acceptance granted");
    }
  }

  @Override
  protected void shutdown() {
    if (pesthauchAuraSystem != null) {
      pesthauchAuraSystem.clear();
      pesthauchAuraSystem = null;
    }
    if (classAuraSystem != null) {
      classAuraSystem.clear();
      classAuraSystem = null;
    }
    if (unlockSystem != null) {
      unlockSystem.clear();
      unlockSystem = null;
    }
    mmoBridge = null;
    if (classDefinition != null) {
      EndlessLevelingAPI.get().unregisterClass(classDefinition.getId());
      classDefinition = null;
    }
    if (astralRiftPulseSystem != null) {
      astralRiftPulseSystem.clear();
      astralRiftPulseSystem = null;
    }
    maintenanceSystem = null;
    if (nekrotoxinDamageSystem != null) {
      nekrotoxinDamageSystem.clear();
      nekrotoxinDamageSystem = null;
    }
    ((HytaleLogger.Api) getLogger().atInfo()).log("Shadow:Seuchenweber runtime shut down and ephemeral state cleared");
  }
}
