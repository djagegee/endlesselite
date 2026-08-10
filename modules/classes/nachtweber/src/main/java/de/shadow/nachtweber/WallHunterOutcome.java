package de.shadow.nachtweber;
record WallHunterOutcome(WallHunterStatus status,double verticalVelocity){static WallHunterOutcome of(WallHunterStatus s){return new WallHunterOutcome(s,0);}}