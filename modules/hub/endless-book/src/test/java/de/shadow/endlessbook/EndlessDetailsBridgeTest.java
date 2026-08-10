package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EndlessDetailsBridgeTest {
  @Test void combinesAuthoritativeLevelingAndFatesReaders() {
    UUID player = UUID.randomUUID();
    EndlessDetailsBridge.LevelingReader leveling = new EndlessDetailsBridge.LevelingReader() {
      public Map<String, Object> profileCard(UUID uuid) { return Map.of("name", "Agegee", "augments", List.of(Map.of("value", "+22%"))); }
      public double currentXp(UUID uuid) { return 88.0; }
      public double nextLevelXp(UUID uuid) { return 120.0; }
    };
    EndlessDetailsBridge.FatesReader fates = uuid -> "{\"slots\":[{\"slot\":\"CROWN\"}],\"setBonuses\":[]}";

    EndlessDetailsData data = EndlessDetailsBridge.load(player, leveling, fates);

    assertEquals("Agegee", data.profile().get("name"));
    assertEquals("+22%", data.augments().getFirst().get("value"));
    assertEquals("CROWN", data.fateSlots().getFirst().get("slot").getAsString());
    assertEquals(88.0, data.currentXp());
    assertEquals(120.0, data.nextLevelXp());
  }

  @Test void absentRuntimeModsDoNotBreakDetails() {
    EndlessDetailsData data = EndlessDetailsBridge.load(UUID.randomUUID(),
        EndlessDetailsBridge.unavailableLeveling(), EndlessDetailsBridge.unavailableFates());
    assertTrue(data.profile().isEmpty());
    assertTrue(data.augments().isEmpty());
    assertTrue(data.fateSlots().isEmpty());
  }
}
