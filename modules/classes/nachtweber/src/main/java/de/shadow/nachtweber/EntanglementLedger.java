package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class EntanglementLedger {
  private final int stackCap;
  private final int boundThreshold;
  private final Map<Integer, Map<UUID, State>> byTarget = new HashMap<>();

  EntanglementLedger(int stackCap, int boundThreshold) {
    if (stackCap < 1 || boundThreshold < 1 || boundThreshold > stackCap) throw new IllegalArgumentException("invalid entanglement limits");
    this.stackCap = stackCap;
    this.boundThreshold = boundThreshold;
  }

  int apply(int target, UUID owner, int addedStacks, long durationMs, long nowMs) {
    if (target < 0 || owner == null || addedStacks < 1 || durationMs < 1) return 0;
    Map<UUID, State> owners = byTarget.computeIfAbsent(target, ignored -> new HashMap<>());
    State prior = owners.get(owner);
    int stacks = prior == null || prior.expiresAtMs <= nowMs ? Math.min(stackCap, addedStacks) : Math.min(stackCap, prior.stacks + addedStacks);
    owners.put(owner, new State(stacks, TimeMath.saturatedAdd(nowMs, durationMs)));
    return stacks;
  }

  int stacks(int target, UUID owner, long nowMs) {
    Map<UUID, State> owners = byTarget.get(target);
    if (owners == null) return 0;
    State state = owners.get(owner);
    if (state == null) return 0;
    if (state.expiresAtMs <= nowMs) { owners.remove(owner); if (owners.isEmpty()) byTarget.remove(target); return 0; }
    return state.stacks;
  }

  boolean isBound(int target, UUID owner, long nowMs) { return stacks(target, owner, nowMs) >= boundThreshold; }
  int consumeAllIfAtLeast(int target, UUID owner, int requiredStacks, long nowMs) {
    int available=stacks(target,owner,nowMs);
    if(requiredStacks<1||available<requiredStacks)return 0;
    cleanse(target,owner);
    return available;
  }
  void cleanse(int target, UUID owner) { Map<UUID, State> owners=byTarget.get(target); if (owners==null) return; owners.remove(owner); if (owners.isEmpty()) byTarget.remove(target); }
  void cleanseOwner(UUID owner) {
    if (owner == null) return;
    byTarget.entrySet().removeIf(entry -> {
      entry.getValue().remove(owner);
      return entry.getValue().isEmpty();
    });
  }
  int purgeExpired(long nowMs) {
    int before = byTarget.values().stream().mapToInt(Map::size).sum();
    byTarget.entrySet().removeIf(entry -> {
      entry.getValue().entrySet().removeIf(owner -> owner.getValue().expiresAtMs <= nowMs);
      return entry.getValue().isEmpty();
    });
    return before - byTarget.values().stream().mapToInt(Map::size).sum();
  }
  void clear() { byTarget.clear(); }
  private record State(int stacks, long expiresAtMs) { }
}
