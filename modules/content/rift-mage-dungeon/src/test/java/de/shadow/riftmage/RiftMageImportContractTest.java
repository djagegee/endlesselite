package de.shadow.riftmage;

import com.airijko.endlessleveling.imports.ImportManifest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class RiftMageImportContractTest {
    private static final Path MANIFEST = Path.of("src", "main", "dlc", "import.json");

    @Test
    void importManifestParsesAgainstInstalledEndlessLevelingCore() throws Exception {
        assertTrue(Files.isRegularFile(MANIFEST), "Rift Mage DLC import.json must exist");

        try (Reader reader = Files.newBufferedReader(MANIFEST, StandardCharsets.UTF_8)) {
            ImportManifest manifest = ImportManifest.parse(reader);
            assertEquals(1, manifest.schemaVersion());
            assertEquals("endless-elite-rift-mage", manifest.importId());
            assertEquals("Rift Mage: Shattered Technodistrict", manifest.displayName());
            assertEquals("instance-endless_elite_rift_mage-*", manifest.worldPattern());
            assertFalse(manifest.installedExternally());
            assertTrue(manifest.renderWithGenericTile());
            assertEquals("1-4", manifest.recommendedPlayers());
        }
    }

    @Test
    void dungeonTabRegistersInstantTeleportWhileRewardsAndMatchmakingStayClosed() throws Exception {
        JsonObject root = JsonParser.parseReader(Files.newBufferedReader(MANIFEST, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject dungeonEntries = root.getAsJsonObject("dungeons_entry");
        assertEquals(Set.of("instance-endless_elite_rift_mage-*"), dungeonEntries.keySet(),
                "EL dungeons_entry must be keyed by world_pattern like the known-good import");
        JsonObject dungeon = dungeonEntries.getAsJsonObject("instance-endless_elite_rift_mage-*");
        JsonObject banking = dungeon.getAsJsonObject("xp_banking");
        JsonObject matchmaking = dungeon.getAsJsonObject("matchmaking");
        ImportManifest manifest;
        try (Reader reader = Files.newBufferedReader(MANIFEST, StandardCharsets.UTF_8)) {
            manifest = ImportManifest.parse(reader);
        }

        assertEquals("DUNGEON", root.get("import_type").getAsString());
        assertTrue(manifest.showInImportMenu(), "Rift Mage must appear in the EndlessLeveling dungeon tab");
        assertTrue(manifest.renderWithGenericTile(), "Rift Mage must use EL's generic imported dungeon card");
        assertEquals("endless-elite-rift-mage", manifest.importId());
        assertEquals("EndlessElite_RiftMage", manifest.instanceDefinition().routingTemplate());
        assertEquals("EndlessElite_RiftMage_Portal", manifest.instanceDefinition().portalBlockId());
        assertTrue(dungeon.get("allow_teleport").getAsBoolean(), "EL ENTER must start the instance immediately");
        assertFalse(banking.get("enabled").getAsBoolean());
        assertFalse(banking.get("cooldown_enabled").getAsBoolean());
        assertEquals(0, banking.getAsJsonArray("reward_commands").size());
        assertFalse(matchmaking.get("enabled").getAsBoolean());
        assertEquals(1200, dungeon.get("instance_duration_seconds").getAsInt());
        assertEquals(120, banking.get("dungeon_close_seconds").getAsInt());
        assertEquals("EndlessElite_RiftMage", dungeon.getAsJsonArray("boss_mob_ids").get(0).getAsString());
    }

    @Test
    void waveConfigResolvesInsideInstalledAssetPackAtRuntime() throws Exception {
        ImportManifest manifest;
        try (Reader reader = Files.newBufferedReader(MANIFEST, StandardCharsets.UTF_8)) {
            manifest = ImportManifest.parse(reader);
        }

        assertEquals("0.1.1", manifest.version(),
                "EL must see the wave runtime fix as an update over installed 0.1.0");
        assertEquals("waves/rift_mage_technodistrict_waves.json", manifest.waveConfigPath());
        assertTrue(
                Files.isRegularFile(Path.of("src", "main", "dlc").resolve(manifest.waveConfigPath())),
                "EL resolves external wave_config beneath mods/<packFolderName>, so the file must remain in the DLC pack root"
        );
        assertFalse(
                Files.exists(Path.of("src", "main", "dlc", "plugin").resolve(manifest.waveConfigPath())),
                "plugin/waves is moved into Airijko_EndlessLevelingCore and is invisible to ImportRegistry.findWavesPathForWorld"
        );
    }

    @Test
    void releaseGateBlocksImportReloadAndManualAcceptance() throws Exception {
        Path gatePath = Path.of("src", "main", "dlc", "release-gate.json");
        JsonObject gate = JsonParser.parseReader(Files.newBufferedReader(gatePath, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonArray blockers = gate.getAsJsonArray("blockers");
        Set<String> actual = java.util.stream.StreamSupport.stream(blockers.spliterator(), false)
                .map(value -> value.getAsString())
                .collect(java.util.stream.Collectors.toSet());

        assertFalse(gate.get("release_ready").getAsBoolean());
        assertFalse(gate.get("deployment_allowed").getAsBoolean());
        assertEquals(Set.of("manual/technodistrict-visual-gameplay-acceptance"), actual);
        assertEquals(
                "Validate waves, Rift Mage combat, death/fallback and visual quality before enabling deployment or rewards.",
                gate.get("next_gate").getAsString()
        );
    }
}
