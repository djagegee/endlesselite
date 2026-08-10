package de.shadow.endlesselite.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Owns only exact registry values installed by this lifecycle. */
public final class OwnedRegistryLifecycle<T> {
  public interface Registry<T> {
    boolean registerIfAbsent(String id, T value);
    boolean unregister(String id, T expectedValue);
  }

  private final Registry<T> registry;
  private final Map<String, T> requested;
  private final Map<String, T> owned = new LinkedHashMap<>();
  private boolean started;

  public OwnedRegistryLifecycle(Registry<T> registry, Map<String, T> requested) {
    this.registry = Objects.requireNonNull(registry, "registry");
    Objects.requireNonNull(requested, "requested");
    this.requested = new LinkedHashMap<>();
    requested.forEach((id, value) -> this.requested.put(
        Objects.requireNonNull(id, "registry id"),
        Objects.requireNonNull(value, "registry value")));
  }

  public synchronized boolean start() {
    if (started || !owned.isEmpty() || requested.isEmpty()) return false;
    List<Map.Entry<String, T>> attempted = new ArrayList<>();
    Throwable registrationFailure = null;
    boolean collision = false;
    for (Map.Entry<String, T> entry : requested.entrySet()) {
      attempted.add(entry);
      try {
        if (!registry.registerIfAbsent(entry.getKey(), entry.getValue())) {
          collision = true;
          break;
        }
      } catch (Throwable error) {
        registrationFailure = error;
        break;
      }
    }
    if (collision || registrationFailure != null) {
      Throwable cleanupFailure = rollback(attempted);
      if (registrationFailure != null) {
        if (cleanupFailure != null && cleanupFailure != registrationFailure) {
          registrationFailure.addSuppressed(cleanupFailure);
        }
        rethrow(registrationFailure);
      }
      if (cleanupFailure != null) rethrow(cleanupFailure);
      return false;
    }
    attempted.forEach(entry -> owned.put(entry.getKey(), entry.getValue()));
    started = true;
    return true;
  }

  public synchronized void shutdown() {
    Throwable failure = unregisterReverse(new ArrayList<>(owned.entrySet()));
    owned.clear();
    started = false;
    if (failure != null) rethrow(failure);
  }

  public synchronized int ownedCount() {
    return owned.size();
  }

  private Throwable rollback(List<Map.Entry<String, T>> installed) {
    return unregisterReverse(installed);
  }

  private Throwable unregisterReverse(List<Map.Entry<String, T>> entries) {
    Throwable failure = null;
    for (int index = entries.size() - 1; index >= 0; index--) {
      Map.Entry<String, T> entry = entries.get(index);
      try {
        registry.unregister(entry.getKey(), entry.getValue());
      } catch (Throwable error) {
        if (failure == null) failure = error;
        else if (error != failure) failure.addSuppressed(error);
      }
    }
    return failure;
  }

  private static void rethrow(Throwable error) {
    if (error instanceof RuntimeException runtime) throw runtime;
    if (error instanceof Error fatal) throw fatal;
    throw new IllegalStateException("Registry lifecycle failed", error);
  }
}
