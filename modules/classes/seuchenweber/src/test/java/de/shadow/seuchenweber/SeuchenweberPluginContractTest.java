package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SeuchenweberPluginContractTest {
  @Test void manifestEntryPointExistsAndIsAHytaleJavaPlugin() throws Exception {
    try (InputStream input = getClass().getClassLoader().getResourceAsStream("manifest.json")) {
      String manifest = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(manifest.contains("\"Main\": \"de.shadow.seuchenweber.SeuchenweberPlugin\""));
      assertTrue(manifest.contains("\"DisabledByDefault\": false"));
    }
    assertEquals(JavaPlugin.class, SeuchenweberPlugin.class.getSuperclass());
  }

  @Test void pluginUsesFailClosedOwnedEffectLifecycleAndRethrowsSetupFailures() throws Exception {
    String plugin = Files.readString(Path.of("src", "main", "java", "de", "shadow",
        "seuchenweber", "SeuchenweberPlugin.java"), StandardCharsets.UTF_8);
    String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
    String manifest = Files.readString(Path.of("src", "main", "resources", "manifest.json"),
        StandardCharsets.UTF_8);

    assertTrue(pom.contains("<version>1.5.2-owned-local</version>"));
    assertTrue(plugin.contains("MmoOwnedEffectAbi.requireAvailable(SeuchenweberPlugin.class.getClassLoader());"));
    assertTrue(plugin.contains("new OwnedRegistryLifecycle<>(new MmoEffectRegistry(), effects)"));
    assertTrue(plugin.contains("service.registerIfAbsent(id, effect)"));
    assertTrue(plugin.contains("service.unregister(id, expectedEffect)"));
    assertTrue(plugin.contains("rollbackSetup(error);"));
    assertTrue(plugin.contains("throw propagateSetupFailure(error);"));
    assertTrue(plugin.contains("BestEffortCleanup.run("));
    assertTrue(plugin.contains("effectLifecycle = null; if (value != null) value.shutdown();"));
    assertTrue(plugin.contains("EndlessLevelingAPI.get().unregisterClass(value.getId())"));
    assertTrue(manifest.contains("Requires the hash-verified MMOSkillTree owned-effects ABI patch"));
  }
}
