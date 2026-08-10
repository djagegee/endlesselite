package de.shadow.riftmage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NativeAssetEvidence {
    private static final Path CONTRACT = Path.of("src", "main", "contracts", "asset-baseline.json");

    private NativeAssetEvidence() { }

    static void assertEntries(List<String> entries) throws Exception {
        JsonObject root = JsonParser.parseReader(
                Files.newBufferedReader(CONTRACT, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject hashes = root.getAsJsonObject("required_native_entries");
        assertTrue(hashes != null, "required_native_entries missing");
        for (String entry : entries) {
            assertTrue(hashes.has(entry), "missing committed native-asset evidence: " + entry);
            assertTrue(hashes.get(entry).getAsString().matches("[0-9a-f]{64}"),
                    "invalid native-asset SHA-256: " + entry);
        }

        String optionalArchive = System.getProperty("hytale.assets.zip");
        if (optionalArchive == null || optionalArchive.isBlank()) {
            optionalArchive = System.getenv("HYTALE_ASSETS_ZIP");
        }
        if (optionalArchive == null || optionalArchive.isBlank()) return;

        try (ZipFile assets = new ZipFile(Path.of(optionalArchive).toFile())) {
            for (String entry : entries) {
                var zipEntry = assets.getEntry(entry);
                assertTrue(zipEntry != null, "native asset missing from live archive: " + entry);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (InputStream input = assets.getInputStream(zipEntry)) {
                    byte[] buffer = new byte[64 * 1024];
                    for (int read; (read = input.read(buffer)) >= 0; ) {
                        if (read > 0) digest.update(buffer, 0, read);
                    }
                }
                assertEquals(hashes.get(entry).getAsString(), HexFormat.of().formatHex(digest.digest()),
                        "native asset changed: " + entry);
            }
        }
    }
}
