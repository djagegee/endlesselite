package de.shadow.endlessbook;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PersonalClaimsAccessTest {
  @Test void claimAccessIsRecheckedAtMutationTime() {
    UUID player = UUID.randomUUID();

    assertTrue(PersonalClaimsAccess.mayCreateClaim(player, id -> false));
    assertFalse(PersonalClaimsAccess.mayCreateClaim(player, id -> true));
  }
}
