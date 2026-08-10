package de.shadow.seuchenweber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Backup-first migration of public config units and verified unchanged combat defaults. */
final class SeuchenweberConfigMigration {
  private static final Pattern SCHEMA_ONE = Pattern.compile("(\"schemaVersion\"\\s*:\\s*)1(?![0-9])");
  private static final Pattern SCHEMA_TWO = Pattern.compile("(\"schemaVersion\"\\s*:\\s*)2(?![0-9])");
  private static final Pattern SCHEMA_THREE = Pattern.compile("(\"schemaVersion\"\\s*:\\s*)3(?![0-9])");
  private static final Map<String, String> MILLISECOND_KEYS = Map.ofEntries(
      Map.entry("cooldownMilliseconds", "cooldownSeconds"),
      Map.entry("duration_ms", "durationSeconds"),
      Map.entry("tick_interval_ms", "tickIntervalSeconds"),
      Map.entry("cooldown_ms", "cooldownSeconds"),
      Map.entry("rift_duration_ms", "durationSeconds"),
      Map.entry("pulse_interval_ms", "pulseIntervalSeconds"),
      Map.entry("full_mark_stun_ms", "fullMarkStunSeconds"),
      Map.entry("cooldown_refund_ms", "cooldownRefundSeconds"));
  private static final Map<String, String> PERCENT_KEYS = Map.of(
      "damagePercent", "damageMultiplier");

  private SeuchenweberConfigMigration() { }

