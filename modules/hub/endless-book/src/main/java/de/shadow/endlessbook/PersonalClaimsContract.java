package de.shadow.endlessbook;

import java.util.List;

final class PersonalClaimsContract {
  static final int GRID_SIZE = 9;
  static final int GRID_RADIUS = 4;
  static final String CELL_RESOURCE = "Pages/EndlessGuilds/GuildClaimCell.ui";
  static final String AVAILABLE_SELECTOR = "#StateClaimable";
  static final String SELECTED_SELECTOR = "#ZoneSel";
  static final String BUTTON_SELECTOR = "#CellBtn";
  static final String COMMAND = "pclaims";
  static final List<String> ALIASES = List.of("personalclaims", "pclaim");

  private PersonalClaimsContract() {}
}
