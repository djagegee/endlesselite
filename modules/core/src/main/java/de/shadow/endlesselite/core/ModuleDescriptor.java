package de.shadow.endlesselite.core;

import java.util.Objects;
import java.util.Set;

public record ModuleDescriptor(
    String id,
    String displayName,
    String version,
    Set<String> capabilities,
    boolean runtimePlugin) {

  public ModuleDescriptor {
    id = requireText(id, "id");
    displayName = requireText(displayName, "displayName");
    version = requireText(version, "version");
    capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
  }

  private static String requireText(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
    return value;
  }
}
