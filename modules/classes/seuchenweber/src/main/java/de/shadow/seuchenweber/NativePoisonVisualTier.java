package de.shadow.seuchenweber;

/** Visual-only presentation tier for owner-scoped Nekrotoxin marks. */
enum NativePoisonVisualTier {
  NONE(null),
  POISON_I("Seuchenweber_Poison_Visual_T1"),
  POISON_II("Seuchenweber_Poison_Visual_T2"),
  POISON_III("Seuchenweber_Poison_Visual_T3");

  private final String effectId;

  NativePoisonVisualTier(String effectId) {
    this.effectId = effectId;
  }

  String effectId() {
    return effectId;
  }

  static NativePoisonVisualTier forStacks(int stacks) {
    if (stacks <= 0) return NONE;
    if (stacks <= 2) return POISON_I;
    if (stacks <= 4) return POISON_II;
    return POISON_III;
  }
}
