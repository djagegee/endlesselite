package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Identity-partitioned owner binding; equal-but-distinct stores never share entity refs. */
final class OwnerEntityBindingRegistry<S, R> {
  private final IdentityHashMap<S, Map<UUID, R>> byStore = new IdentityHashMap<>();

  synchronized boolean bind(S store, UUID owner, R ref) {
    if (store == null || owner == null || ref == null) return false;
    byStore.computeIfAbsent(store, ignored -> new HashMap<>()).put(owner, ref);
    return true;
  }

  synchronized R lookup(S store, UUID owner) {
    if (store == null || owner == null) return null;
    Map<UUID, R> owners = byStore.get(store);
    return owners == null ? null : owners.get(owner);
  }

  synchronized boolean removeOwner(S store, UUID owner) {
    if (store == null || owner == null) return false;
    Map<UUID, R> owners = byStore.get(store);
    if (owners == null || owners.remove(owner) == null) return false;
    if (owners.isEmpty()) byStore.remove(store);
    return true;
  }

  synchronized boolean release(S store) {
    return store != null && byStore.remove(store) != null;
  }

  synchronized void clear() {
    byStore.clear();
  }

  synchronized int storeCount() { return byStore.size(); }
}
