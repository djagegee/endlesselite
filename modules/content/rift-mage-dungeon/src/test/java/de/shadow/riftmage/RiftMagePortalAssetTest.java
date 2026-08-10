package de.shadow.riftmage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

final class RiftMagePortalAssetTest {
    private static final Path PORTAL = Path.of("src", "main", "dlc", "server", "Server", "Item", "Items", "Portal", "EndlessElite", "EndlessElite_RiftMage_Portal.json");
    private static final Path LANGUAGES = Path.of("src", "main", "dlc", "server", "Server", "Languages");


    @Test
    void portalKeyUsesCanonicalNativeContractWithoutCustomInteractionCode() throws Exception {
        assertTrue(Files.isRegularFile(PORTAL), "Rift Mage portal key must exist");
        JsonObject root = JsonParser.parseReader(Files.newBufferedReader(PORTAL, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject translation = root.getAsJsonObject("TranslationProperties");
        JsonObject block = root.getAsJsonObject("BlockType");
        JsonObject key = root.getAsJsonObject("PortalKey");

        assertFalse(root.has("Parent"), "must not inherit Portal_Device configuration UI");
        assertEquals("endlesselite.items.EndlessElite_RiftMage_Portal.name", translation.get("Name").getAsString());
        assertEquals("endlesselite.items.EndlessElite_RiftMage_Portal.description", translation.get("Description").getAsString());
        assertEquals("EndlessElite_RiftMage_Portal", key.get("PortalType").getAsString());
        assertEquals(1200, key.get("TimeLimitSeconds").getAsInt());
        assertEquals("Icons/ItemsGenerated/PortalKey_Howling_Sands.png", root.get("Icon").getAsString());
        assertEquals("Blocks/Miscellaneous/Portal_Shard.blockymodel", block.get("CustomModel").getAsString());
        assertEquals("Blocks/Miscellaneous/Portal_Shard_Texture.png",
                block.getAsJsonArray("CustomModelTexture").get(0).getAsJsonObject().get("Texture").getAsString());
        assertFalse(block.has("BlockEntity"));
        assertFalse(block.has("Interactions"), "native PortalKey/PortalTypes own activation");
        assertFalse(root.has("Recipe"), "technical alpha key must not be craftable");
        assertEquals("Technical", root.get("Quality").getAsString());
        assertEquals(1, root.get("MaxStack").getAsInt());
        assertEquals(List.of("Portal", "Temporary"), java.util.stream.StreamSupport.stream(
                root.getAsJsonObject("Tags").getAsJsonArray("Type").spliterator(), false)
                .map(value -> value.getAsString()).toList());

        NativeAssetEvidence.assertEntries(List.of(
                "Common/Icons/ItemsGenerated/PortalKey_Howling_Sands.png",
                "Common/Blocks/Miscellaneous/Portal_Shard.blockymodel",
                "Common/Blocks/Miscellaneous/Portal_Shard_Texture.png"));
    }

    @Test
    void localizationDescribesAlphaRouteAndGateRemainsClosedForRuntimeAcceptance() throws Exception {
        Path english = LANGUAGES.resolve("en-US/endlesselite.lang");
        Path german = LANGUAGES.resolve("de-DE/endlesselite.lang");
        assertEquals("Rift Mage Portal", RiftMageBossAssetTest.translation(english, "items.EndlessElite_RiftMage_Portal.name"));
        assertTrue(RiftMageBossAssetTest.translation(english, "items.EndlessElite_RiftMage_Portal.description").contains("Runtime validation"));
        assertEquals("Rissmagierportal", RiftMageBossAssetTest.translation(german, "items.EndlessElite_RiftMage_Portal.name"));
        assertTrue(RiftMageBossAssetTest.translation(german, "items.EndlessElite_RiftMage_Portal.description").contains("Laufzeitprüfung"));

        JsonObject gate = JsonParser.parseReader(Files.newBufferedReader(
                Path.of("src", "main", "dlc", "release-gate.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        Set<String> blockers = java.util.stream.StreamSupport.stream(gate.getAsJsonArray("blockers").spliterator(), false)
                .map(value -> value.getAsString()).collect(java.util.stream.Collectors.toSet());
        assertFalse(gate.get("release_ready").getAsBoolean());
        assertFalse(gate.get("deployment_allowed").getAsBoolean());
        assertEquals(Set.of("manual/technodistrict-visual-gameplay-acceptance"), blockers);
    }
}
