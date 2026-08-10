package de.shadow.seuchenweber;

/** Pure, testable unlock contract for the Seuchenweber class. */
final class SeuchenweberUnlockRules {
  static final int REQUIRED_PRESTIGE = 30;
  static final String UNLOCK_ITEM_ID = "ArcanePower_CosmicRuin_Spellbook";

  private SeuchenweberUnlockRules() { }

  static boolean isUnlockItem(String itemId) {
    return UNLOCK_ITEM_ID.equals(itemId);
  }

  static boolean isEligible(int prestige, boolean hasUnlockItem) {
    return prestige >= REQUIRED_PRESTIGE && hasUnlockItem;
  }
}
