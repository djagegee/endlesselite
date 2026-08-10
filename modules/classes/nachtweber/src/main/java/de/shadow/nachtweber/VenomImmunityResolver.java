package de.shadow.nachtweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

@FunctionalInterface
interface VenomImmunityResolver {
  boolean isImmune(Store<EntityStore> store,Ref<EntityStore> target);
  static VenomImmunityResolver none(){return (store,target)->false;}
}
