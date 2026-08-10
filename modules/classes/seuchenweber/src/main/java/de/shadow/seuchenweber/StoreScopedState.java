package de.shadow.seuchenweber;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/** Thread-safe identity registry that prevents mutable runtime state crossing Hytale Stores. */
final class StoreScopedState<S, V> {
  private final Map<S, V> values = new IdentityHashMap<>();

  synchronized V getOrCreate(S store, Function<? super S, ? extends V> factory) {
    if (store == null) throw new IllegalArgumentException("store is required");
    V existing = values.get(store);
    if (existing != null) return existing;
    V created = factory.apply(store);
    if (created == null) throw new IllegalStateException("store state factory returned null");
    values.put(store, created);
    return created;
  }

  synchronized V getIfPresent(S store) {
    return store == null ? null : values.get(store);
  }

  synchronized void clear(Consumer<? super V> cleaner) {
    for (V value : new ArrayList<>(values.values())) cleaner.accept(value);
    values.clear();
  }

  synchronized void remove(S store, Consumer<? super V> cleaner) {
    if (store == null) return;
    V removed = values.remove(store);
    if (removed != null) cleaner.accept(removed);
  }

  synchronized int size() {
    return values.size();
  }
}
