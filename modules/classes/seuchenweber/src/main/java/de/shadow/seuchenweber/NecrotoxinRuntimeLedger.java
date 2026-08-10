package de.shadow.seuchenweber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ephemeral server-side state for ECS-backed Nekrotoxin ticks.
 * All access is intended from the owning world tick thread.
 */
final class NekrotoxinRuntimeLedger {
  private final int stackCap;
  private final long tickIntervalMs;
  private final Map<Integer, Map<UUID, State>> statesByTarget = new HashMap<>();

  NekrotoxinRuntimeLedger(int stackCap, long tickIntervalMs) {
    if (stackCap < 1 || tickIntervalMs < 1) throw new IllegalArgumentException("positive runtime limits required");
    this.stackCap = stackCap;
    this.tickIntervalMs = tickIntervalMs;
  }

  int apply(int targetEntityIndex, UUID owner, int stacks, long durationMs, long nowMs) {
    return apply(targetEntityIndex, owner, stacks, durationMs, nowMs, true);
  }

  int apply(int targetEntityIndex, UUID owner, int stacks, long durationMs, long nowMs,
      boolean allowAstralEcho) {
    if (targetEntityIndex < 0 || owner == null || stacks < 1 || durationMs < 1) return 0;
    Map<UUID, State> targetStates = statesByTarget.computeIfAbsent(targetEntityIndex, ignored -> new HashMap<>());
    State state = targetStates.get(owner);
    if (state == null || state.expiresAtMs <= nowMs) {
      targetStates.put(owner, new State(Math.min(stackCap, stacks), nowMs + durationMs,
          nowMs, allowAstralEcho));
      return Math.min(stackCap, stacks);
    }
    state.stacks = Math.min(stackCap, state.stacks + stacks);
    state.expiresAtMs = nowMs + durationMs;
    state.allowAstralEcho |= allowAstralEcho;
    return state.stacks;
  }

  TickResult consumeOneDueTick(int targetEntityIndex, UUID owner, long nowMs) {
    Map<UUID, State> targetStates = statesByTarget.get(targetEntityIndex);
    if (targetStates == null) return TickResult.NONE;
    State state = targetStates.get(owner);
    if (state == null) return TickResult.NONE;
    if (state.expiresAtMs <= nowMs) {
      targetStates.remove(owner);
      removeTargetIfEmpty(targetEntityIndex, targetStates);
      return TickResult.NONE;
    }
    if (nowMs < state.nextTickAtMs) return TickResult.NONE;
    state.nextTickAtMs = nowMs + tickIntervalMs;
    return new TickResult(state.stacks);
  }

  List<DueTick> consumeDueTicks(int targetEntityIndex, long nowMs) {
    Map<UUID, State> targetStates = statesByTarget.get(targetEntityIndex);
    if (targetStates == null) return List.of();
    List<DueTick> ticks = new ArrayList<>();
    targetStates.entrySet().removeIf(entry -> entry.getValue().expiresAtMs <= nowMs);
    for (Map.Entry<UUID, State> entry : targetStates.entrySet()) {
      State state = entry.getValue();
      if (nowMs >= state.nextTickAtMs) {
        state.nextTickAtMs = nowMs + tickIntervalMs;
        ticks.add(new DueTick(entry.getKey(), state.stacks, state.allowAstralEcho));
      }
    }
    removeTargetIfEmpty(targetEntityIndex, targetStates);
    return List.copyOf(ticks);
  }

  List<NaturalExpiration> consumeNaturalExpirations(int targetEntityIndex, long nowMs) {
    List<NaturalExpiration> fullMarks = new ArrayList<>();
    for (Expiration expiration : consumeExpirations(targetEntityIndex, nowMs)) {
      if (expiration.fullMark()) {
        fullMarks.add(new NaturalExpiration(expiration.owner(), expiration.stacks()));
      }
    }
    return List.copyOf(fullMarks);
  }

