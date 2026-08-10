package de.shadow.nachtweber;

import java.util.UUID;
import java.util.function.BooleanSupplier;

record PassiveVenomRequest(UUID owner,int target,boolean serverVerified,boolean toxicGlandsUnlocked,
    boolean huntingInstinctUnlocked,VenomDamageCause cause,long nowMs,BooleanSupplier targetImmune) { }
