package de.shadow.seuchenweber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Ephemeral server state for bounded Astral Rift pulses; no overdue pulse bursts. */
final class AstralRiftRuntimeLedger {
  private final long pulseIntervalMs;
  private final Map<UUID, RiftState> riftsByOwner = new HashMap<>();
  private final List<UUID> expiredOwners = new ArrayList<>();

  AstralRiftRuntimeLedger(long pulseIntervalMs) {
    if (pulseIntervalMs <= 0L) throw new IllegalArgumentException("pulseIntervalMs must be positive");
    this.pulseIntervalMs = pulseIntervalMs;
  }

  void open(UUID owner, double x, double y, double z, long nowMs, long durationMs) {
    if (owner == null || durationMs <= 0L || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return;
    riftsByOwner.put(owner, new RiftState(owner, x, y, z, nowMs + durationMs, nowMs + pulseIntervalMs));
  }

  List<RiftPulse> consumeDue(long nowMs) {
    if (riftsByOwner.isEmpty()) return List.of();
    List<RiftPulse> due = null;
    for (var iterator = riftsByOwner.entrySet().iterator(); iterator.hasNext();) {
      RiftState state = iterator.next().getValue();
      if (nowMs >= state.expiresAtMs) {
        expiredOwners.add(state.owner);
        iterator.remove();
      } else if (nowMs >= state.nextPulseAtMs) {
        if (due == null) due = new ArrayList<>();
        due.add(new RiftPulse(state.owner, state.x, state.y, state.z));
        state.nextPulseAtMs = nowMs + pulseIntervalMs;
      }
    }
    return due == null ? List.of() : List.copyOf(due);
  }

  List<UUID> expireOnly(long nowMs) {
    if (riftsByOwner.isEmpty()) return List.of();
    for (var iterator = riftsByOwner.entrySet().iterator(); iterator.hasNext();) {
      RiftState state = iterator.next().getValue();
      if (nowMs < state.expiresAtMs) continue;
      expiredOwners.add(state.owner);
      iterator.remove();
    }
    return drainExpiredOwners();
  }

  int activeCount() {
    return riftsByOwner.size();
  }

  boolean hasOwner(UUID owner) {
    return riftsByOwner.containsKey(owner);
  }

  long nextEventAtMs() {
    long next = Long.MAX_VALUE;
    for (RiftState state : riftsByOwner.values()) {
      next = Math.min(next, Math.min(state.nextPulseAtMs, state.expiresAtMs));
    }
    return next;
  }

  List<UUID> drainExpiredOwners() {
    if (expiredOwners.isEmpty()) return List.of();
    List<UUID> drained = List.copyOf(expiredOwners);
    expiredOwners.clear();
    return drained;
  }

  void clear() {
    riftsByOwner.clear();
    expiredOwners.clear();
  }

  record RiftPulse(UUID owner, double x, double y, double z) { }

  private static final class RiftState {
    private final UUID owner;
    private final double x;
    private final double y;
    private final double z;
    private final long expiresAtMs;
    private long nextPulseAtMs;

    private RiftState(UUID owner, double x, double y, double z, long expiresAtMs, long nextPulseAtMs) {
      this.owner = owner;
      this.x = x;
      this.y = y;
      this.z = z;
      this.expiresAtMs = expiresAtMs;
      this.nextPulseAtMs = nextPulseAtMs;
    }
  }
}
