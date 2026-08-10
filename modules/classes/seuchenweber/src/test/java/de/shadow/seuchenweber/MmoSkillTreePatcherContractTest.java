package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MmoSkillTreePatcherContractTest {
  @Test void patchesCustomTooltipAndPassiveCardButtonInExactlyTwoClasses() throws Exception {
    Path tool = Path.of("tools/mmoskilltree-card-description-patcher");
    String patcher = Files.readString(tool.resolve("CardDescriptionPatcher.java"), StandardCharsets.UTF_8);
    String verifier = Files.readString(tool.resolve("verify_patch.py"), StandardCharsets.UTF_8);

    assertTrue(patcher.contains("AbilityDescriptionRenderer.class"));
    assertTrue(patcher.contains("AbilityBindPage.class"));
    assertTrue(patcher.contains("renderMsg"));
    assertTrue(patcher.contains("ability.bind_page.passive_tag"));
    assertTrue(verifier.contains("CHANGED_ENTRIES=2"));
  }
}