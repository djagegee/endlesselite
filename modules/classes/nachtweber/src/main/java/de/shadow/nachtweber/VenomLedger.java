package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.function.BooleanSupplier;

final class VenomLedger {
  private final int stackCap;
  private final long tickIntervalMs;
  private final Map<Integer, Map<UUID, State>> byTarget = new HashMap<>();

  VenomLedger(int stackCap, long tickIntervalMs) {
    if (stackCap < 1 || tickIntervalMs < 1) throw new IllegalArgumentException("positive venom limits required");
    this.stackCap=stackCap; this.tickIntervalMs=tickIntervalMs;
  }

  int apply(int target, UUID owner, int addedStacks, long durationMs, long nowMs, BooleanSupplier immune) {
    if (target < 0 || owner == null || immune == null || immune.getAsBoolean() || addedStacks < 1 || durationMs < 1) return 0;
    Map<UUID, State> owners=byTarget.computeIfAbsent(target, ignored -> new HashMap<>());
    State prior=owners.get(owner);
    int stacks=prior==null || prior.expiresAtMs<=nowMs ? Math.min(stackCap,addedStacks) : Math.min(stackCap,prior.stacks+addedStacks);
    long next=prior==null || prior.expiresAtMs<=nowMs ? TimeMath.saturatedAdd(nowMs,tickIntervalMs) : prior.nextTickAtMs;
    owners.put(owner,new State(stacks,TimeMath.saturatedAdd(nowMs,durationMs),next));
    return stacks;
  }

  DueTick consumeOneDueTick(int target, UUID owner, long nowMs) {
    Map<UUID, State> owners=byTarget.get(target); if (owners==null) return DueTick.EMPTY;
    State state=owners.get(owner); if (state==null) return DueTick.EMPTY;
    if (state.expiresAtMs<=nowMs) { cleanse(target,owner); return DueTick.EMPTY; }
    if (nowMs<state.nextTickAtMs) return DueTick.EMPTY;
    state.nextTickAtMs=TimeMath.saturatedAdd(nowMs,tickIntervalMs);
    return new DueTick(state.stacks,state.nextTickAtMs);
  }

  boolean hasState(int target, UUID owner, long nowMs) { Map<UUID,State> owners=byTarget.get(target); if(owners==null) return false; State s=owners.get(owner); if(s==null)return false; if(s.expiresAtMs<=nowMs){cleanse(target,owner);return false;} return true; }
  List<UUID> activeOwners(int target, long nowMs) {
    Map<UUID, State> owners = byTarget.get(target);
    if (owners == null) return List.of();
    owners.entrySet().removeIf(entry -> entry.getValue().expiresAtMs <= nowMs);
    if (owners.isEmpty()) { byTarget.remove(target); return List.of(); }
    return List.copyOf(owners.keySet());
  }
  void cleanse(int target, UUID owner) { Map<UUID,State> owners=byTarget.get(target); if(owners==null)return; owners.remove(owner); if(owners.isEmpty())byTarget.remove(target); }
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
  record DueTick(int stacks,long nextTickAtMs) { static final DueTick EMPTY=new DueTick(0,0); boolean isEmpty(){return stacks==0;} }
  private static final class State { private final int stacks; private final long expiresAtMs; private long nextTickAtMs; private State(int stacks,long expiresAtMs,long nextTickAtMs){this.stacks=stacks;this.expiresAtMs=expiresAtMs;this.nextTickAtMs=nextTickAtMs;} }
}
