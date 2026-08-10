package de.shadow.endlesselite.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ModuleCoordinator {
  private final List<ManagedModule> modules;
  private final List<ManagedModule> started = new ArrayList<>();

  public ModuleCoordinator(List<ManagedModule> modules) {
    this.modules = List.copyOf(Objects.requireNonNull(modules, "modules"));
  }

  public synchronized void startAll() {
    if (!started.isEmpty()) throw new IllegalStateException("Modules already started");
    try {
      for (ManagedModule module : modules) {
        module.start();
        started.add(module);
      }
    } catch (RuntimeException | Error failure) {
      rollback(failure);
      throw failure;
    }
  }

  public synchronized void stopAll() {
    Throwable failure = null;
    List<ManagedModule> reverse = new ArrayList<>(started);
    Collections.reverse(reverse);
    for (ManagedModule module : reverse) {
      try {
        module.stop();
      } catch (Throwable error) {
        if (failure == null) failure = error;
        else if (error != failure) failure.addSuppressed(error);
      }
    }
    started.clear();
    if (failure != null) rethrow(failure);
  }

  private void rollback(Throwable cause) {
    List<ManagedModule> reverse = new ArrayList<>(started);
    Collections.reverse(reverse);
    for (ManagedModule module : reverse) {
      try {
        module.stop();
      } catch (Throwable error) {
        if (error != cause) cause.addSuppressed(error);
      }
    }
    started.clear();
  }

  private static void rethrow(Throwable failure) {
    if (failure instanceof RuntimeException runtime) throw runtime;
    if (failure instanceof Error error) throw error;
    throw new IllegalStateException("Unexpected checked module failure", failure);
  }
}