  static Result migrate(Path config) throws IOException {
    String original = Files.readString(config, StandardCharsets.UTF_8);
    boolean fromOne = SCHEMA_ONE.matcher(original).find();
    boolean fromTwo = SCHEMA_TWO.matcher(original).find();
    boolean fromThree = SCHEMA_THREE.matcher(original).find();
    Path schemaOneBackup = config.resolveSibling(config.getFileName() + ".schema-v1.bak");
    Path schemaTwoBackup = config.resolveSibling(config.getFileName() + ".schema-v2.bak");
    Path schemaThreeBackup = config.resolveSibling(config.getFileName() + ".schema-v3.bak");
    Path backup = fromOne ? schemaOneBackup : (fromTwo ? schemaTwoBackup : schemaThreeBackup);
    if (!fromOne && !fromTwo && !fromThree) {
      if (Files.exists(schemaOneBackup)) backup = schemaOneBackup;
      else if (Files.exists(schemaTwoBackup)) backup = schemaTwoBackup;
      else if (Files.exists(schemaThreeBackup)) backup = schemaThreeBackup;
      return new Result(false, backup, List.of());
    }
    if (!Files.exists(backup)) Files.copy(config, backup);

    String migrated = original;
    List<String> changes = new ArrayList<>();
    if (fromOne) {
      migrated = SCHEMA_ONE.matcher(migrated).replaceFirst("$1" + "2");
      for (Map.Entry<String, String> entry : MILLISECOND_KEYS.entrySet()) {
        Migration replacement = convertKnownNumber(migrated, entry.getKey(), entry.getValue(), 1000.0);
        migrated = replacement.content();
        changes.addAll(replacement.changes());
      }
      for (Map.Entry<String, String> entry : PERCENT_KEYS.entrySet()) {
        Migration replacement = convertKnownNumber(migrated, entry.getKey(), entry.getValue(), 100.0);
        migrated = replacement.content();
        changes.addAll(replacement.changes());
      }
    }
    migrated = SCHEMA_TWO.matcher(migrated).replaceFirst("$1" + "3");
    Migration cadence = replaceUnchangedDefault(migrated, "tickIntervalSeconds", 1.0, 5.0);
    migrated = cadence.content();
    changes.addAll(cadence.changes());
    Migration spread = replaceUnchangedDefault(migrated, "maximumEchoTargetsPerTick", 1.0, 2.0);
    migrated = spread.content();
    changes.addAll(spread.changes());
    migrated = SCHEMA_THREE.matcher(migrated).replaceFirst("$1" + "4");
    Migration pesthauch = addMissingPesthauchDefaults(migrated);
    migrated = pesthauch.content();
    changes.addAll(pesthauch.changes());

    Path temporary = config.resolveSibling(config.getFileName() + ".schema-v4.tmp");
    Files.writeString(temporary, migrated, StandardCharsets.UTF_8);
    try {
      Files.move(temporary, config, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException unsupported) {
      Files.move(temporary, config, StandardCopyOption.REPLACE_EXISTING);
    }
    return new Result(true, backup, List.copyOf(changes));
  }

  private static Migration addMissingPesthauchDefaults(String content) {
    int keyIndex = content.indexOf("\"necrotoxic_mastery\"");
    int start = keyIndex < 0 ? -1 : content.indexOf('{', keyIndex);
    if (start < 0) return new Migration(content, List.of());
    int end = matchingObjectEnd(content, start);
    if (end < 0) return new Migration(content, List.of());
    String body = content.substring(start + 1, end);
    List<String> entries = new ArrayList<>();
    List<String> changes = new ArrayList<>();
    if (!body.contains("\"auraRadiusBlocks\"")) {
      entries.add("\"auraRadiusBlocks\": 8.0");
      changes.add("passives.necrotoxic_mastery.auraRadiusBlocks=<missing> -> 8.0");
    }
    if (!body.contains("\"auraPulseIntervalSeconds\"")) {
      entries.add("\"auraPulseIntervalSeconds\": 2.0");
      changes.add("passives.necrotoxic_mastery.auraPulseIntervalSeconds=<missing> -> 2.0");
    }
    if (entries.isEmpty()) return new Migration(content, List.of());

    int lineStart = content.lastIndexOf('\n', keyIndex);
    String keyIndent = lineStart < 0 ? "" : content.substring(lineStart + 1, keyIndex);
    String childIndent = keyIndent + "  ";
    int contentEnd = end;
    while (contentEnd > start + 1 && Character.isWhitespace(content.charAt(contentEnd - 1))) contentEnd--;
    String joined = String.join(",\n" + childIndent, entries);
    String insertion = contentEnd == start + 1
        ? "\n" + childIndent + joined + "\n" + keyIndent
        : ",\n" + childIndent + joined;
    String migrated = content.substring(0, contentEnd) + insertion + content.substring(contentEnd);
    return new Migration(migrated, List.copyOf(changes));
  }

  private static int matchingObjectEnd(String content, int start) {
    int depth = 0;
    boolean quoted = false;
    boolean escaped = false;
    for (int index = start; index < content.length(); index++) {
      char current = content.charAt(index);
      if (quoted) {
        if (escaped) escaped = false;
        else if (current == '\\') escaped = true;
        else if (current == '"') quoted = false;
      } else if (current == '"') quoted = true;
      else if (current == '{') depth++;
      else if (current == '}' && --depth == 0) return index;
    }
    return -1;
  }

  private static Migration replaceUnchangedDefault(String content, String key,
      double oldDefault, double newDefault) {
    Pattern pattern = Pattern.compile("(\"" + Pattern.quote(key)
        + "\"\\s*:\\s*)([0-9]+(?:\\.[0-9]+)?)");
    Matcher matcher = pattern.matcher(content);
    if (!matcher.find() || Double.compare(Double.parseDouble(matcher.group(2)), oldDefault) != 0) {
      return new Migration(content, List.of());
    }
    String oldValue = matcher.group(2);
    String formatted = OperatorConfigNumbers.formatForStorage(newDefault);
    String replaced = matcher.replaceFirst(Matcher.quoteReplacement(matcher.group(1) + formatted));
    return new Migration(replaced, List.of(key + "=" + oldValue + " -> " + key + "=" + formatted));
  }

  private static Migration convertKnownNumber(String content, String oldKey, String newKey, double divisor) {
    Pattern pattern = Pattern.compile("\"" + Pattern.quote(oldKey) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    Matcher matcher = pattern.matcher(content);
    StringBuffer output = new StringBuffer();
    List<String> changes = new ArrayList<>();
    while (matcher.find()) {
      double oldValue = Double.parseDouble(matcher.group(1));
      double newValue = oldValue / divisor;
      String formatted = OperatorConfigNumbers.formatForStorage(newValue);
      matcher.appendReplacement(output, Matcher.quoteReplacement("\"" + newKey + "\": " + formatted));
      changes.add(oldKey + "=" + matcher.group(1) + " -> " + newKey + "=" + formatted);
    }
    matcher.appendTail(output);
    return new Migration(output.toString(), changes);
  }

  record Result(boolean migrated, Path backup, List<String> changes) { }
  private record Migration(String content, List<String> changes) { }
}
