package de.shadow.seuchenweber;

/** Pure balance gate used by configuration tests and balance reporting. */
final class DamageBudget {
  private DamageBudget() { }

  static boolean isDotDominant(double directDamage, double necrotoxinDamage, double requiredDotShare) {
    if (!Double.isFinite(directDamage) || !Double.isFinite(necrotoxinDamage)
        || !Double.isFinite(requiredDotShare) || directDamage < 0.0 || necrotoxinDamage < 0.0
        || requiredDotShare <= 0.0 || requiredDotShare > 1.0) return false;
    double total = directDamage + necrotoxinDamage;
    return total > 0.0 && necrotoxinDamage / total >= requiredDotShare;
  }
}
