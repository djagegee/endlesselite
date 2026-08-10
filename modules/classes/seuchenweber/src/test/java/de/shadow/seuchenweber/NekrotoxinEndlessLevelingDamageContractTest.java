package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NekrotoxinEndlessLevelingDamageContractTest {
  @Test void usesOfficialAbilityDotFactoryAndRestoresNativePoisonCause() throws Exception {
    String source = Files.readString(Path.of("src/main/java/de/shadow/seuchenweber/NecrotoxinDamageSystem.java"),
        StandardCharsets.UTF_8);
    assertTrue(source.contains("PlayerCombatSystem.createAbilityDotDamage"));
    assertTrue(source.contains("setDamageCauseIndex(SeuchenweberDamageCause.requireIndex())"));
  }
}
