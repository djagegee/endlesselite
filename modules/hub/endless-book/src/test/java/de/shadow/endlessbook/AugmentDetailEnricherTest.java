package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AugmentDetailEnricherTest {
  @Test void addsResolvedDetailsWithoutChangingAuthoritativeCardValues() {
    Map<String, Object> profile = Map.of("augments", List.of(
        Map.of("id", "titans_might", "name", "Titan's Might", "tier", "EPIC", "value", "EPIC"),
        Map.of("id", "common_stat:strength", "name", "Strength", "tier", "COMMON", "value", "+12")
    ));

    Map<String, Object> enriched = AugmentDetailEnricher.enrich(profile,
        id -> id.equals("titans_might") ? "Damage +18.5%\nGuard +7%" : "");
    @SuppressWarnings("unchecked") List<Map<String, String>> augments = (List<Map<String, String>>) enriched.get("augments");

    assertEquals("EPIC", augments.get(0).get("value"));
    assertEquals("Damage +18.5%\nGuard +7%", augments.get(0).get("details"));
    assertEquals("+12", augments.get(1).get("value"));
  }
}
