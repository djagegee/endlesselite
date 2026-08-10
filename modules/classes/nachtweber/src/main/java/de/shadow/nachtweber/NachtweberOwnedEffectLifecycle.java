package de.shadow.nachtweber;

import com.ziggfreed.mmoskilltree.ability.AbilityEffect;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Owns only the exact MMOSkillTree effect instances successfully installed by Nachtweber. */
final class NachtweberOwnedEffectLifecycle {
  interface Registry {
    boolean registerIfAbsent(String discriminator, AbilityEffect effect);
    boolean unregister(String discriminator, AbilityEffect expectedEffect);
  }

  private final Registry registry;
  private final Map<String, AbilityEffect> requested;
  private final Map<String, AbilityEffect> owned = new LinkedHashMap<>();
  private boolean started;

  NachtweberOwnedEffectLifecycle(Registry registry, Map<String, AbilityEffect> requested) {
    this.registry = Objects.requireNonNull(registry, "registry");
    Objects.requireNonNull(requested, "requested");
    this.requested = new LinkedHashMap<>();
    requested.forEach((id, effect) -> this.requested.put(
        Objects.requireNonNull(id, "effect discriminator"),
        Objects.requireNonNull(effect, "effect")));
  }

  synchronized boolean start() {
    if (started || !owned.isEmpty() || requested.isEmpty()) return false;
    List<Map.Entry<String, AbilityEffect>> installed = new ArrayList<>();
    for (Map.Entry<String, AbilityEffect> entry : requested.entrySet()) {
      if (!registry.registerIfAbsent(entry.getKey(), entry.getValue())) {
        rollback(installed);
        return false;
      }
      installed.add(entry);
    }
    installed.forEach(entry -> owned.put(entry.getKey(), entry.getValue()));
    started = true;
    return true;
  }

  synchronized void shutdown() {
    if (!started && owned.isEmpty()) return;
    List<Map.Entry<String, AbilityEffect>> entries = new ArrayList<>(owned.entrySet());
    for (int index = entries.size() - 1; index >= 0; index--) {
      Map.Entry<String, AbilityEffect> entry = entries.get(index);
      registry.unregister(entry.getKey(), entry.getValue());
    }
    owned.clear();
    started = false;
  }

  synchronized int ownedCount() {
    return owned.size();
  }

  private void rollback(List<Map.Entry<String, AbilityEffect>> installed) {
    for (int index = installed.size() - 1; index >= 0; index--) {
      Map.Entry<String, AbilityEffect> entry = installed.get(index);
      registry.unregister(entry.getKey(), entry.getValue());
    }
  }
}