  List<Expiration> consumeExpirations(int targetEntityIndex, long nowMs) {
    Map<UUID, State> targetStates = statesByTarget.get(targetEntityIndex);
    if (targetStates == null) return List.of();
    List<Expiration> expirations = new ArrayList<>();
    Iterator<Map.Entry<UUID, State>> iterator = targetStates.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<UUID, State> entry = iterator.next();
      State state = entry.getValue();
      if (state.expiresAtMs > nowMs) continue;
      expirations.add(new Expiration(entry.getKey(), state.stacks, state.stacks >= stackCap));
      iterator.remove();
    }
    removeTargetIfEmpty(targetEntityIndex, targetStates);
    return List.copyOf(expirations);
  }

  boolean hasOwner(UUID owner) {
    if (owner == null) return false;
    for (Map<UUID, State> targetStates : statesByTarget.values()) {
      if (targetStates.containsKey(owner)) return true;
    }
    return false;
  }

  List<TargetExpiration> consumeAllExpirations(long nowMs) {
    if (statesByTarget.isEmpty()) return List.of();
    List<TargetExpiration> all = new ArrayList<>();
    for (int targetEntityIndex : List.copyOf(statesByTarget.keySet())) {
      for (Expiration expiration : consumeExpirations(targetEntityIndex, nowMs)) {
        all.add(new TargetExpiration(targetEntityIndex, expiration.owner(),
            expiration.stacks(), expiration.fullMark()));
      }
    }
    return List.copyOf(all);
  }

  boolean hasState(int targetEntityIndex, UUID owner) {
    Map<UUID, State> targetStates = statesByTarget.get(targetEntityIndex);
    return targetStates != null && targetStates.containsKey(owner);
  }

  boolean hasTarget(int targetEntityIndex) {
    return statesByTarget.containsKey(targetEntityIndex);
  }

  int maximumActiveStacks(int targetEntityIndex) {
    Map<UUID, State> targetStates = statesByTarget.get(targetEntityIndex);
    if (targetStates == null || targetStates.isEmpty()) return 0;
    int maximum = 0;
    for (State state : targetStates.values()) maximum = Math.max(maximum, state.stacks);
    return maximum;
  }

  long maximumRemainingDurationMs(int targetEntityIndex, long nowMs) {
    Map<UUID, State> targetStates = statesByTarget.get(targetEntityIndex);
    if (targetStates == null || targetStates.isEmpty()) return 0L;
    long maximum = 0L;
    for (State state : targetStates.values()) {
      maximum = Math.max(maximum, Math.max(0L, state.expiresAtMs - nowMs));
    }
    return maximum;
  }

  List<UUID> discardTarget(int targetEntityIndex) {
    Map<UUID, State> discarded = statesByTarget.remove(targetEntityIndex);
    return discarded == null ? List.of() : List.copyOf(discarded.keySet());
  }

  void clear() {
    statesByTarget.clear();
  }

  record DueTick(UUID owner, int stacks, boolean allowAstralEcho) {
    DueTick(UUID owner, int stacks) { this(owner, stacks, true); }
  }

  record NaturalExpiration(UUID owner, int stacks) { }

  record Expiration(UUID owner, int stacks, boolean fullMark) { }

  record TargetExpiration(int targetEntityIndex, UUID owner, int stacks, boolean fullMark) { }

  record TickResult(int stacks) {
    static final TickResult NONE = new TickResult(0);
    boolean isEmpty() { return stacks == 0; }
  }

  private void removeTargetIfEmpty(int targetEntityIndex, Map<UUID, State> targetStates) {
    if (targetStates.isEmpty()) statesByTarget.remove(targetEntityIndex, targetStates);
  }

  private static final class State {
    private int stacks;
    private long expiresAtMs;
    private long nextTickAtMs;
    private boolean allowAstralEcho;

    private State(int stacks, long expiresAtMs, long nextTickAtMs, boolean allowAstralEcho) {
      this.stacks = stacks;
      this.expiresAtMs = expiresAtMs;
      this.nextTickAtMs = nextTickAtMs;
      this.allowAstralEcho = allowAstralEcho;
    }
  }
}
