package de.shadow.endlessbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AugmentDetailEnricher {
  @FunctionalInterface interface Resolver { String resolve(String augmentId); }

  private AugmentDetailEnricher() {}

  static Map<String, Object> enrich(Map<String, Object> profile, Resolver resolver) {
    if (profile == null || profile.isEmpty()) return profile;
    Object raw = profile.get("augments");
    if (!(raw instanceof List<?> list)) return profile;
    List<Map<String, String>> enriched = new ArrayList<>();
    for (Object item : list) {
      if (!(item instanceof Map<?, ?> map)) continue;
      Map<String, String> copy = new LinkedHashMap<>();
      map.forEach((key, value) -> copy.put(String.valueOf(key), value == null ? "" : String.valueOf(value)));
      String id = copy.getOrDefault("id", "");
      String details = id.isBlank() || resolver == null ? "" : safeResolve(resolver, id);
      if (!details.isBlank()) copy.put("details", details);
      enriched.add(Collections.unmodifiableMap(copy));
    }
    Map<String, Object> result = new LinkedHashMap<>(profile);
    result.put("augments", List.copyOf(enriched));
    return Collections.unmodifiableMap(result);
  }

  private static String safeResolve(Resolver resolver, String id) {
    try {
      String value = resolver.resolve(id);
      return value == null ? "" : value;
    } catch (RuntimeException | LinkageError ignored) {
      return "";
    }
  }
}
