package de.shadow.nachtweber;

import static org.junit.jupiter.api.Assertions.*;

import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class NachtweberRuntimeAdapterTest {
  @Test void classDefinitionUsesStableOriginalIdentityAndFailClosedPermission() {
    CharacterClassDefinition definition = NachtweberClassDefinition.create();
    assertEquals("elite_nightweaver", definition.getId());
    assertEquals("Nachtweber", definition.getDisplayName());
    assertEquals("A dark hunter who binds prey with black thread and wears it down with venom.",
        definition.getDescription());
    assertEquals(List.of("Assassin"), definition.getRoles());
    assertEquals("Physical", definition.getDamageType());
    assertEquals("melee", definition.getRangeType());
    assertEquals("combat", definition.getCategory());
    assertEquals("nachtweber.use", definition.getRequiredPermission());
    assertTrue(definition.isEnabled());
    assertTrue(definition.getWeaponMultipliers().isEmpty(), "No unverified weapon IDs in the adapter slice");
  }

  @Test void lifecycleRegistersPermissionThenClassAndUnregistersExactlyOnce() {
    RecordingPort port = new RecordingPort(true);
    NachtweberLifecycle lifecycle = new NachtweberLifecycle(port, NachtweberClassDefinition.create());
    lifecycle.start();
    lifecycle.start();
    assertEquals(List.of("permission:nachtweber.use", "register:elite_nightweaver"), port.calls);
    lifecycle.shutdown();
    lifecycle.shutdown();
    assertEquals(List.of("permission:nachtweber.use", "register:elite_nightweaver", "unregister:elite_nightweaver"), port.calls);
  }

  @Test void failedClassRegistrationRemainsUnregisteredAndShutdownDoesNotRemoveForeignState() {
    RecordingPort port = new RecordingPort(false);
    NachtweberLifecycle lifecycle = new NachtweberLifecycle(port, NachtweberClassDefinition.create());
    assertThrows(IllegalStateException.class, lifecycle::start);
    lifecycle.shutdown();
    assertEquals(List.of("permission:nachtweber.use", "register:elite_nightweaver"), port.calls);
    assertFalse(lifecycle.isClassRegistered());
  }

  @Test void pluginEntrypointUsesProvenHytaleConstructorAndManifestStaysDisabled() throws Exception {
    assertTrue(JavaPlugin.class.isAssignableFrom(NachtweberPlugin.class));
    Constructor<NachtweberPlugin> constructor = NachtweberPlugin.class.getConstructor(JavaPluginInit.class);
    assertNotNull(constructor);
    String manifest;
    try (var stream = getClass().getClassLoader().getResourceAsStream("manifest.json")) {
      assertNotNull(stream);
      manifest = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
    assertTrue(manifest.contains("\"Main\": \"de.shadow.nachtweber.NachtweberPlugin\""));
    assertTrue(manifest.contains("\"DisabledByDefault\": true"));
    assertTrue(manifest.contains("\"Airijko:EndlessLevelingCore\""));
    assertTrue(manifest.contains("\"Ziggfreed:MMOSkillTree\""));
  }

  @Test void mavenBuildPinsAReproducibleOutputTimestamp() throws Exception {
    String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
    assertTrue(
        pom.contains("<project.build.outputTimestamp>2026-08-09T00:00:00Z</project.build.outputTimestamp>"),
        "Release artifacts need a fixed Maven output timestamp so identical sources produce identical JAR bytes");
  }

  private static final class RecordingPort implements NachtweberLifecycle.RuntimePort {
    private final boolean registerResult;
    private final List<String> calls = new ArrayList<>();
    private RecordingPort(boolean registerResult) { this.registerResult = registerResult; }
    @Override public void registerPermission(String permission) { calls.add("permission:" + permission); }
    @Override public boolean registerClass(CharacterClassDefinition definition) { calls.add("register:" + definition.getId()); return registerResult; }
    @Override public boolean unregisterClass(String id) { calls.add("unregister:" + id); return true; }
  }
}
