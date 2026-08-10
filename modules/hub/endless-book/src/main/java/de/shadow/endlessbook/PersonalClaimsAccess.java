package de.shadow.endlessbook;

import java.util.UUID;

final class PersonalClaimsAccess {
  @FunctionalInterface
  interface MembershipLookup { boolean isInGuild(UUID playerId); }

  private PersonalClaimsAccess() {}

  static boolean mayCreateClaim(UUID playerId, MembershipLookup membership) {
    return playerId != null && membership != null && !membership.isInGuild(playerId);
  }
}
