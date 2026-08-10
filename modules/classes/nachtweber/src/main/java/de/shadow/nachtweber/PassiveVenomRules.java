package de.shadow.nachtweber;

record PassiveVenomRules(int toxicGlandsStacks,int huntingInstinctBonusStacks,long venomDurationMs,long internalCooldownMs) {
  PassiveVenomRules {
    if(toxicGlandsStacks<1||toxicGlandsStacks>100)throw new IllegalArgumentException("toxicGlandsStacks must be in [1,100]");
    if(huntingInstinctBonusStacks<0||huntingInstinctBonusStacks>100)throw new IllegalArgumentException("huntingInstinctBonusStacks must be in [0,100]");
    if(venomDurationMs<1||internalCooldownMs<1)throw new IllegalArgumentException("positive durations required");
  }
}
