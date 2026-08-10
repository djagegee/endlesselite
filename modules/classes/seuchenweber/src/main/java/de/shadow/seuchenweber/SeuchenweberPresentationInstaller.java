package de.shadow.seuchenweber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** Installs MMOSkillTree owner messages without replacing translations from other mods. */
final class SeuchenweberPresentationInstaller {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private SeuchenweberPresentationInstaller() { }

  static int install(Path mmoConfigDirectory) throws IOException {
    Files.createDirectories(mmoConfigDirectory);
    installLocale(mmoConfigDirectory.resolve("messages-de-DE.json"), german());
    installLocale(mmoConfigDirectory.resolve("messages-en-US.json"), english());
    return german().size();
  }

  private static void installLocale(Path file, Map<String, String> additions) throws IOException {
    JsonObject root = Files.exists(file)
        ? JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject()
        : new JsonObject();
    additions.forEach(root::addProperty);
    Path backup = file.resolveSibling(file.getFileName() + ".seuchenweber-before.bak");
    if (Files.exists(file) && Files.notExists(backup)) Files.copy(file, backup);
    Path temporary = file.resolveSibling(file.getFileName() + ".seuchenweber.tmp");
    Files.writeString(temporary, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
    try {
      Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException unsupported) {
      Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static Map<String, String> german() {
    LinkedHashMap<String, String> messages = new LinkedHashMap<>();
    messages.put("skill.seuchenweber_mastery", "Seuchenweber");
    messages.put("skill.seuchenweber_mastery.desc",
        "Meisterschaft des Cosmic Ruin Spellbooks und der seuchengewebten Fähigkeiten.");
    shopDescriptions(messages,
        "Gewährt Seuchenweber-Meisterschaftserfahrung.",
        "Gewährt eine große Menge Seuchenweber-Meisterschaftserfahrung.",
        "Gewährt eine meisterliche Menge Seuchenweber-Meisterschaftserfahrung.");
    ability(messages, "seuchenweber_seal_of_decay", "Siegel der Fäulnis",
        "Prägt einem feindlichen Ziel Nekrotoxin ein und eröffnet den Giftzyklus.");
    ability(messages, "seuchenweber_passive_necrotoxic_mastery", "Pesthauch",
        "Alle 2 Sekunden erhalten feindliche Ziele im Umkreis von 8 Blöcken einen Nekrotoxin-Stapel.");
    ability(messages, "seuchenweber_astral_rift", "Astralriss",
        "Teleportiert durch einen Riss und infiziert nahe feindliche Kreaturen.");
    ability(messages, "seuchenweber_passive_astral_echo", "Astrales Echo",
        "Überträgt begrenzt ein Nekrotoxin-Zeichen auf einen nahen Feind.");
    ability(messages, "seuchenweber_chronoblight", "Chronofäule",
        "Vertieft eigene Nekrotoxin-Zeichen und kontrolliert voll markierte Ziele kurz.");
    ability(messages, "seuchenweber_passive_soul_diagnosis", "Seelendiagnose",
        "Zeigt stark markierte Ziele ausschließlich ihrem Besitzer an.");
    ability(messages, "seuchenweber_passive_relic_attunement", "Reliktresonanz",
        "Natürlich ablaufende volle Zeichen erstatten Mana und Abklingzeit.");
    return messages;
  }

  private static Map<String, String> english() {
    LinkedHashMap<String, String> messages = new LinkedHashMap<>();
    messages.put("skill.seuchenweber_mastery", "Seuchenweber");
    messages.put("skill.seuchenweber_mastery.desc",
        "Mastery of the Cosmic Ruin Spellbook and plague-woven abilities.");
    shopDescriptions(messages,
        "Grants Seuchenweber mastery experience.",
        "Grants a large amount of Seuchenweber mastery experience.",
        "Grants a masterful amount of Seuchenweber mastery experience.");
    ability(messages, "seuchenweber_seal_of_decay", "Seal of Decay",
        "Imprints Nekrotoxin on a hostile target and begins the poison cycle.");
    ability(messages, "seuchenweber_passive_necrotoxic_mastery", "Blight Breath",
        "Every 2 seconds, hostile targets within 8 blocks gain one Nekrotoxin stack.");
    ability(messages, "seuchenweber_astral_rift", "Astral Rift",
        "Teleports through a rift and infects nearby hostile creatures.");
    ability(messages, "seuchenweber_passive_astral_echo", "Astral Echo",
        "Transfers a bounded Nekrotoxin mark to one nearby enemy.");
    ability(messages, "seuchenweber_chronoblight", "Chronoblight",
        "Deepens owned Nekrotoxin marks and briefly controls fully marked targets.");
    ability(messages, "seuchenweber_passive_soul_diagnosis", "Soul Diagnosis",
        "Shows heavily marked targets only to their owner.");
    ability(messages, "seuchenweber_passive_relic_attunement", "Relic Attunement",
        "Naturally expiring full marks refund mana and cooldown time.");
    return messages;
  }

  private static void ability(Map<String, String> messages, String id, String name, String flavor) {
    messages.put("ability." + id + ".name", name);
    messages.put("ability." + id + ".flavor", flavor);
  }

  private static void shopDescriptions(Map<String, String> messages,
      String normal, String greater, String master) {
    messages.put("shop.shop_xp_seuchenweber_mastery.desc", normal);
    messages.put("shop.shop_xp_greater_seuchenweber_mastery.desc", greater);
    messages.put("shop.shop_xp_master_seuchenweber_mastery.desc", master);
  }
}
