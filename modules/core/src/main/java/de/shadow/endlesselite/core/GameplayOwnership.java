package de.shadow.endlesselite.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GameplayOwnership {
  private final Map<GameplayConcern, String> owners = new EnumMap<>(GameplayConcern.class);

  public synchronized void claim(GameplayConcern concern, String owner) {
    Objects.requireNonNull(concern, "concern");
    Objects.requireNonNull(owner, "owner");
    if (owner.isBlank()) throw new IllegalArgumentException("owner must not be blank");
    String existing = owners.putIfAbsent(concern, owner);
    if (existing != null && !existing.equals(owner)) {
      throw new IllegalStateException(
          concern + " already owned by " + existing + "; competing owner rejected: " + owner);
    }
  }

  public synchronized Optional<String> owner(GameplayConcern concern) {
    return Optional.ofNullable(owners.get(Objects.requireNonNull(concern, "concern")));
  }

  public synchronized void release(GameplayConcern concern, String owner) {
    owners.remove(Objects.requireNonNull(concern, "concern"), Objects.requireNonNull(owner, "owner"));
  }
}
