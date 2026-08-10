package de.shadow.nachtweber;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class NachtweberRuntimeCoordinator<S> {
  private final NachtweberStoreStateRegistry<S, NachtweberStoreRuntime> stores =
      new NachtweberStoreStateRegistry<>();
  private boolean closed;

  synchronized NachtweberStoreRuntime bind(
      S store, Supplier<? extends NachtweberStoreRuntime> factory) {
    if (closed) throw new IllegalStateException("Nachtweber runtime coordinator is closed");
    Objects.requireNonNull(factory, "factory");
    return stores.getOrCreate(store, () -> factory.get());
  }

  synchronized boolean maintain(S store, long nowMs) {
    if (closed || nowMs < 0L) return false;
    return stores.ifPresent(store, runtime -> runtime.maintain(nowMs));
  }

  synchronized boolean withGameplay(
      S store, Consumer<NachtweberGameplayRuntime> action) {
    Objects.requireNonNull(action, "action");
    if (closed) return false;
    boolean[] invoked = {false};
    stores.ifPresent(store, runtime -> {
      if (runtime instanceof NachtweberGameplayRuntime gameplay) {
        invoked[0] = true;
        action.accept(gameplay);
      }
    });
    return invoked[0];
  }

  synchronized boolean release(S store) {
    if (closed) return false;
    return stores.release(store, NachtweberStoreRuntime::close);
  }

  synchronized void shutdown() {
    if (closed) return;
    closed = true;
    stores.clear(NachtweberStoreRuntime::close);
  }

  synchronized int activeStoreCount() { return stores.size(); }
}
