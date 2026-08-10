package de.shadow.nachtweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;

@FunctionalInterface
interface OwnerEntityBindingPort {
  boolean bind(Store<EntityStore> store, UUID owner, Ref<EntityStore> ref);
}
