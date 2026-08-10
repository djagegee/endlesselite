package de.shadow.endlesselite.core;

import java.util.Objects;

/** Executes every cleanup action and rethrows the first failure after all actions ran. */
public final class BestEffortCleanup {
  @FunctionalInterface
  public interface Action {
    void run() throws Throwable;
  }

  private BestEffortCleanup() { }

  public static void run(Action... actions) {
    Objects.requireNonNull(actions, "actions");
    Throwable failure = null;
    for (Action action : actions) {
      try {
        Objects.requireNonNull(action, "cleanup action").run();
      } catch (Throwable error) {
        if (failure == null) failure = error;
        else if (error != failure) failure.addSuppressed(error);
      }
    }
    if (failure != null) rethrow(failure);
  }

  private static void rethrow(Throwable error) {
    if (error instanceof RuntimeException runtime) throw runtime;
    if (error instanceof Error fatal) throw fatal;
    throw new IllegalStateException("Cleanup failed", error);
  }
}
