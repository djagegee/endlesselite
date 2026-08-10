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
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

final class RiftMageTechnodistrictContractTest {
    private static final Path DLC = Path.of("src", "main", "dlc");
    private static final Path SERVER = DLC.resolve("server/Server");
    private static final Path INSTANCE = SERVER.resolve("Instances/EndlessElite_RiftMage/instance.bson");
    private static final Path STRUCTURE = SERVER.resolve("HytaleGenerator/WorldStructures/EndlessElite_RiftMage_Technodistrict.json");
    private static final Path PORTAL_ITEM = SERVER.resolve("Item/Items/Portal/EndlessElite/EndlessElite_RiftMage_Portal.json");
    private static final Path PORTAL_TYPE = SERVER.resolve("PortalTypes/EndlessElite_RiftMage_Portal.json");
    private static final Path WAVES = DLC.resolve("waves/rift_mage_technodistrict_waves.json");
    private static final Path ASSETS = Path.of("C:/Users/agege/Desktop/LOKAL SERVER/Assets.zip");

    @Test
    void installedParserReceivesCompleteInstanceRoutingAndWaveContract() throws Exception {
        try (Reader reader = Files.newBufferedReader(DLC.resolve("import.json"), StandardCharsets.UTF_8)) {
            ImportManifest manifest = ImportManifest.parse(reader);
            assertNotNull(manifest.instanceDefinition());
            assertEquals("EndlessElite_RiftMage", manifest.instanceDefinition().routingTemplate());
            assertEquals("EndlessElite_RiftMage_Portal", manifest.instanceDefinition().portalBlockId());
            assertEquals("endless_elite_rift_mage", manifest.instanceDefinition().worldNameToken());
            assertEquals("EndlessElite_RiftMage", manifest.instanceDefinition().spawnSuffix());
            assertEquals("EndlessElite_RiftMage", manifest.instanceDefinition().legacyTemplate());
            assertEquals("waves/rift_mage_technodistrict_waves.json", manifest.waveConfigPath());
            assertNotNull(manifest.objectivePanel());
        }
    }

    @Test
    void customInstanceUsesCustomWorldStructureAndNativeScifiBiome() throws Exception {
        assertTrue(Files.isRegularFile(INSTANCE), "custom Hytale instance template missing");
        assertTrue(Files.isRegularFile(STRUCTURE), "custom Hytale world structure missing");

        JsonObject instance = json(INSTANCE);
        JsonObject worldGen = instance.getAsJsonObject("WorldGen");
        assertEquals("Adventure", instance.get("GameMode").getAsString());
        assertEquals("HytaleGenerator", worldGen.get("Type").getAsString());
        assertEquals("EndlessElite_RiftMage_Technodistrict", worldGen.get("WorldStructure").getAsString());
        assertEquals("Default_Instance", instance.get("GameplayConfig").getAsString());
        assertTrue(instance.has("Seed"), "instance worldgen seed must be explicit");
        assertEquals(2026080801L, instance.get("Seed").getAsLong(), "instance worldgen seed must be deterministic");
        assertFalse(instance.get("IsPvpEnabled").getAsBoolean());
        assertTrue(instance.get("DeleteOnRemove").getAsBoolean());
        byte[] uuid = Base64.getDecoder().decode(instance.getAsJsonObject("UUID").get("$binary").getAsString());
        assertEquals(16, uuid.length);
        assertNotEquals("AZKxiVAMQfWIS0qBsBfjzQ==", instance.getAsJsonObject("UUID").get("$binary").getAsString());

        JsonObject structure = json(STRUCTURE);
        assertEquals("NoiseRange", structure.get("Type").getAsString());
        assertEquals("ScifiBlockLandscape", structure.get("DefaultBiome").getAsString());
        assertEquals(0, structure.getAsJsonArray("Biomes").size());

        try (ZipFile assets = new ZipFile(ASSETS.toFile())) {
            assertNotNull(assets.getEntry("Server/HytaleGenerator/Biomes/Experimental/ScifiBlockLandscape.json"));
        }
    }

    @Test
    void generatedInstanceShipsCompleteMutableResourceBaseline() {
        Path resources = INSTANCE.getParent().resolve("resources");
        for (String file : List.of(
                "BlockCounter.json", "BlockMapMarkers.json", "ChunkStorage.json", "InstanceData.json",
                "PrefabEditSession.json", "ReputationData.json", "SharedUserMapMarkers.json",
                "SpawnSuppressionController.json", "Time.json")) {
            assertTrue(Files.isRegularFile(resources.resolve(file)), file);
        }
    }

