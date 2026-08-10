package de.shadow.portalspawn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

class PortalSpawnContractArtifactIT {
  @Test
  void contractZipContainsOnlyTheAuditedSnapshotFiles() throws Exception {
    Path artifact = Path.of("target", "portal-spawn-0.0.0-SNAPSHOT-contract.zip");
    assertTrue(artifact.toFile().isFile());
    try (ZipFile zip = new ZipFile(artifact.toFile())) {
      Set<String> entries = new TreeSet<>();
      zip.stream().filter(entry -> !entry.isDirectory()).forEach(entry -> entries.add(entry.getName()));
      assertEquals(Set.of(
          "SOURCE_SNAPSHOT.json",
          "manifest.json",
          "release-gate.json",
          "Server/Prefabs/Portal Spawn.prefab.json",
          "Server/Prefabs/spawn.prefab.json"), entries);
    }
  }
}
