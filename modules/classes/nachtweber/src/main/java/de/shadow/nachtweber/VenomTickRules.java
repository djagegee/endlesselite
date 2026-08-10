package de.shadow.nachtweber;

record VenomTickRules(double baseDamagePerStack,double maximumTickDamage,double sorceryPointsForDoubleDamage) {
  VenomTickRules {
    if(!Double.isFinite(baseDamagePerStack)||baseDamagePerStack<=0.0||baseDamagePerStack>10_000.0)
      throw new IllegalArgumentException("baseDamagePerStack must be finite in (0,10000]");
    if(!Double.isFinite(maximumTickDamage)||maximumTickDamage<=0.0||maximumTickDamage>1_000_000.0)
      throw new IllegalArgumentException("maximumTickDamage must be finite in (0,1000000]");
    if(!Double.isFinite(sorceryPointsForDoubleDamage)||sorceryPointsForDoubleDamage<=0.0||sorceryPointsForDoubleDamage>1_000_000.0)
      throw new IllegalArgumentException("sorceryPointsForDoubleDamage must be finite in (0,1000000]");
  }
}
