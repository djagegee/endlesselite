package de.shadow.seuchenweber;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Immutable subset of the central config needed before runtime systems register. */
record SeuchenweberRuntimeConfig(long toxinDurationMs, long tickIntervalMs, int maxStacksPerOwner,
                                 float baseTickDamage, double masteryTickDamageMultiplier,
                                 double pesthauchRadiusBlocks, long pesthauchPulseIntervalMs,
                                 double riftRadiusBlocks, long riftDurationMs,
                                 long riftPulseIntervalMs, int riftMaxTargetsPerPulse,
                                 int riftPulseStacksApplied, int astralEchoMaxTargetsPerTick,
                                 double astralEchoRadiusBlocks, int astralEchoStacksApplied,
                                 int soulDiagnosisStackThreshold, float relicManaRefundAmount,
                                 long relicCooldownRefundMs) {
  private static final String RESOURCE = "config/seuchenweber.json";
  private static final String FILE_NAME = "seuchenweber.json";

  static SeuchenweberRuntimeConfig loadBundled() {
    return parse(readBundled());
  }

  static SeuchenweberRuntimeConfig load(Path dataDirectory) {
    try {
      Files.createDirectories(dataDirectory);
      Path config = dataDirectory.resolve(FILE_NAME);
      if (Files.notExists(config)) writeDefaultAtomically(config, readBundled());
      SeuchenweberConfigMigration.Result migration = SeuchenweberConfigMigration.migrate(config);
      if (migration.migrated()) {
        System.getLogger(SeuchenweberRuntimeConfig.class.getName()).log(System.Logger.Level.INFO,
            "Seuchenweber-Konfiguration auf Schema 4 migriert. Backup: " + migration.backup()
                + "; Änderungen: " + migration.changes());
      }
      return parse(Files.readString(config, StandardCharsets.UTF_8));
    } catch (IOException error) {
      throw new IllegalStateException("Seuchenweber-Konfiguration im Plugin-Datenordner konnte nicht geladen werden", error);
    }
  }

  private static String readBundled() {
    try (InputStream input = SeuchenweberRuntimeConfig.class.getClassLoader().getResourceAsStream(RESOURCE)) {
      if (input == null) throw new IllegalStateException("Missing bundled config: " + RESOURCE);
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException error) {
      throw new IllegalStateException("Cannot read bundled Seuchenweber config", error);
    }
  }

  private static void writeDefaultAtomically(Path config, String content) throws IOException {
    Path temporary = config.resolveSibling(config.getFileName() + ".default.tmp");
    Files.writeString(temporary, content, StandardCharsets.UTF_8);
    try {
      Files.move(temporary, config, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException unsupported) {
      Files.move(temporary, config);
    }
  }

  private static SeuchenweberRuntimeConfig parse(String json) {
    String necrotoxin = requiredObject(json, "necrotoxin");
    String abilities = requiredObject(json, "abilities");
    String rift = requiredObject(abilities, "astral_rift");
    String passives = requiredObject(json, "passives");
    String mastery = requiredObject(passives, "necrotoxic_mastery");
    String astralEcho = requiredObject(passives, "astral_echo");
    String soulDiagnosis = requiredObject(passives, "soul_diagnosis");
    String relicAttunement = requiredObject(passives, "relic_attunement");
    System.Logger logger = System.getLogger(SeuchenweberRuntimeConfig.class.getName());
    java.util.function.Consumer<String> warnings = message -> logger.log(System.Logger.Level.WARNING, message);
    double toxinDurationSeconds = OperatorConfigValidation.decimal("necrotoxin.durationSeconds",
        finiteDouble(necrotoxin, "durationSeconds"), 0.05, 300.0, 10.0, warnings);
    double tickIntervalSeconds = OperatorConfigValidation.decimal("necrotoxin.tickIntervalSeconds",
        finiteDouble(necrotoxin, "tickIntervalSeconds"), 0.05, 60.0, 5.0, warnings);
    long duration = ConfigTimeConversion.secondsToMilliseconds(toxinDurationSeconds);
    long interval = ConfigTimeConversion.secondsToMilliseconds(tickIntervalSeconds);
    int cap = OperatorConfigValidation.integer("necrotoxin.maximumStacksPerOwner",
        wholeNumber(necrotoxin, "maximumStacksPerOwner"), 1, 16, 5, warnings);
    float damage = (float) OperatorConfigValidation.decimal("necrotoxin.baseDamagePerTick",
        finiteDouble(necrotoxin, "baseDamagePerTick"), 0.0, 1000.0, 6.0, warnings);
    double masteryMultiplier = OperatorConfigValidation.decimal("passives.necrotoxic_mastery.tickDamageMultiplier",
        finiteDouble(mastery, "tickDamageMultiplier"), 0.0, 3.0, 1.25, warnings);
    double pesthauchRadius = OperatorConfigValidation.decimal(
        "passives.necrotoxic_mastery.auraRadiusBlocks",
        finiteDouble(mastery, "auraRadiusBlocks"), 0.5, 32.0, 8.0, warnings);
    double pesthauchPulseSeconds = OperatorConfigValidation.decimal(
        "passives.necrotoxic_mastery.auraPulseIntervalSeconds",
        finiteDouble(mastery, "auraPulseIntervalSeconds"), 0.25, 30.0, 2.0, warnings);
    long pesthauchPulseInterval = ConfigTimeConversion.secondsToMilliseconds(pesthauchPulseSeconds);
    double riftRadius = OperatorConfigValidation.decimal("abilities.astral_rift.riftRadiusBlocks",
        finiteDouble(rift, "riftRadiusBlocks"), 0.5, 32.0, 5.0, warnings);
    double riftDurationSeconds = OperatorConfigValidation.decimal("abilities.astral_rift.durationSeconds",
        finiteDouble(rift, "durationSeconds"), 0.05, 60.0, 5.0, warnings);
    double pulseIntervalSeconds = OperatorConfigValidation.decimal("abilities.astral_rift.pulseIntervalSeconds",
        finiteDouble(rift, "pulseIntervalSeconds"), 0.05, 10.0, 1.0, warnings);
    long riftDuration = ConfigTimeConversion.secondsToMilliseconds(riftDurationSeconds);
    long riftPulseInterval = ConfigTimeConversion.secondsToMilliseconds(pulseIntervalSeconds);
    int riftTargetLimit = OperatorConfigValidation.integer("abilities.astral_rift.maximumTargetsPerPulse",
        wholeNumber(rift, "maximumTargetsPerPulse"), 1, RiftPulseBudget.GLOBAL_MAX_TARGETS, 8, warnings);
    int riftPulseStacks = OperatorConfigValidation.integer("abilities.astral_rift.nekrotoxinStacksPerPulse",
        wholeNumber(rift, "nekrotoxinStacksPerPulse"), 1, 16, 1, warnings);
    int echoTargetLimit = OperatorConfigValidation.integer("passives.astral_echo.maximumEchoTargetsPerTick",
        wholeNumber(astralEcho, "maximumEchoTargetsPerTick"), 1, AstralEchoBudget.GLOBAL_MAX_TARGETS, 2, warnings);
    double echoRadius = OperatorConfigValidation.decimal("passives.astral_echo.echoRadiusBlocks",
        finiteDouble(astralEcho, "echoRadiusBlocks"), 0.5, 32.0, 5.0, warnings);
    int echoStacks = OperatorConfigValidation.integer("passives.astral_echo.echoStacksApplied",
        wholeNumber(astralEcho, "echoStacksApplied"), 1, 16, 1, warnings);
    int diagnosisThreshold = OperatorConfigValidation.integer("passives.soul_diagnosis.diagnosisStackThreshold",
        wholeNumber(soulDiagnosis, "diagnosisStackThreshold"), 1, cap, 3, warnings);
    float manaRefund = (float) OperatorConfigValidation.decimal("passives.relic_attunement.manaRefundAmount",
        finiteDouble(relicAttunement, "manaRefundAmount"), 0.0, 1000.0, 8.0, warnings);
    double cooldownRefundSeconds = OperatorConfigValidation.decimal(
        "passives.relic_attunement.cooldownRefundSeconds",
        finiteDouble(relicAttunement, "cooldownRefundSeconds"), 0.0, 60.0, 1.2, warnings);
    long cooldownRefund = ConfigTimeConversion.secondsToMilliseconds(cooldownRefundSeconds);
    return new SeuchenweberRuntimeConfig(duration, interval, cap, damage, masteryMultiplier,
        pesthauchRadius, pesthauchPulseInterval, riftRadius, riftDuration,
        riftPulseInterval, riftTargetLimit, riftPulseStacks, echoTargetLimit, echoRadius, echoStacks,
        diagnosisThreshold, manaRefund, cooldownRefund);
  }

  private static int wholeNumber(String json, String key) {
    double parsed = OperatorConfigNumbers.requiredDouble(json, key);
    if (parsed == Math.rint(parsed) && parsed >= Integer.MIN_VALUE && parsed <= Integer.MAX_VALUE) return (int) parsed;
    throw new IllegalStateException("Config key must be a whole number: " + key + " (received " + parsed + ")");
  }

  private static double finiteDouble(String json, String key) {
    return OperatorConfigNumbers.requiredDouble(json, key);
  }

  private static String requiredObject(String json, String key) {
    int keyIndex = json.indexOf("\"" + key + "\"");
    int start = keyIndex < 0 ? -1 : json.indexOf('{', keyIndex);
    if (start < 0) throw new IllegalStateException("Missing config object: " + key);
    int depth = 0;
    boolean quoted = false;
    boolean escaped = false;
    for (int index = start; index < json.length(); index++) {
      char current = json.charAt(index);
      if (quoted) {
        if (escaped) escaped = false;
        else if (current == '\\') escaped = true;
        else if (current == '"') quoted = false;
      } else if (current == '"') quoted = true;
      else if (current == '{') depth++;
      else if (current == '}' && --depth == 0) return json.substring(start, index + 1);
    }
    throw new IllegalStateException("Unclosed config object: " + key);
  }
}
