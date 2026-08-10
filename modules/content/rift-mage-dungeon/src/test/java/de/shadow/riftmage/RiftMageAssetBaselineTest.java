package de.shadow.riftmage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

final class RiftMageAssetBaselineTest {
    @Test
    void nativeBaselinesExistAndRemainReferenceOnly() throws Exception {
        Path contractPath = Path.of("src", "main", "contracts", "asset-baseline.json");
        JsonObject contract = JsonParser.parseReader(Files.newBufferedReader(contractPath, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("copy_or_extend_never_override_native_assets", contract.get("ownership_rule").getAsString());

        JsonObject world = contract.getAsJsonObject("world");
        JsonObject boss = contract.getAsJsonObject("boss");
        JsonObject portal = contract.getAsJsonObject("portal");
        Path archive = Path.of(contract.get("source_archive").getAsString());

        try (ZipFile assets = new ZipFile(archive.toFile())) {
            assertNotNull(assets.getEntry(world.get("boss_room_reference").getAsString()));
            assertTrue(assets.stream().filter(e -> e.getName().startsWith(world.get("prototype_prefab_family").getAsString())).count() >= 50);
            assertNotNull(assets.getEntry(boss.get("visual_model_reference").getAsString()));
            assertNotNull(assets.getEntry(boss.get("spellbook_animation_reference").getAsString()));

            JsonObject behavior = readJson(assets, boss.get("behavior_reference").getAsString());
            assertEquals("Template_Trork_Mage", behavior.get("Reference").getAsString());
            assertEquals("Skeleton_Archmage_Staff_Corruption_Orb",
                    behavior.getAsJsonObject("Modify").get("RangedAttack").getAsString());

            JsonObject portalItem = readJson(assets, portal.get("item_reference").getAsString());
            JsonObject blockType = portalItem.getAsJsonObject("BlockType");
            assertTrue(blockType.getAsJsonObject("BlockEntity").getAsJsonObject("Components").has("Portal"));
            assertEquals("Portal", blockType.getAsJsonObject("State")
                    .getAsJsonObject("Definitions").getAsJsonObject("Active")
                    .getAsJsonObject("Interactions").getAsJsonObject("CollisionEnter")
                    .getAsJsonArray("Interactions").get(0).getAsJsonObject().get("Type").getAsString());
        }

        assertEquals("EndlessElite_RiftMage", boss.get("custom_mob_id").getAsString());
        assertEquals("EndlessElite_RiftMage_Portal", portal.get("custom_item_id").getAsString());
        assertFalse(boss.get("custom_role_target").getAsString().equals(boss.get("behavior_reference").getAsString()));
        assertFalse(portal.get("custom_item_target").getAsString().equals(portal.get("item_reference").getAsString()));
    }

    private static JsonObject readJson(ZipFile zip, String path) throws Exception {
        try (Reader reader = new InputStreamReader(zip.getInputStream(zip.getEntry(path)), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
