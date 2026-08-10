package de.shadow.endlessbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

public final class EndlessBookConfig {
    public static final int SCHEMA_VERSION = 1;
    public static final String FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private EndlessBookConfig() {}

    public static List<LinkDefinition> defaultLinks() {
        return List.of(
            new LinkDefinition("your-path", "profile", "YOUR PATH", "DEIN PFAD", true),
            new LinkDefinition("augments", "augments", "AUGMENTS", "AUGMENTE", true),
            new LinkDefinition("fates", "fates", "FATES", "SCHICKSALE", true),
            new LinkDefinition("mastery", "xp", "MASTERY", "MEISTERSCHAFT", true),
            new LinkDefinition("guild", "guild", "GUILD", "GILDE", true),
            new LinkDefinition("dungeons", "dungeons", "DUNGEONS", "DUNGEONS", true),
            new LinkDefinition("claims", "pclaims", "CLAIMS", "GRUNDSTÜCKE", true)
        );
    }

    public static Path bootstrap(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        Path target = dataDirectory.resolve(FILE_NAME);
        if (Files.exists(target)) return target;
        CoreConfig config = new CoreConfig(
            SCHEMA_VERSION,
            new BookSettings("EndlessBook_LevelMenu", "getting_started", -1, true, 12),
            defaultLinks(),
            new Localization("ENDLESS BOOK", "ENDLESS BOOK", "QUICK ACCESS", "SCHNELLZUGRIFF")
        );
        String json = GSON.toJson(config) + System.lineSeparator();
        Path temporary = Files.createTempFile(dataDirectory, "config-", ".tmp");
        Files.writeString(temporary, json, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target);
        }
        return target;
    }

    public static CoreConfig load(Path dataDirectory) throws IOException {
        Path file = bootstrap(dataDirectory);
        CoreConfig parsed = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), CoreConfig.class);
        if (parsed == null || parsed.book() == null || parsed.links() == null || parsed.localization() == null) {
            throw new IOException("Endless Book config is incomplete: " + file);
        }
        List<LinkDefinition> safeLinks = parsed.links().stream()
            .filter(link -> link != null && link.enabled())
            .filter(link -> link.id() != null && !link.id().isBlank())
            .filter(link -> isSafeCommand(link.command()))
            .limit(Math.max(1, Math.min(12, parsed.book().maximumLinks())))
            .toList();
        return new CoreConfig(parsed.schemaVersion(), parsed.book(), safeLinks, parsed.localization());
    }

    public static boolean isSafeCommand(String command) {
        if (command == null || command.isBlank()) return false;
        String value = command.startsWith("/") ? command.substring(1) : command;
        return !value.isBlank() && !value.contains("\n") && !value.contains("\r") && !value.contains(";");
    }

    public static String normalizeCommand(String command) {
        String value = command == null ? "" : command.trim();
        return value.startsWith("/") ? value.substring(1) : value;
    }

    public static String localized(LinkDefinition link, String language) {
        String normalized = language == null ? "" : language.toLowerCase(Locale.ROOT);
        return normalized.startsWith("de") ? link.german() : link.english();
    }

    public record CoreConfig(int schemaVersion, BookSettings book, List<LinkDefinition> links, Localization localization) {}
    public record BookSettings(String itemId, String requiredQuest, int reservedHotbarSlot, boolean lockSlot, int maximumLinks) {}
    public record Localization(String titleEnglish, String titleGerman, String linksEnglish, String linksGerman) {}
}
