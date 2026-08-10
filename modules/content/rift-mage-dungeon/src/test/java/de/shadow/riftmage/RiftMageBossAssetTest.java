package de.shadow.riftmage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

final class RiftMageBossAssetTest {
    private static final Path ROLE = Path.of("src", "main", "dlc", "server", "Server", "NPC", "Roles", "EndlessElite", "EndlessElite_RiftMage.json");
    private static final Path ASSETS = Path.of("C:/Users/agege/Desktop/LOKAL SERVER/Assets.zip");

    @Test
    void roleUsesOnlyProvenNativeCombatAndVisualReferences() throws Exception {
        assertTrue(Files.isRegularFile(ROLE), "custom Rift Mage role must exist in the installer server/ branch");
        JsonObject root = JsonParser.parseReader(Files.newBufferedReader(ROLE, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject modify = root.getAsJsonObject("Modify");

        assertEquals("Variant", root.get("Type").getAsString());
        assertEquals("Template_Trork_Mage", root.get("Reference").getAsString(),
                "nested Outlander_Sorcerer variants expose only NameTranslationKey");
        assertEquals("Necromancer_Void", modify.get("Appearance").getAsString());
        assertEquals(List.of("Weapon_Spellbook_Demon", "Weapon_Spellbook_Demon"), strings(modify.getAsJsonArray("Weapons")));
        assertEquals("Empty", modify.get("DropList").getAsString());
        assertEquals(List.of("Void"), strings(modify.getAsJsonArray("TargetGroups")));
        assertEquals(List.of("Void"), strings(modify.getAsJsonArray("UnderlingGroups")));
        assertEquals(List.of("Self", "Void"), strings(modify.getAsJsonArray("DisableDamageGroups")));
        assertEquals("Spawn_Void", modify.get("SummonKind").getAsString());
        assertEquals("Void", modify.get("AttitudeGroup").getAsString());
        assertEquals("Skeleton_Archmage_Staff_Corruption_Orb", modify.get("RangedAttack").getAsString());
        assertEquals("Component_Instruction_Attack_Sequence_Outlander_Sorcerer_Ranged",
                modify.get("RangedAttackSequence").getAsString());
        assertFalse(modify.has("MaxHealth"), "EndlessEliteMobs/EndlessLeveling retain scaling ownership");

        JsonObject nameParameter = root.getAsJsonObject("Parameters").getAsJsonObject("NameTranslationKey");
        assertEquals("endlesselite.npcRoles.RiftMage.name", nameParameter.get("Value").getAsString());

        try (ZipFile assets = new ZipFile(ASSETS.toFile())) {
            for (String entry : List.of(
                    "Server/NPC/Roles/Intelligent/Aggressive/Trork/Templates/Template_Trork_Mage.json",
                    "Server/NPC/Roles/Intelligent/Aggressive/Outlander/Outlander_Sorcerer.json",
                    "Server/NPC/Roles/Intelligent/Aggressive/Outlander/Components/Component_Instruction_Attack_Sequence_Outlander_Sorcerer_Ranged.json",
                    "Server/Models/Void/Necromancer_Void.json",
                    "Server/Item/Items/Weapon/Spellbook/Weapon_Spellbook_Demon.json",
                    "Server/NPC/Roles/Void/Spawn_Void.json",
                    "Server/Item/Interactions/NPCs/Undead/Skeleton_Archmage/Skeleton_Archmage_Staff_Corruption_Orb.json"
            )) {
                assertNotNull(assets.getEntry(entry), entry);
            }
        }
    }

    @Test
    void roleNameHasEnglishAndGermanAssetPackTranslations() throws Exception {
        Path languages = Path.of("src", "main", "dlc", "server", "Server", "Languages");
        Path english = languages.resolve("en-US/endlesselite.lang");
        Path german = languages.resolve("de-DE/endlesselite.lang");
        assertTrue(Files.isRegularFile(english), "English Rift Mage translation must exist");
        assertTrue(Files.isRegularFile(german), "German Rift Mage translation must exist");
        assertEquals("Rift Mage", translation(english, "npcRoles.RiftMage.name"));
        assertEquals("Rissmagier", translation(german, "npcRoles.RiftMage.name"));
    }

    private static List<String> strings(JsonArray array) {
        return java.util.stream.StreamSupport.stream(array.spliterator(), false)
                .map(element -> element.getAsString()).toList();
    }

    static String translation(Path path, String key) throws Exception {
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .map(line -> line.split("\\s*=\\s*", 2))
                .filter(parts -> parts.length == 2 && parts[0].equals(key))
                .map(parts -> parts[1])
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing translation key " + key + " in " + path));
    }
}
