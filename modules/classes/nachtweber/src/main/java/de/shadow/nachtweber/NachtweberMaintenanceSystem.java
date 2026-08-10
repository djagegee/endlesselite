package de.shadow.nachtweber;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import java.util.function.LongSupplier;

final class NachtweberMaintenanceSystem extends TickingSystem<EntityStore> {
  private final NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator;
  private final LongSupplier clock;

  NachtweberMaintenanceSystem(
      NachtweberRuntimeCoordinator<Store<EntityStore>> coordinator, LongSupplier clock) {
    this.coordinator = Objects.requireNonNull(coordinator, "coordinator");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public void tick(float deltaSeconds, int index, Store<EntityStore> store) {
    if (store == null) return;
    long nowMs = clock.getAsLong();
    if (nowMs < 0L) return;
    coordinator.maintain(store, nowMs);
  }
}
