package de.shadow.endlesselite.core;

import java.lang.reflect.Method;

/** Fail-closed preflight for the additive MMOSkillTree owned-effect ABI. */
public final class MmoOwnedEffectAbi {
  private static final String SERVICE = "com.ziggfreed.mmoskilltree.ability.ActiveAbilityService";
  private static final String EFFECT = "com.ziggfreed.mmoskilltree.ability.AbilityEffect";

  private MmoOwnedEffectAbi() { }

  public static void requireAvailable(ClassLoader loader) {
    try {
      Class<?> serviceType = Class.forName(SERVICE, false, loader);
      Class<?> effectType = Class.forName(EFFECT, false, loader);
      requireMethods(serviceType, effectType);
    } catch (ClassNotFoundException error) {
      throw new IllegalStateException(
          "MMOSkillTree owned-effects ABI is unavailable; install the hash-verified 1.5.2-owned-local artifact",
          error);
    }
  }

  static boolean hasRequiredMethods(Class<?> serviceType, Class<?> effectType) {
    try {
      Method register = serviceType.getMethod("registerIfAbsent", String.class, effectType);
      Method unregister = serviceType.getMethod("unregister", String.class, effectType);
      return register.getReturnType() == boolean.class && unregister.getReturnType() == boolean.class;
    } catch (NoSuchMethodException error) {
      return false;
    }
  }

  static void requireMethods(Class<?> serviceType, Class<?> effectType) {
    if (!hasRequiredMethods(serviceType, effectType)) {
      throw new IllegalStateException(
          "MMOSkillTree owned-effects ABI requires boolean registerIfAbsent(String, AbilityEffect) "
              + "and boolean unregister(String, AbilityEffect)");
    }
  }
}