    @Test
    void deterministicSeedUsesMeasuredSurfaceForSpawnPortalFallbackAndArena() throws Exception {
        JsonObject instance = json(INSTANCE);
        JsonObject instanceSpawn = instance.getAsJsonObject("SpawnProvider").getAsJsonObject("SpawnPoint");
        JsonObject portalSpawn = json(PORTAL_TYPE).getAsJsonObject("Spawn")
                .getAsJsonObject("SpawnProviderOverride").getAsJsonObject("SpawnPoint");
        JsonObject floor = json(DLC.resolve("import.json")).getAsJsonObject("dungeons_entry")
                .getAsJsonObject("instance-endless_elite_rift_mage-*").getAsJsonObject("floor");
        JsonArray fallback = floor.getAsJsonArray("teleport_to");
        JsonArray arena = floor.getAsJsonArray("arena_center");

        assertEquals(2026080801L, instance.get("Seed").getAsLong());
        assertEquals(89.0, instanceSpawn.get("Y").getAsDouble());
        assertEquals(89.0, portalSpawn.get("Y").getAsDouble());
        assertEquals(89.0, fallback.get(1).getAsDouble());
        assertEquals(89.0, arena.get(1).getAsDouble());
    }

    @Test
    void portalKeyAndPortalTypeRouteOnlyToCustomInstance() throws Exception {
        assertTrue(Files.isRegularFile(PORTAL_TYPE), "custom PortalType missing");
        JsonObject item = json(PORTAL_ITEM);
        JsonObject key = item.getAsJsonObject("PortalKey");
        assertNotNull(key, "portal item is still sealed");
        assertEquals("EndlessElite_RiftMage_Portal", key.get("PortalType").getAsString());
        assertEquals(1200, key.get("TimeLimitSeconds").getAsInt());
        assertFalse(item.has("Recipe"));
        assertEquals("Technical", item.get("Quality").getAsString());

        JsonObject type = json(PORTAL_TYPE);
        assertEquals("EndlessElite_RiftMage", type.get("InstanceId").getAsString());
        JsonObject spawn = type.getAsJsonObject("Spawn").getAsJsonObject("SpawnProviderOverride").getAsJsonObject("SpawnPoint");
        assertEquals(0.5, spawn.get("X").getAsDouble());
        assertEquals(0.5, spawn.get("Z").getAsDouble());
        JsonObject instanceSpawn = json(INSTANCE).getAsJsonObject("SpawnProvider").getAsJsonObject("SpawnPoint");
        assertEquals(instanceSpawn.get("X").getAsDouble(), spawn.get("X").getAsDouble());
        assertEquals(instanceSpawn.get("Y").getAsDouble(), spawn.get("Y").getAsDouble());
        assertEquals(instanceSpawn.get("Z").getAsDouble(), spawn.get("Z").getAsDouble());
        assertEquals("Rift Mage Portal", RiftMageBossAssetTest.translation(
                SERVER.resolve("Languages/en-US/endlesselite.lang"), "items.EndlessElite_RiftMage_Portal.name"));
        assertEquals("Rissmagierportal", RiftMageBossAssetTest.translation(
                SERVER.resolve("Languages/de-DE/endlesselite.lang"), "items.EndlessElite_RiftMage_Portal.name"));
        assertEquals("Shattered Technodistrict", RiftMageBossAssetTest.translation(
                SERVER.resolve("Languages/en-US/endlesselite.lang"), "portals.RiftMage.name"));
        assertEquals("Zerstörter Technologiedistrikt", RiftMageBossAssetTest.translation(
                SERVER.resolve("Languages/de-DE/endlesselite.lang"), "portals.RiftMage.name"));
    }

    @Test
    void threeWaveEncounterEndsWithSingleRiftMageAuthority() throws Exception {
        assertTrue(Files.isRegularFile(WAVES), "wave contract missing");
        JsonObject root = json(WAVES);
        assertFalse(root.get("pull_to_center").getAsBoolean());
        assertEquals(3, root.getAsJsonArray("waves").size());
        JsonObject finale = root.getAsJsonArray("waves").get(2).getAsJsonObject();
        assertEquals(3, finale.get("wave").getAsInt());
        JsonArray bosses = finale.getAsJsonArray("bosses");
        assertEquals(1, bosses.size());
        assertEquals("EndlessElite_RiftMage", bosses.get(0).getAsJsonObject().get("id").getAsString());
        assertFalse(root.has("health_multiplier"), "EndlessEliteMobs/EndlessLeveling remain scaling authorities");
        assertFalse(root.has("damage_multiplier"), "EndlessEliteMobs/EndlessLeveling remain scaling authorities");

        try (ZipFile assets = new ZipFile(ASSETS.toFile())) {
            for (String role : List.of(
                    "Server/NPC/Roles/Void/Eye_Void.json",
                    "Server/NPC/Roles/Void/Spectre_Void.json",
                    "Server/NPC/Roles/Intelligent/Aggressive/Outlander/Outlander_Sorcerer.json")) {
                assertNotNull(assets.getEntry(role), role);
            }
        }
    }

    @Test
    void releaseGateKeepsManualAcceptanceClosedAfterGeneratedSpawnPass() throws Exception {
        JsonObject gate = json(DLC.resolve("release-gate.json"));
        Set<String> blockers = java.util.stream.StreamSupport.stream(gate.getAsJsonArray("blockers").spliterator(), false)
                .map(value -> value.getAsString()).collect(java.util.stream.Collectors.toSet());
        assertFalse(gate.get("release_ready").getAsBoolean());
        assertFalse(gate.get("deployment_allowed").getAsBoolean());
        assertEquals(Set.of("manual/technodistrict-visual-gameplay-acceptance"), blockers);
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseReader(Files.newBufferedReader(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
