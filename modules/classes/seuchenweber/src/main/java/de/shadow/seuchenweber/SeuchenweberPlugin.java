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
import de.shadow.endlesselite.core.BestEffortCleanup;
import de.shadow.endlesselite.core.MmoOwnedEffectAbi;
import de.shadow.endlesselite.core.OwnedRegistryLifecycle;
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
  private OwnedRegistryLifecycle<AbilityEffect> effectLifecycle;

  public SeuchenweberPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    MmoOwnedEffectAbi.requireAvailable(SeuchenweberPlugin.class.getClassLoader());
    try {
      PermissionsModule.registerPermission(PERMISSION);
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
      Map<String, AbilityEffect> effects = SeuchenweberAbilityRegistry.createRuntimeEffects(
          nekrotoxinDamageSystem, config.toxinDurationMs(), astralRiftPulseSystem,
          config.riftDurationMs());
      effectLifecycle = new OwnedRegistryLifecycle<>(new MmoEffectRegistry(), effects);
      if (!effectLifecycle.start()) {
        throw new IllegalStateException("Seuchenweber effect discriminator collision");
      }
      ActiveAbilitiesConfig.getInstance().load();
      ((HytaleLogger.Api) getLogger().atInfo()).log(
          "Shadow:Seuchenweber runtime initialized: class elite_plagueweaver, Cosmic Ruin unlock at Prestige 30, %s MMO ability definitions, %s presentation keys per locale, 3 active abilities, 4 passive hooks",
          installedAbilities, installedPresentationKeys);
    } catch (Throwable error) {
      rollbackSetup(error);
      ((HytaleLogger.Api) ((HytaleLogger.Api) getLogger().atSevere()).withCause(error))
          .log("Shadow:Seuchenweber runtime registration failed; no deployment acceptance granted");
      throw propagateSetupFailure(error);
    }
  }

  @Override
  protected void shutdown() {
    BestEffortCleanup.run(
        () -> { var value = effectLifecycle; effectLifecycle = null; if (value != null) value.shutdown(); },
        () -> { var value = pesthauchAuraSystem; pesthauchAuraSystem = null; if (value != null) value.clear(); },
        () -> { var value = classAuraSystem; classAuraSystem = null; if (value != null) value.clear(); },
        () -> { var value = unlockSystem; unlockSystem = null; if (value != null) value.clear(); },
        () -> mmoBridge = null,
        () -> { var value = classDefinition; classDefinition = null; if (value != null) EndlessLevelingAPI.get().unregisterClass(value.getId()); },
        () -> { var value = astralRiftPulseSystem; astralRiftPulseSystem = null; if (value != null) value.clear(); },
        () -> maintenanceSystem = null,
        () -> { var value = nekrotoxinDamageSystem; nekrotoxinDamageSystem = null; if (value != null) value.clear(); },
        () -> ((HytaleLogger.Api) getLogger().atInfo()).log("Shadow:Seuchenweber runtime shut down and ephemeral state cleared"));
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
    return new IllegalStateException("Seuchenweber setup failed", error);
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
