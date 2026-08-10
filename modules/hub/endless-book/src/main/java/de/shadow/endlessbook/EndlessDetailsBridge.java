package de.shadow.endlessbook;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EndlessDetailsBridge {
  private static final String LEVELING_API = "com.airijko.endlessleveling.api.EndlessLevelingAPI";
  private static final String FATE_MANAGER = "com.airijko.endlessfates.manager.FateManager";

  private EndlessDetailsBridge() {}

  public interface LevelingReader {
    Map<String, Object> profileCard(UUID uuid);
    double currentXp(UUID uuid);
    double nextLevelXp(UUID uuid);
  }

  @FunctionalInterface
  public interface FatesReader { String loadoutJson(UUID uuid); }

  public static EndlessDetailsData load(UUID uuid) {
    return load(uuid, new ReflectiveLevelingReader(), new ReflectiveFatesReader());
  }

  static EndlessDetailsData load(UUID uuid, LevelingReader leveling, FatesReader fates) {
    if (uuid == null) return EndlessDetailsData.from(null, null, 0.0, 0.0);
    Map<String, Object> profile = safeProfile(leveling, uuid);
    String fateJson = safeFates(fates, uuid);
    return EndlessDetailsData.from(profile, fateJson, safeNumber(() -> leveling.currentXp(uuid)),
        safeNumber(() -> leveling.nextLevelXp(uuid)));
  }

  static LevelingReader unavailableLeveling() {
    return new LevelingReader() {
      public Map<String, Object> profileCard(UUID uuid) { return null; }
      public double currentXp(UUID uuid) { return 0.0; }
      public double nextLevelXp(UUID uuid) { return 0.0; }
    };
  }

  static FatesReader unavailableFates() { return uuid -> null; }

  private static Map<String, Object> safeProfile(LevelingReader reader, UUID uuid) {
    try { return reader == null ? null : reader.profileCard(uuid); }
    catch (RuntimeException | LinkageError ignored) { return null; }
  }

  private static String safeFates(FatesReader reader, UUID uuid) {
    try { return reader == null ? null : reader.loadoutJson(uuid); }
    catch (RuntimeException | LinkageError ignored) { return null; }
  }

  private static double safeNumber(NumberCall call) {
    try { return call.get(); }
    catch (RuntimeException | LinkageError ignored) { return 0.0; }
  }

  @FunctionalInterface private interface NumberCall { double get(); }

  private static final class ReflectiveLevelingReader implements LevelingReader {
    @Override public Map<String, Object> profileCard(UUID uuid) {
      Object api = singleton(LEVELING_API);
      if (api == null) return null;
      Object value = invoke(api, "getProfileCard", new Class<?>[]{UUID.class}, uuid);
      if (!(value instanceof Map<?, ?> raw)) return null;
      @SuppressWarnings("unchecked") Map<String, Object> card = (Map<String, Object>) raw;
      return AugmentDetailEnricher.enrich(card, id -> resolvedAugmentDetails(api, id));
    }

    @Override public double currentXp(UUID uuid) {
      return number(invoke(singleton(LEVELING_API), "getPlayerXp", new Class<?>[]{UUID.class}, uuid));
    }

    @Override public double nextLevelXp(UUID uuid) {
      Object api = singleton(LEVELING_API);
      if (api == null) return 0.0;
      int level = (int) number(invoke(api, "getPlayerLevel", new Class<?>[]{UUID.class}, uuid));
      return number(invoke(api, "getXpForNextLevel", new Class<?>[]{UUID.class, int.class}, uuid, level));
    }
  }

  private static final class ReflectiveFatesReader implements FatesReader {
    @Override public String loadoutJson(UUID uuid) {
      Object manager = singleton(FATE_MANAGER);
      Object value = invoke(manager, "buildWebLoadoutJson", new Class<?>[]{UUID.class}, uuid);
      return value instanceof String text ? text : null;
    }
  }

  private static String resolvedAugmentDetails(Object api, String id) {
    if (api == null || id == null || id.isBlank() || id.startsWith("common_stat:")) return "";
    Object definition = invoke(api, "getAugmentDefinition", new Class<?>[]{String.class}, id);
    if (definition == null) return "";
    Object resolver = invoke(definition, "getVarResolver", new Class<?>[0]);
    List<String> lines = new ArrayList<>();
    addResolved(lines, resolver, text(invoke(definition, "getDescription", new Class<?>[0])));
    Object rawSections = invoke(definition, "getUiSections", new Class<?>[0]);
    if (rawSections instanceof List<?> sections) {
      for (Object section : sections) {
        if (section == null) continue;
        String title = resolve(resolver, text(invoke(section, "title", new Class<?>[0])));
        String body = resolve(resolver, text(invoke(section, "body", new Class<?>[0])));
        if (title.isBlank() && body.isBlank()) continue;
        lines.add(title.isBlank() ? body : (body.isBlank() ? title : title + ": " + body));
      }
    }
    return String.join("\n", lines.stream().filter(line -> !line.isBlank()).distinct().toList());
  }

  private static void addResolved(List<String> lines, Object resolver, String raw) {
    String value = resolve(resolver, raw);
    if (!value.isBlank()) lines.add(value);
  }

  private static String resolve(Object resolver, String raw) {
    if (raw == null || raw.isBlank()) return "";
    Object value = invoke(resolver, "substitute", new Class<?>[]{String.class}, raw);
    return value instanceof String resolved && !resolved.isBlank() ? resolved.trim() : raw.trim();
  }

  private static String text(Object value) { return value == null ? "" : String.valueOf(value); }

  private static Object singleton(String className) {
    try {
      Class<?> type = Class.forName(className, true, EndlessDetailsBridge.class.getClassLoader());
      return type.getMethod("get").invoke(null);
    } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
      return null;
    }
  }

  private static Object invoke(Object target, String name, Class<?>[] types, Object... arguments) {
    if (target == null) return null;
    try {
      Method method = target.getClass().getMethod(name, types);
      return method.invoke(target, arguments);
    } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
      return null;
    }
  }

  private static double number(Object value) { return value instanceof Number number ? number.doubleValue() : 0.0; }
}
