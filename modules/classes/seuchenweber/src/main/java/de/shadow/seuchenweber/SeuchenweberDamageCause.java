package de.shadow.seuchenweber;

import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;

/** Resolves the Poison cause that is present in the installed Hytale Assets.zip. */
final class SeuchenweberDamageCause {
  static final String ID = "Poison";
  private static volatile DamageCause cached;

  private SeuchenweberDamageCause() { }

  static DamageCause requireAvailable() {
    DamageCause current = cached;
    if (current != null) return current;
    synchronized (SeuchenweberDamageCause.class) {
      current = cached;
      if (current == null) {
        current = DamageCause.getAssetMap().getAsset(ID);
        if (current == null) throw new IllegalStateException("Required Hytale damage cause is unavailable: " + ID);
        cached = current;
      }
      return current;
    }
  }

  static int requireIndex() {
    requireAvailable();
    int index = DamageCause.getAssetMap().getIndex(ID);
    if (index == Integer.MIN_VALUE) {
      throw new IllegalStateException("Required Hytale damage cause has no asset index: " + ID);
    }
    return index;
  }
}
