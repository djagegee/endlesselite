package de.shadow.seuchenweber;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.lang.reflect.Method;

/** Reflection boundary for the optional, installed Perfect Utils 1.1 StunMobAPI. */
final class PerfectUtilsControl {
  private static final String API_CLASS = "com.narwhals.perfectutils.api.StunMobAPI";

  private PerfectUtilsControl() { }

  static boolean applyStun(Store<EntityStore> store, Ref<EntityStore> target,
      long durationMs, Ref<EntityStore> source) {
    if (store == null || target == null || source == null || durationMs <= 0L
        || !target.isValid() || !source.isValid() || target.getStore() != store || source.getStore() != store) return false;
    try {
      Class<?> apiClass = Class.forName(API_CLASS);
      Object api = apiClass.getMethod("get").invoke(null);
      if (api == null) return false;
      Method applyStun = apiClass.getMethod("applyStun",
          Store.class, Ref.class, long.class, Ref.class);
      applyStun.invoke(api, store, target, durationMs, source);
      return true;
    } catch (ReflectiveOperationException | LinkageError | RuntimeException unavailable) {
      return false;
    }
  }
}
