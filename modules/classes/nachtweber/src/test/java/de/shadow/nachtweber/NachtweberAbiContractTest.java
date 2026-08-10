package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class NachtweberAbiContractTest {
  @Test void buildManifestAndRuntimeRequireTheOwnedEffectsAbi() throws Exception {
    String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
    String manifest = Files.readString(Path.of("src", "main", "resources", "manifest.json"),
        StandardCharsets.UTF_8);
    String plugin = Files.readString(Path.of("src", "main", "java", "de", "shadow",
        "nachtweber", "NachtweberPlugin.java"), StandardCharsets.UTF_8);

    assertTrue(pom.contains("<version>1.5.2-owned-local</version>"));
    assertTrue(manifest.contains("Requires the hash-verified MMOSkillTree owned-effects ABI patch"));
    String preflight = "MmoOwnedEffectAbi.requireAvailable(NachtweberPlugin.class.getClassLoader());";
    assertTrue(plugin.contains(preflight));
    assertTrue(plugin.indexOf(preflight)
        < plugin.indexOf("candidateLifecycle.start();"));
    assertTrue(plugin.contains("new OwnedRegistryLifecycle<>("));
    assertTrue(plugin.contains("BestEffortCleanup.run("));
    assertTrue(plugin.contains("rollbackSetup(error, candidateEffects, candidateWiring, candidateLifecycle);"));
    assertTrue(plugin.contains("throw propagateSetupFailure(error);"));
    assertTrue(plugin.contains("effectLifecycle = null; if (value != null) value.shutdown();"));
  }
}
