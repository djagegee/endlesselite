package de.shadow.nachtweber;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class NachtweberMmoContract {
  static final String SKILL_ID = "NACHTWEBER_MASTERY";
  private static final List<Unlock> UNLOCKS = List.of(
      new Unlock(1, "nachtweber_passive_danger_sense", "Gefahrensinn", "Danger Sense", true),
      new Unlock(2, "nachtweber_black_thread", "Schwarzer Faden", "Black Thread", false),
      new Unlock(25, "nachtweber_shadow_swing", "Schattenschwung", "Shadow Swing", false),
      new Unlock(40, "nachtweber_passive_wall_hunter", "Wandjäger", "Wall Hunter", true),
      new Unlock(55, "nachtweber_hunting_cocoon", "Jagdkokon", "Hunting Cocoon", false),
      new Unlock(70, "nachtweber_passive_toxic_glands", "Giftige Drüsen", "Toxic Glands", true),
      new Unlock(90, "nachtweber_passive_hunting_instinct", "Jagdinstinkt", "Hunting Instinct", true));

  private NachtweberMmoContract() { }
  static List<Unlock> unlocks() { return UNLOCKS; }
  static Set<String> activeAbilityIds() { return ids(false); }
  static Set<String> passiveAbilityIds() { return ids(true); }
  static Set<Integer> unlockLevels() { return UNLOCKS.stream().map(Unlock::level).collect(Collectors.toUnmodifiableSet()); }
  static Set<String> allAbilityIds() { LinkedHashSet<String> ids=new LinkedHashSet<>(); UNLOCKS.forEach(u->ids.add(u.abilityId())); return Set.copyOf(ids); }
  private static Set<String> ids(boolean passive) { LinkedHashSet<String> ids=new LinkedHashSet<>(); UNLOCKS.stream().filter(u->u.passive()==passive).forEach(u->ids.add(u.abilityId())); return Set.copyOf(ids); }
  record Unlock(int level,String abilityId,String germanName,String englishName,boolean passive) { }
}
