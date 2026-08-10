package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class SeuchenweberTreeBonusResolverTest {
  @Test void sumsOnlyClaimedRewardsOfTheRequestedBonusKind() {
    Set<String> claimed = Set.of(
        "seuchenweber_dot_damage_5_l18",
        "seuchenweber_dot_damage_10_l100",
        "seuchenweber_direct_damage_5_l15");
    assertEquals(15.0, SeuchenweberTreeBonusResolver.claimedPercent(claimed, "dot_damage"));
    assertEquals(5.0, SeuchenweberTreeBonusResolver.claimedPercent(claimed, "direct_damage"));
    assertEquals(0.0, SeuchenweberTreeBonusResolver.claimedPercent(claimed, "crit_damage"));
  }

  @Test void appliesFinitePercentBonusWithoutProducingNegativeOrNanDamage() {
    assertEquals(115.0f, SeuchenweberTreeBonusResolver.applyPercent(100.0f, 15.0));
    assertEquals(0.0f, SeuchenweberTreeBonusResolver.applyPercent(Float.NaN, 15.0));
    assertEquals(0.0f, SeuchenweberTreeBonusResolver.applyPercent(100.0f, -1.0));
  }
}
