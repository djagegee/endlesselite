package de.shadow.seuchenweber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Allows one bounded owner action per configured interval and supports explicit lifecycle cleanup. */
final class OwnerIntervalBudget {
  private final long intervalMs;
  private final Map<UUID, Long> nextClaimAtMs = new HashMap<>();

  OwnerIntervalBudget(long intervalMs) {
    this.intervalMs = Math.max(1L, intervalMs);
  }

  boolean tryClaim(UUID owner, long nowMs) {
    if (owner == null) return false;
    long next = nextClaimAtMs.getOrDefault(owner, Long.MIN_VALUE);
    if (nowMs < next) return false;
    nextClaimAtMs.put(owner, saturatedAdd(nowMs, intervalMs));
    return true;
  }

  void remove(UUID owner) {
    nextClaimAtMs.remove(owner);
  }

  void clear() {
    nextClaimAtMs.clear();
  }

  private static long saturatedAdd(long value, long increment) {
    if (increment > 0L && value > Long.MAX_VALUE - increment) return Long.MAX_VALUE;
    return value + increment;
  }
}
