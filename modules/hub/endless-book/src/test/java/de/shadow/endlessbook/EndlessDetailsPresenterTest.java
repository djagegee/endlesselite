package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EndlessDetailsPresenterTest {
  @Test void expandsEveryResolvedBonusSourceIntoRows() {
    Map<String, Object> profile = Map.of(
        "attrTotals", Map.of("STRENGTH", "65", "PRECISION", "31%"),
        "augments", List.of(
            Map.of("name", "Titan's Might", "tier", "EPIC", "value", "+18.5%", "details", "Damage +18.5%; Guard +7%"),
            Map.of("name", "Flow", "tier", "COMMON", "value", "+12", "details", "Flat Flow +12")
        ));
    String fates = """
        {"slots":[
          {"slot":"CROWN","setName":"Stormcaller","mainStat":{"stat":"STRENGTH","formatted":"+12.75"},"substats":[{"stat":"CRIT","formatted":"+8.5%"},{"stat":"HASTE","formatted":"+6"}]},
          {"slot":"SOUL","setName":"Stormcaller","mainStat":{"stat":"LIFE","formatted":"+120"},"substats":[{"stat":"DEFENSE","formatted":"+5"}]}
        ],"setBonuses":[
          {"setName":"Stormcaller","pieces":4,"twoPiece":[{"stat":"HASTE","formatted":"+10%"}],"fourPieceDesc":"Lightning proc: 25%"}
        ]}
        """;
    EndlessDetailsData data = EndlessDetailsData.from(profile, fates, 0, 0);

    EndlessDetailsPresenter presenter = new EndlessDetailsPresenter(data);

    assertEquals(2, presenter.attributeRows().size());
    assertEquals(2, presenter.augmentRows().size());
    assertTrue(presenter.augmentRows().stream().anyMatch(row -> row.details().contains("Guard +7%")));
    assertEquals(5, presenter.fateRows().size(), "two main stats plus three substats");
    assertTrue(presenter.fateRows().stream().anyMatch(row -> row.value().equals("+8.5%")));
    assertEquals(2, presenter.setBonusRows().size(), "2-piece stat and 4-piece proc");
    assertTrue(presenter.setBonusRows().stream().anyMatch(row -> row.details().contains("25%")));
  }
}
