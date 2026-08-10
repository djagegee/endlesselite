package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EndlessDetailsDataTest {
  @Test void preservesResolvedAugmentAndFateValuesWithoutRecalculation() {
    Map<String, Object> profile = Map.of(
        "name", "Agegee",
        "level", 42,
        "prestige", 7,
        "attrTotals", Map.of("STRENGTH", "65", "PRECISION", "31%"),
        "augments", List.of(Map.of("name", "Titan's Might", "value", "+18.5%", "description", "Resolved by EndlessLeveling"))
    );
    String fateJson = """
        {"slots":[{"slot":"CROWN","mainStat":{"stat":"STRENGTH","value":12.75,"formatted":"+12.75"},"substats":[{"stat":"CRIT","value":8.5,"formatted":"+8.5%"}]}],
         "setBonuses":[{"setName":"Stormcaller","pieces":4,"twoPiece":[{"stat":"HASTE","value":10.0,"formatted":"+10%"}],"fourPieceDesc":"Accurate proc text"}]}
        """;

    EndlessDetailsData data = EndlessDetailsData.from(profile, fateJson, 420.5, 900.0);

    assertEquals("+18.5%", data.augments().getFirst().get("value"));
    assertEquals(12.75, data.fateSlots().getFirst().getAsJsonObject("mainStat").get("value").getAsDouble());
    assertEquals(8.5, data.fateSlots().getFirst().getAsJsonArray("substats").get(0).getAsJsonObject().get("value").getAsDouble());
    assertEquals("Accurate proc text", data.setBonuses().getFirst().get("fourPieceDesc").getAsString());
    assertEquals(420.5, data.currentXp());
    assertEquals(900.0, data.nextLevelXp());
  }

  @Test void missingOptionalIntegrationsProduceSafeEmptyDetails() {
    EndlessDetailsData data = EndlessDetailsData.from(null, null, 0.0, 0.0);
    assertTrue(data.profile().isEmpty());
    assertTrue(data.augments().isEmpty());
    assertTrue(data.fateSlots().isEmpty());
    assertTrue(data.setBonuses().isEmpty());
  }
}
