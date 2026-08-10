package de.shadow.nachtweber;

import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import java.util.Objects;

final class NachtweberLifecycle {
  interface RuntimePort {
    void registerPermission(String permission);
    boolean registerClass(CharacterClassDefinition definition);
    boolean unregisterClass(String id);
  }

  private final RuntimePort port;
  private final CharacterClassDefinition definition;
  private boolean startAttempted;
  private boolean classRegistered;

  NachtweberLifecycle(RuntimePort port, CharacterClassDefinition definition) {
    this.port = Objects.requireNonNull(port, "port");
    this.definition = Objects.requireNonNull(definition, "definition");
  }

  synchronized void start() {
    if (classRegistered) return;
    if (startAttempted) throw new IllegalStateException("Nachtweber lifecycle already attempted and did not register");
    startAttempted = true;
    port.registerPermission(NachtweberClassDefinition.PERMISSION);
    if (!port.registerClass(definition)) {
      throw new IllegalStateException("EndlessLeveling rejected class registration: " + definition.getId());
    }
    classRegistered = true;
  }

  synchronized void shutdown() {
    if (!classRegistered) return;
    port.unregisterClass(definition.getId());
    classRegistered = false;
  }

  synchronized boolean isClassRegistered() { return classRegistered; }
}
