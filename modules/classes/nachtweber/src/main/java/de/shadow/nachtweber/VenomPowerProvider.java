package de.shadow.nachtweber;

import java.util.UUID;

@FunctionalInterface
interface VenomPowerProvider {
  double sorcery(UUID owner);
}
