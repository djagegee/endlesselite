package de.shadow.mjolnirsafety;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

class MjolnirSafetyPatchContractArtifactIT {
  @Test
  void contractZipContainsOnlyTheAuditedPatchSnapshot() throws Exception {
    Path artifact = Path.of("target", "mjolnir-safety-patch-1.0.0-SNAPSHOT-contract.zip");
    assertTrue(artifact.toFile().isFile());
    try (ZipFile zip = new ZipFile(artifact.toFile())) {
      Set<String> entries = new TreeSet<>();
      zip.stream().filter(entry -> !entry.isDirectory()).forEach(entry -> entries.add(entry.getName()));
      assertEquals(Set.of("README.md", "SOURCE_SNAPSHOT.json", "manifest.json", "release-gate.json",
          "Server/Item/Items/Weapon_Mjolnir_Starky.json.patch"), entries);
    }
  }
}
