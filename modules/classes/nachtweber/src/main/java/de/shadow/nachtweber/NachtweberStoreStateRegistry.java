package de.shadow.nachtweber;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class NachtweberStoreStateRegistry<S, V> {
  private final Map<S, V> byStore = new IdentityHashMap<>();

  synchronized V getOrCreate(S store, Supplier<V> factory) {
    Objects.requireNonNull(store, "store");
    Objects.requireNonNull(factory, "factory");
    V existing = byStore.get(store);
    if (existing != null) return existing;
    V created = Objects.requireNonNull(factory.get(), "factory result");
    byStore.put(store, created);
    return created;
  }

  boolean release(S store, Consumer<V> cleaner) {
    Objects.requireNonNull(store, "store");
    Objects.requireNonNull(cleaner, "cleaner");
    V removed;
    synchronized (this) { removed = byStore.remove(store); }
    if (removed == null) return false;
    cleaner.accept(removed);
    return true;
  }

  void clear(Consumer<V> cleaner) {
    Objects.requireNonNull(cleaner, "cleaner");
    List<V> removed;
    synchronized (this) {
      removed = new ArrayList<>(byStore.values());
      byStore.clear();
    }
    removed.forEach(cleaner);
  }

  boolean ifPresent(S store, Consumer<V> action) {
    Objects.requireNonNull(store, "store");
    Objects.requireNonNull(action, "action");
    V existing;
    synchronized (this) { existing = byStore.get(store); }
    if (existing == null) return false;
    action.accept(existing);
    return true;
  }

  synchronized int size() { return byStore.size(); }
}
