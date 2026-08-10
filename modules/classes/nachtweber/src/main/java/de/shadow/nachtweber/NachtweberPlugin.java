package de.shadow.nachtweber;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import com.ziggfreed.mmoskilltree.ability.ActiveAbilityService;
import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.UUID;

/** Store-lifecycle ECS wiring with exact-instance-owned MMOSkillTree effects. */
public final class NachtweberPlugin extends JavaPlugin {
  private NachtweberLifecycle lifecycle;
  private NachtweberRuntimeWiring runtimeWiring;
  private NachtweberOwnedEffectLifecycle effectLifecycle;

  public NachtweberPlugin(JavaPluginInit init) { super(init); }

  @Override
  protected void setup() {
    RuntimePort port = new RuntimePort();
    NachtweberLifecycle candidateLifecycle =
        new NachtweberLifecycle(port, NachtweberClassDefinition.create());
    NachtweberRuntimeWiring candidateWiring = new NachtweberRuntimeWiring(
        System::currentTimeMillis,
        nowMs -> ((HytaleLogger.Api) getLogger().atInfo()).log(
            "Shadow:Nachtweber first bound-store maintenance tick observed at %d ms", nowMs),
        new EndlessLevelingVenomPowerProvider(),
        new DamageCauseReadinessProbe(),
        readiness -> ((HytaleLogger.Api) getLogger().atInfo()).log(
            "Shadow:Nachtweber DamageCause AssetStore ready: cause=%s index=%d; positive damage requires exact-store real owner/target refs",
            readiness.causeId(), readiness.assetIndex()));
    NachtweberOwnedEffectLifecycle candidateEffects = null;
    try {
      candidateLifecycle.start();
      candidateWiring.start(port);
      candidateEffects = new NachtweberOwnedEffectLifecycle(
          new MmoEffectRegistry(),
          candidateWiring.createStoreBoundEffects(VenomImmunityResolver.none()));
      if (!candidateEffects.start()) {
        throw new IllegalStateException("Nachtweber effect discriminator collision");
      }
      lifecycle = candidateLifecycle;
      runtimeWiring = candidateWiring;
      effectLifecycle = candidateEffects;
      ((HytaleLogger.Api) getLogger().atInfo()).log(
          "Shadow:Nachtweber adapter initialized: class elite_nightweaver; "
              + "store lifecycle ECS wired; 3 owned gameplay effects registered");
    } catch (RuntimeException | Error error) {
      if (candidateEffects != null) candidateEffects.shutdown();
      candidateWiring.shutdown();
      candidateLifecycle.shutdown();
      ((HytaleLogger.Api) ((HytaleLogger.Api) getLogger().atSevere()).withCause(error))
          .log("Shadow:Nachtweber runtime registration failed; plugin setup rejected");
      throw error;
    }
  }

  @Override
  protected void shutdown() {
    if (effectLifecycle != null) {
      effectLifecycle.shutdown();
      effectLifecycle = null;
    }
    if (runtimeWiring != null) {
      runtimeWiring.shutdown();
      runtimeWiring = null;
    }
    if (lifecycle != null) {
      lifecycle.shutdown();
      lifecycle = null;
    }
    ((HytaleLogger.Api) getLogger().atInfo()).log("Shadow:Nachtweber adapter shut down");
  }

  private static final class MmoEffectRegistry implements NachtweberOwnedEffectLifecycle.Registry {
    private final ActiveAbilityService service = ActiveAbilityService.getInstance();
    @Override public boolean registerIfAbsent(String discriminator, AbilityEffect effect) {
      return service.registerIfAbsent(discriminator, effect);
    }
    @Override public boolean unregister(String discriminator, AbilityEffect expectedEffect) {
      return service.unregister(discriminator, expectedEffect);
    }
  }

  private final class RuntimePort
      implements NachtweberLifecycle.RuntimePort, NachtweberRuntimeWiring.RegistrationPort {
    @Override public void registerPermission(String permission) {
      PermissionsModule.registerPermission(permission);
    }
    @Override public boolean registerClass(CharacterClassDefinition definition) {
      return EndlessLevelingAPI.get().registerClass(definition, false);
    }
    @Override public boolean unregisterClass(String id) {
      return EndlessLevelingAPI.get().unregisterClass(id);
    }
    @Override public void registerMaintenance(NachtweberMaintenanceSystem system) {
      getEntityStoreRegistry().registerSystem(system);
    }
    @Override public void registerShadowSwingMovement(ShadowSwingMovementSystem system) {
      getEntityStoreRegistry().registerSystem(system);
    }
    @Override public void registerVenomDamage(VenomDamageSystem system) {
      getEntityStoreRegistry().registerSystem(system);
    }
    @Override public void registerWorldStart(Consumer<Store<EntityStore>> handler) {
      getEventRegistry().registerGlobal(
          StartWorldEvent.class,
          event -> handler.accept(event.getWorld().getEntityStore().getStore()));
    }
    @Override public void registerWorldRemoval(Consumer<Store<EntityStore>> handler) {
      getEventRegistry().registerGlobal(
          RemoveWorldEvent.class,
          event -> handler.accept(event.getWorld().getEntityStore().getStore()));
    }
    @Override public void registerPlayerDisconnect(
        BiConsumer<Store<EntityStore>, UUID> handler) {
      getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, event -> {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef == null) return;
        Ref<EntityStore> entityRef = playerRef.getReference();
        handler.accept(entityRef == null ? null : entityRef.getStore(), playerRef.getUuid());
      });
    }
  }
}
