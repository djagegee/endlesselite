package de.shadow.seuchenweber;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Store-level maintenance that runs even when no NPC archetype is ticking. */
final class SeuchenweberMaintenanceSystem extends TickingSystem<EntityStore> {
  private final NekrotoxinDamageSystem nekrotoxin;
  private final AstralRiftPulseSystem astralRift;

  SeuchenweberMaintenanceSystem(NekrotoxinDamageSystem nekrotoxin, AstralRiftPulseSystem astralRift) {
    this.nekrotoxin = nekrotoxin;
    this.astralRift = astralRift;
  }

  void register(ComponentRegistryProxy<EntityStore> registry) {
    registry.registerSystem((ISystem) this);
  }

  @Override public void tick(float deltaTime, int index, Store<EntityStore> store) {
    if (store == null) return;
    long nowMs = System.currentTimeMillis();
    nekrotoxin.sweepExpired(store, nowMs);
    astralRift.expireExpired(store, nowMs);
  }
}
