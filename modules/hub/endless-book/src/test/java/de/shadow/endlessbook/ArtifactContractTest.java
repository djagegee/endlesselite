package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ArtifactContractTest {
  private String resource(String name) throws Exception {
    try (var stream = getClass().getClassLoader().getResourceAsStream(name)) {
      assertNotNull(stream, name);
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test void manifestIsDependencyReducedAndContainsAssetPack() throws Exception {
    String manifest = resource("manifest.json");
    assertTrue(manifest.contains("Ziggfreed:MMOSkillTree"));
    assertTrue(manifest.contains("\"IncludesAssetPack\": true"));
    assertFalse(manifest.contains("Aetherhaven"));
  }

  @Test void itemHasOwnIconPermanentSingleItemAndCompleteInteractionChain() throws Exception {
    String item = resource("Server/Item/Items/EndlessBook_LevelMenu.json");
    assertTrue(item.contains("EndlessBook_InfinityTerminal_Red.png"));
    assertTrue(item.contains("\"MaxStack\": 1"));
    assertTrue(item.contains("\"DropOnDeath\": false"));
    assertTrue(resource("Server/Item/Interactions/EndlessLevelMenu.json").contains("\"Type\": \"EndlessLevelMenu\""));
    assertTrue(resource("Server/Item/RootInteractions/EndlessLevelMenu.json").contains("\"EndlessLevelMenu\""));
  }

  @Test void uiProvidesSevenDefaultsAndFiveConfigSlots() throws Exception {
    String ui = resource("Common/UI/Custom/EndlessBook/EndlessBookHub.ui");
    for (int i = 1; i <= 12; i++) assertTrue(ui.contains("#LinkButton" + i));
  }

  @Test void itemIsLocalizedInGermanAndEnglish() throws Exception {
    assertTrue(resource("Server/Languages/de-DE/endlessbook.lang").contains("Endless-Buch"));
    assertTrue(resource("Server/Languages/en-US/endlessbook.lang").contains("Endless Book"));
  }

  @Test void endlessBookOwnsThePersonalClaimsPatchAssets() throws Exception {
    String manifest = resource("manifest.json");
    assertTrue(manifest.contains("Airijko:EndlessGuilds"));
    String page = resource("Common/UI/Custom/EndlessBook/PersonalClaimsPage.ui");
    String cell = resource("Common/UI/Custom/EndlessBook/PersonalClaimCell.ui");
    assertTrue(page.contains("#InfoLimitValue"));
    assertTrue(page.contains("#GridRow8"));
    assertTrue(cell.contains("#CellButton"));
  }

  @Test void hubIsAPermanentLargeMmoCharacterSheet() throws Exception {
    String hub = resource("Common/UI/Custom/EndlessBook/EndlessBookHub.ui");
    assertTrue(hub.contains("Width: 1480, Height: 850"));
    assertFalse(hub.contains("#ShowDetailsButton"));
    assertTrue(hub.contains("#AttributesSection"));
    assertTrue(hub.contains("#AugmentsSection"));
    assertTrue(hub.contains("#FatesSection"));
    assertTrue(hub.contains("#SetBonusesSection"));
    assertTrue(hub.contains("#QuickAccessRail"));
    assertTrue(hub.contains("Color: #101a2c(0.96)"));
    assertTrue(hub.contains("CharacterPreviewComponent #CharacterPreview"));
    assertNotNull(resource("Common/UI/Custom/EndlessBook/EndlessBookDetailRow.ui"));

    String manifest = resource("manifest.json");
    assertTrue(manifest.contains("\"Airijko:EndlessLevelingCore\""));
    assertTrue(manifest.contains("\"Airijko:EndlessFates\""));
  }
}
