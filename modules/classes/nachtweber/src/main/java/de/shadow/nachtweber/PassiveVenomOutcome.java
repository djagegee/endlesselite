package de.shadow.nachtweber;

record PassiveVenomOutcome(PassiveVenomStatus status,int appliedStacks,long nextReadyAtMs) {
  static PassiveVenomOutcome rejected(PassiveVenomStatus status){return new PassiveVenomOutcome(status,0,0L);}
}
