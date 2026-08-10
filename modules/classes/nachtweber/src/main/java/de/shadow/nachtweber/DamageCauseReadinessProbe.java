package de.shadow.nachtweber;

import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class DamageCauseReadinessProbe {
  private static final String PHYSICAL_CAUSE_ID = "Physical";

  enum Status { READY, UNAVAILABLE, INCONSISTENT }

  record Result(Status status, String causeId, int assetIndex) {
    Result {
      if (status == null) throw new IllegalArgumentException("status");
    }

    boolean ready() { return status == Status.READY; }
  }

  private final Supplier<Result> reader;
  private boolean reported;

  DamageCauseReadinessProbe() {
    this(DamageCauseReadinessProbe::inspectRuntime);
  }

  DamageCauseReadinessProbe(Supplier<Result> reader) {
    this.reader = Objects.requireNonNull(reader, "reader");
  }

  synchronized boolean reportReadyOnce(Consumer<Result> observer) {
    Objects.requireNonNull(observer, "observer");
    if (reported) return false;
    Result result;
    try {
      result = reader.get();
    } catch (RuntimeException | LinkageError unavailableReader) {
      return false;
    }
    if (result == null || !result.ready()) return false;
    reported = true;
    try {
      observer.accept(result);
    } catch (RuntimeException | LinkageError unavailableObserver) {
      return false;
    }
    return true;
  }

  static Result inspectRuntime() {
    try {
      AssetStore<String, DamageCause, IndexedLookupTableAssetMap<String, DamageCause>> store =
          DamageCause.getAssetStore();
      IndexedLookupTableAssetMap<String, DamageCause> map = DamageCause.getAssetMap();
      if (store == null || map == null) {
        return unavailable();
      }
      if (store.getAssetMap() != map) {
        return new Result(Status.INCONSISTENT, null, -1);
      }
      int index = map.getIndexOrDefault(PHYSICAL_CAUSE_ID, -1);
      DamageCause physical = index < 0 ? null : map.getAsset(index);
      if (physical == null || !PHYSICAL_CAUSE_ID.equals(physical.getId())) {
        return new Result(Status.INCONSISTENT, PHYSICAL_CAUSE_ID, index);
      }
      return new Result(Status.READY, PHYSICAL_CAUSE_ID, index);
    } catch (RuntimeException | LinkageError unavailableRuntime) {
      return unavailable();
    }
  }

  private static Result unavailable() {
    return new Result(Status.UNAVAILABLE, null, -1);
  }
}
