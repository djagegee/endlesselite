package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoreContractTest {
  @TempDir Path temp;

  @Test void defaultLinksUseVerifiedCommandsInRequiredOrder() {
    List<LinkDefinition> links = EndlessBookConfig.defaultLinks();
    assertEquals(List.of("your-path","augments","fates","mastery","guild","dungeons","claims"), links.stream().map(LinkDefinition::id).toList());
    assertEquals(List.of("profile","augments","fates","xp","guild","dungeons","pclaims"), links.stream().map(LinkDefinition::command).toList());
  }

  @Test void localizationSupportsGermanAndFallsBackToEnglish() {
    LinkDefinition link = new LinkDefinition("your-path","profile","YOUR PATH","DEIN PFAD",true);
    assertEquals("DEIN PFAD", EndlessBookConfig.localized(link,"de-DE"));
    assertEquals("YOUR PATH", EndlessBookConfig.localized(link,"en-US"));
    assertEquals("YOUR PATH", EndlessBookConfig.localized(link,"fr-FR"));
  }

  @Test void bootstrapCreatesCentralConfigWithoutOverwritingAdminChanges() throws Exception {
    Path file = EndlessBookConfig.bootstrap(temp);
    assertTrue(Files.isRegularFile(file));
    String original = Files.readString(file);
    assertTrue(original.contains("claim"));
    Files.writeString(file, "{\"adminEdited\":true}");
    EndlessBookConfig.bootstrap(temp);
    assertEquals("{\"adminEdited\":true}", Files.readString(file));
  }

  @Test void bookIsEntitledOnlyAfterMeetGuideQuestCompletes() {
    assertEquals("getting_started", QuestEntitlement.REQUIRED_QUEST);
    assertFalse(QuestEntitlement.mayOwnBook(false));
    assertTrue(QuestEntitlement.mayOwnBook(true));
  }

  @Test void packagedQuestOverrideAwardsBookInsteadOfMmoScroll() throws Exception {
    var stream = getClass().getClassLoader().getResourceAsStream("Server/MMOSkillTree/Quests/EndlessBook_Getting_Started.json");
    assertNotNull(stream);
    String json = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    assertTrue(json.contains("\"id\": \"getting_started\""));
    assertTrue(json.contains("EndlessBook_LevelMenu"));
    assertFalse(json.contains("Mmo_Menu_Scroll"));
  }
}
