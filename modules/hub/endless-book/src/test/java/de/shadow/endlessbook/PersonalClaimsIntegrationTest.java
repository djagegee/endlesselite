package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class PersonalClaimsIntegrationTest {
  @Test void personalClaimIntegrationUsesOwnedNineByNineGridContract() {
    assertEquals(9, PersonalClaimsContract.GRID_SIZE);
    assertEquals(4, PersonalClaimsContract.GRID_RADIUS);
    assertEquals("Pages/EndlessGuilds/GuildClaimCell.ui", PersonalClaimsContract.CELL_RESOURCE);
    assertEquals("#StateClaimable", PersonalClaimsContract.AVAILABLE_SELECTOR);
    assertEquals("#ZoneSel", PersonalClaimsContract.SELECTED_SELECTOR);
    assertEquals("#CellBtn", PersonalClaimsContract.BUTTON_SELECTOR);
  }

  @Test void integrationCommandKeepsPatchCompatibilityAliases() {
    assertEquals("pclaims", PersonalClaimsContract.COMMAND);
    assertEquals(List.of("personalclaims", "pclaim"), PersonalClaimsContract.ALIASES);
  }

  @Test void endlessBookPluginOwnsTheIntegratedCommand() {
    assertEquals(List.of("pclaims"), EndlessBookIntegrations.commandNames());
  }
}
