package de.shadow.endlessbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EndlessDetailsData {
  private final Map<String, Object> profile;
  private final List<Map<String, String>> augments;
  private final List<JsonObject> fateSlots;
  private final List<JsonObject> setBonuses;
  private final double currentXp;
  private final double nextLevelXp;

  private EndlessDetailsData(Map<String, Object> profile, List<Map<String, String>> augments,
      List<JsonObject> fateSlots, List<JsonObject> setBonuses, double currentXp, double nextLevelXp) {
    this.profile = profile;
    this.augments = augments;
    this.fateSlots = fateSlots;
    this.setBonuses = setBonuses;
    this.currentXp = currentXp;
    this.nextLevelXp = nextLevelXp;
  }

  public static EndlessDetailsData from(Map<String, Object> profileCard, String fateLoadoutJson,
      double currentXp, double nextLevelXp) {
    Map<String, Object> profile = profileCard == null
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(profileCard));
    List<Map<String, String>> augments = resolvedAugments(profile.get("augments"));
    JsonObject fates = parseObject(fateLoadoutJson);
    return new EndlessDetailsData(profile, augments, objects(fates, "slots"),
        objects(fates, "setBonuses"), currentXp, nextLevelXp);
  }

  private static List<Map<String, String>> resolvedAugments(Object raw) {
    if (!(raw instanceof List<?> list)) return List.of();
    List<Map<String, String>> result = new ArrayList<>();
    for (Object item : list) {
      if (!(item instanceof Map<?, ?> map)) continue;
      Map<String, String> copy = new LinkedHashMap<>();
      map.forEach((key, value) -> copy.put(String.valueOf(key), value == null ? "" : String.valueOf(value)));
      result.add(Collections.unmodifiableMap(copy));
    }
    return List.copyOf(result);
  }

  private static JsonObject parseObject(String json) {
    if (json == null || json.isBlank()) return new JsonObject();
    try {
      JsonElement parsed = JsonParser.parseString(json);
      return parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
    } catch (RuntimeException ignored) {
      return new JsonObject();
    }
  }

  private static List<JsonObject> objects(JsonObject root, String key) {
    JsonArray array = root.has(key) && root.get(key).isJsonArray() ? root.getAsJsonArray(key) : new JsonArray();
    List<JsonObject> result = new ArrayList<>();
    for (JsonElement element : array) if (element.isJsonObject()) result.add(element.getAsJsonObject().deepCopy());
    return List.copyOf(result);
  }

  public Map<String, Object> profile() { return profile; }
  public List<Map<String, String>> augments() { return augments; }
  public List<JsonObject> fateSlots() { return fateSlots; }
  public List<JsonObject> setBonuses() { return setBonuses; }
  public double currentXp() { return currentXp; }
  public double nextLevelXp() { return nextLevelXp; }
}
