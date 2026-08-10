package de.shadow.nachtweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.lang.reflect.Method;

final class NachtweberControl {
  private static final String API_CLASS="com.narwhals.perfectutils.api.StunMobAPI";
  private NachtweberControl() { }
  static boolean applyStun(Store<EntityStore> store,Ref<EntityStore> target,long durationMs,Ref<EntityStore> source){
    if(store==null||target==null||source==null||durationMs<=0L||!target.isValid()||!source.isValid()||target.getStore()!=store||source.getStore()!=store)return false;
    try{Class<?> type=Class.forName(API_CLASS);Object api=type.getMethod("get").invoke(null);if(api==null)return false;Method method=type.getMethod("applyStun",Store.class,Ref.class,long.class,Ref.class);method.invoke(api,store,target,durationMs,source);return true;}
    catch(ReflectiveOperationException|LinkageError|RuntimeException unavailable){return false;}
  }
}
