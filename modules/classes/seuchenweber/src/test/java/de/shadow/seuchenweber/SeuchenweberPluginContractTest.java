package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
}
