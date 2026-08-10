package de.shadow.nachtweber;

enum VenomDamageCause {
  DIRECT_HIT(true), VENOM_TICK(false), REFLECTION(false), ENVIRONMENT(false);
  private final boolean mayApplyVenom;
  VenomDamageCause(boolean mayApplyVenom) { this.mayApplyVenom=mayApplyVenom; }
  boolean mayApplyVenom() { return mayApplyVenom; }
}
