package de.shadow.nachtweber;

import java.util.UUID;

@FunctionalInterface
interface VenomDamagePort {
  boolean emit(UUID owner,int target,float damage,VenomDamageCause cause);
}
