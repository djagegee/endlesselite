package de.shadow.riftmage;

import com.airijko.endlessleveling.imports.ImportManifest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

final class RiftMageContractArtifactIT {
    @Test
    void contractZipHasOnlyValidatedRootFilesAndRemainsNonDeployable() throws Exception {
        Path zipPath = Path.of("target", "rift-mage-dungeon-0.1.1-SNAPSHOT-contract.zip");
        assertTrue(Files.isRegularFile(zipPath));

        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            Set<String> entries = zip.stream()
                    .filter(entry -> !entry.isDirectory())
                    .map(ZipEntry::getName)
                    .collect(java.util.stream.Collectors.toSet());
            assertEquals(Set.of(
                    "import.json",
                    "release-gate.json",
                    "waves/rift_mage_technodistrict_waves.json",
                    "assets/UI/Custom/Pages/Dungeons/Cards/Imports/Gear/endless-elite-rift-mage.ui",
                    "server/Server/NPC/Roles/EndlessElite/EndlessElite_RiftMage.json",
                    "server/Server/Item/Items/Portal/EndlessElite/EndlessElite_RiftMage_Portal.json",
                    "server/Server/PortalTypes/EndlessElite_RiftMage_Portal.json",
                    "server/Server/HytaleGenerator/WorldStructures/EndlessElite_RiftMage_Technodistrict.json",
                    "server/Server/Instances/EndlessElite_RiftMage/instance.bson",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/BlockCounter.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/BlockMapMarkers.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/ChunkStorage.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/InstanceData.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/PrefabEditSession.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/ReputationData.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/SharedUserMapMarkers.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/SpawnSuppressionController.json",
                    "server/Server/Instances/EndlessElite_RiftMage/resources/Time.json",
                    "server/Server/Languages/en-US/endlesselite.lang",
                    "server/Server/Languages/de-DE/endlesselite.lang"
            ), entries);

            try (Reader reader = new InputStreamReader(zip.getInputStream(zip.getEntry("import.json")), StandardCharsets.UTF_8)) {
                assertEquals("endless-elite-rift-mage", ImportManifest.parse(reader).importId());
            }
            try (Reader reader = new InputStreamReader(zip.getInputStream(zip.getEntry("release-gate.json")), StandardCharsets.UTF_8)) {
                JsonObject gate = JsonParser.parseReader(reader).getAsJsonObject();
                assertFalse(gate.get("release_ready").getAsBoolean());
                assertFalse(gate.get("deployment_allowed").getAsBoolean());
            }
        }
    }
}
