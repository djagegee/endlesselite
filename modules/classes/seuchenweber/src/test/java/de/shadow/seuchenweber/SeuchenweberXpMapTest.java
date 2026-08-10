package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SeuchenweberXpMapTest {
  @Test void cosmicRuinSpellbookIsTheOnlyXpSourceForTheDedicatedTrack() throws Exception {
    try (var stream = getClass().getClassLoader().getResourceAsStream("Server/MMOSkillTree/XpMaps/Seuchenweber.json")) {
      assertTrue(stream != null);
      JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
      JsonObject payload = root.getAsJsonObject("Payload").getAsJsonObject("SEUCHENWEBER_MASTERY");
      assertEquals(1, payload.size());
      assertTrue(payload.has("ArcanePower_CosmicRuin_Spellbook"));
      assertTrue(payload.get("ArcanePower_CosmicRuin_Spellbook").getAsDouble() > 0.0);
    }
  }
}
