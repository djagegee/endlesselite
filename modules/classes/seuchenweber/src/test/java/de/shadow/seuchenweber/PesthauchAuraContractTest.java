package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PesthauchAuraContractTest {
  private static final Path SOURCE = Path.of(
      "src/main/java/de/shadow/seuchenweber/PesthauchAuraSystem.java");

  @Test void appliesOneOwnerBoundStackEveryTwoSecondsWithinEightBlocks() throws Exception {
    assertTrue(Files.exists(SOURCE), "Pesthauch runtime system is missing");
    String source = Files.readString(SOURCE, StandardCharsets.UTF_8);

    assertTrue(source.contains("extends EntityTickingSystem<EntityStore>"));
    assertTrue(source.contains("DEFAULT_RADIUS_BLOCKS = 8.0"));
    assertTrue(source.contains("DEFAULT_PULSE_INTERVAL_MS = 2_000L"));
    assertTrue(source.contains("private final double radiusBlocks"));
    assertTrue(source.contains("private final long pulseIntervalMs"));
    assertTrue(source.contains("Selector.selectNearbyEntities"));
    assertTrue(source.contains("nekrotoxin.apply(store, target, caster, owner, 1, durationMs)"));
    assertTrue(source.contains("MmoPassiveResolver.NECROTOXIC_MASTERY_ID"));
    assertTrue(source.contains("SeuchenweberTargeting.isHostileCombatMob"));
    assertFalse(source.contains("InventoryComponent"));
    assertFalse(source.contains("ItemStack"));
    assertFalse(source.contains("ParticleUtil"));
    assertFalse(source.contains("DamageSystems.executeDamage"));
  }

  @Test void pluginRegistersPesthauchInsteadOfTheOldHitPassive() throws Exception {
    String plugin = Files.readString(Path.of(
        "src/main/java/de/shadow/seuchenweber/SeuchenweberPlugin.java"), StandardCharsets.UTF_8);

    assertTrue(plugin.contains("private PesthauchAuraSystem pesthauchAuraSystem;"));
    assertTrue(plugin.contains("pesthauchAuraSystem = new PesthauchAuraSystem("));
    assertTrue(plugin.contains("config.pesthauchRadiusBlocks()"));
    assertTrue(plugin.contains("config.pesthauchPulseIntervalMs()"));
    assertTrue(plugin.contains("pesthauchAuraSystem.register("));
    assertTrue(plugin.contains("pesthauchAuraSystem.release(entityStore)"));
    assertTrue(plugin.contains("pesthauchAuraSystem = null; if (value != null) value.clear();"));
    assertFalse(plugin.contains("InnatePlagueTouch"));
  }
}
