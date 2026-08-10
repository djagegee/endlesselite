package de.shadow.seuchenweber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Idempotently installs Seuchenweber ability definitions without removing unrelated MMO abilities. */
final class SeuchenweberAbilityConfigInstaller {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private SeuchenweberAbilityConfigInstaller() { }

  static int install(Path pluginConfig, Path abilitiesFile) throws IOException {
    JsonObject balance = JsonParser.parseString(Files.readString(pluginConfig, StandardCharsets.UTF_8)).getAsJsonObject();
    JsonObject root = Files.exists(abilitiesFile)
        ? JsonParser.parseString(Files.readString(abilitiesFile, StandardCharsets.UTF_8)).getAsJsonObject()
        : new JsonObject();
    if (!root.has("schemaVersion")) root.addProperty("schemaVersion", 24);
    if (!root.has("enabled")) root.addProperty("enabled", true);
    JsonObject abilities = root.has("abilities") ? root.getAsJsonObject("abilities") : new JsonObject();
    root.add("abilities", abilities);

    JsonObject configured = balance.getAsJsonObject("abilities");
    abilities.add("seuchenweber_seal_of_decay", active(
        "SEUCHENWEBER_SEAL_OF_DECAY", "Siegel der Fäulnis",
        "Markiert ein feindliches Ziel mit Nekrotoxin.", "Seuchenweber_Skill_Seal_Of_Decay",
        configured.getAsJsonObject("seal_of_decay"), "range", "castRangeBlocks", "stacks",
        "nekrotoxinStacksApplied", "directDamage", "baseDamage"));
    abilities.add("seuchenweber_astral_rift", active(
        "SEUCHENWEBER_ASTRAL_RIFT", "Astralriss",
        "Teleportiert durch einen Riss und infiziert nahe Feinde.", "Seuchenweber_Skill_Astral_Rift",
        configured.getAsJsonObject("astral_rift"), "teleportRange", "teleportRangeBlocks",
        "riftDurationMs", "durationSeconds", null, null));
    abilities.add("seuchenweber_chronoblight", active(
        "SEUCHENWEBER_CHRONOBLIGHT", "Chronofäule",
        "Vertieft eigene Nekrotoxin-Zeichen.", "Seuchenweber_Skill_Chronoblight",
        configured.getAsJsonObject("chronoblight"), "range", "castRangeBlocks", "stacks",
        "nekrotoxinStacksApplied", "fullMarkStunMs", "fullMarkStunSeconds"));

    addPassive(abilities, "seuchenweber_passive_necrotoxic_mastery", "Pesthauch",
        "Alle 2 Sekunden erhalten feindliche Ziele im Umkreis von 8 Blöcken einen Nekrotoxin-Stapel.",
        "Seuchenweber_Skill_Necrotoxic_Mastery");
    addPassive(abilities, "seuchenweber_passive_astral_echo", "Astrales Echo",
        "Überträgt begrenzt ein Zeichen auf einen nahen Feind.", "Seuchenweber_Skill_Astral_Echo");
    addPassive(abilities, "seuchenweber_passive_soul_diagnosis", "Seelendiagnose",
        "Zeigt stark markierte Ziele nur ihrem Besitzer an.", "Seuchenweber_Skill_Soul_Diagnosis");
    addPassive(abilities, "seuchenweber_passive_relic_attunement", "Reliktresonanz",
        "Natürlich ablaufende volle Zeichen erstatten Ressourcen.", "Seuchenweber_Skill_Relic_Attunement");

    Files.createDirectories(abilitiesFile.getParent());
    Path backup = abilitiesFile.resolveSibling(abilitiesFile.getFileName() + ".seuchenweber-before.bak");
    if (Files.exists(abilitiesFile) && Files.notExists(backup)) Files.copy(abilitiesFile, backup);
    Path temporary = abilitiesFile.resolveSibling(abilitiesFile.getFileName() + ".seuchenweber.tmp");
    Files.writeString(temporary, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
    try {
      Files.move(temporary, abilitiesFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException unsupported) {
      Files.move(temporary, abilitiesFile, StandardCopyOption.REPLACE_EXISTING);
    }
    return 7;
  }

  private static JsonObject active(String effect, String name, String description, String icon,
      JsonObject config, String param1, String key1, String param2, String key2,
      String param3, String key3) {
    JsonObject ability = common(effect, name, description, icon);
    ability.addProperty("cooldownMs", Math.round(config.get("cooldownSeconds").getAsDouble() * 1000.0));
    ability.addProperty("costStat", "MANA");
    ability.addProperty("costAmount", config.get("manaCost").getAsInt());
    JsonObject params = new JsonObject();
    params.addProperty("useCustomCardDescription", true);
    addConvertedParam(params, param1, config, key1);
    addConvertedParam(params, param2, config, key2);
    addConvertedParam(params, param3, config, key3);
    ability.add("params", params);
    return ability;
  }

  private static void addConvertedParam(JsonObject params, String outputKey, JsonObject source, String sourceKey) {
    if (outputKey == null || sourceKey == null) return;
    double value = source.get(sourceKey).getAsDouble();
    if (outputKey.endsWith("Ms")) value *= 1000.0;
    if (value == Math.rint(value)) params.addProperty(outputKey, (long) value);
    else params.addProperty(outputKey, value);
  }

  private static void addPassive(JsonObject abilities, String id, String name, String description, String icon) {
    JsonObject ability = common("NEXT_HIT_BUFF", name, description, icon);
    ability.addProperty("passive", true);
    JsonObject params = new JsonObject();
    params.addProperty("useCustomCardDescription", true);
    params.addProperty("damageMultiplier", 1.0);
    params.addProperty("armedDurationMs", 1);
    JsonObject trigger = new JsonObject();
    trigger.addProperty("event", "seuchenweber_internal");
    params.add("passiveTrigger", trigger);
    ability.add("params", params);
    abilities.add(id, ability);
  }

  private static JsonObject common(String effect, String name, String description, String icon) {
    JsonObject ability = new JsonObject();
    ability.addProperty("cooldownMs", 0);
    ability.addProperty("effect", effect);
    ability.addProperty("displayName", name);
    ability.addProperty("description", description);
    ability.addProperty("icon", icon);
    JsonArray xpSkills = new JsonArray();
    xpSkills.add(SeuchenweberMmoBridge.SKILL_ID);
    ability.add("xpSkills", xpSkills);
    return ability;
  }
}
