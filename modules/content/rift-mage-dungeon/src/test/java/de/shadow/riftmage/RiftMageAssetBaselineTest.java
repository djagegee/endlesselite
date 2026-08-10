package de.shadow.riftmage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RiftMageAssetBaselineTest {
    @Test
    void nativeBaselinesRemainHashBoundReferenceOnlyEvidence() throws Exception {
        Path contractPath = Path.of("src", "main", "contracts", "asset-baseline.json");
        JsonObject contract = JsonParser.parseReader(
                Files.newBufferedReader(contractPath, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("$HYTALE_ASSETS_ZIP", contract.get("source_archive").getAsString());
        assertEquals("copy_or_extend_never_override_native_assets", contract.get("ownership_rule").getAsString());

        JsonObject world = contract.getAsJsonObject("world");
        JsonObject boss = contract.getAsJsonObject("boss");
        JsonObject portal = contract.getAsJsonObject("portal");
        assertTrue(world.get("prototype_prefab_count").getAsInt() >= 50);
        assertEquals("Template_Trork_Mage", boss.get("behavior_template").getAsString());
        assertEquals("Skeleton_Archmage_Staff_Corruption_Orb",
                boss.get("behavior_ranged_attack").getAsString());
        assertTrue(portal.get("native_has_portal_component").getAsBoolean());
        assertEquals("Portal", portal.get("native_active_collision_enter_type").getAsString());

        NativeAssetEvidence.assertEntries(List.of(
                world.get("boss_room_reference").getAsString(),
                boss.get("behavior_reference").getAsString(),
                boss.get("visual_model_reference").getAsString(),
                boss.get("spellbook_animation_reference").getAsString(),
                portal.get("item_reference").getAsString()));

        assertEquals("EndlessElite_RiftMage", boss.get("custom_mob_id").getAsString());
        assertEquals("EndlessElite_RiftMage_Portal", portal.get("custom_item_id").getAsString());
        assertFalse(boss.get("custom_role_target").getAsString().equals(boss.get("behavior_reference").getAsString()));
        assertFalse(portal.get("custom_item_target").getAsString().equals(portal.get("item_reference").getAsString()));
    }
}
