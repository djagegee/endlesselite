package de.shadow.endlessbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EndlessDetailsPresenter {
  public record DetailRow(String label, String value, String details) {}

  private final EndlessDetailsData data;

  public EndlessDetailsPresenter(EndlessDetailsData data) {
    this.data = data == null ? EndlessDetailsData.from(null, null, 0.0, 0.0) : data;
  }

  public List<DetailRow> attributeRows() {
    Object raw = data.profile().get("attrTotals");
    if (!(raw instanceof Map<?, ?> attributes)) return List.of();
    List<DetailRow> rows = new ArrayList<>();
    attributes.forEach((name, value) -> rows.add(new DetailRow(text(name), text(value), "")));
    return List.copyOf(rows);
  }

  public List<DetailRow> augmentRows() {
    List<DetailRow> rows = new ArrayList<>();
    for (Map<String, String> augment : data.augments()) {
      String name = augment.getOrDefault("name", augment.getOrDefault("id", "AUGMENT"));
      String tier = augment.getOrDefault("tier", "");
      String value = augment.getOrDefault("value", "");
      String label = tier.isBlank() ? name : name + " · " + tier;
      rows.add(new DetailRow(label, value, augment.getOrDefault("details", "")));
    }
    return List.copyOf(rows);
  }

  public List<DetailRow> fateRows() {
    List<DetailRow> rows = new ArrayList<>();
    for (JsonObject slot : data.fateSlots()) {
      String slotName = string(slot, "slot", "FATE");
      String setName = string(slot, "setName", "");
      addStat(rows, slotName, setName, object(slot, "mainStat"), "MAIN");
      JsonArray substats = array(slot, "substats");
      for (JsonElement element : substats) if (element.isJsonObject())
        addStat(rows, slotName, setName, element.getAsJsonObject(), "SUB");
    }
    return List.copyOf(rows);
  }

  public List<DetailRow> setBonusRows() {
    List<DetailRow> rows = new ArrayList<>();
    for (JsonObject bonus : data.setBonuses()) {
      String setName = string(bonus, "setName", string(bonus, "setId", "SET"));
      for (JsonElement element : array(bonus, "twoPiece")) {
        if (!element.isJsonObject()) continue;
        JsonObject stat = element.getAsJsonObject();
        rows.add(new DetailRow(setName + " · 2PC · " + string(stat, "stat", "BONUS"),
            resolvedValue(stat), ""));
      }
      if (bonus.has("fourPieceDesc") && !string(bonus, "fourPieceDesc", "").isBlank()) {
        rows.add(new DetailRow(setName + " · 4PC", "ACTIVE", string(bonus, "fourPieceDesc", "")));
      }
    }
    return List.copyOf(rows);
  }

  private static void addStat(List<DetailRow> rows, String slot, String setName, JsonObject stat, String kind) {
    if (stat == null || stat.size() == 0) return;
    String source = setName.isBlank() ? slot : slot + " · " + setName;
    rows.add(new DetailRow(source + " · " + kind + " · " + string(stat, "stat", "STAT"),
        resolvedValue(stat), string(stat, "weaponCategory", "")));
  }

  private static String resolvedValue(JsonObject stat) {
    if (stat.has("formatted")) return string(stat, "formatted", "");
    return stat.has("value") ? stat.get("value").getAsString() : "";
  }

  private static JsonObject object(JsonObject root, String key) {
    return root.has(key) && root.get(key).isJsonObject() ? root.getAsJsonObject(key) : new JsonObject();
  }

  private static JsonArray array(JsonObject root, String key) {
    return root.has(key) && root.get(key).isJsonArray() ? root.getAsJsonArray(key) : new JsonArray();
  }

  private static String string(JsonObject root, String key, String fallback) {
    try { return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsString() : fallback; }
    catch (RuntimeException ignored) { return fallback; }
  }

  private static String text(Object value) { return value == null ? "" : String.valueOf(value); }
}
