package de.shadow.seuchenweber;

import java.util.Arrays;
import java.util.UUID;

final class SeuchenweberCorePerformanceProbe {
  private static volatile long blackhole;

  private SeuchenweberCorePerformanceProbe() { }

  static Result run(int ownerCount, int targetCount, int warmupSamples, int measuredSamples) {
    if (ownerCount <= 0 || targetCount <= 0 || warmupSamples < 0 || measuredSamples <= 0) {
      throw new IllegalArgumentException("Performance probe dimensions must be positive.");
    }
    UUID[] owners = new UUID[ownerCount];
    for (int owner = 0; owner < ownerCount; owner++) {
      owners[owner] = new UUID(0L, owner + 1L);
    }
    NekrotoxinRuntimeLedger ledger = new NekrotoxinRuntimeLedger(5, 1_000L);
    for (int target = 0; target < targetCount; target++) {
      for (UUID owner : owners) {
        ledger.apply(target, owner, 5, Long.MAX_VALUE / 4L, 0L);
      }
    }

    long[] samples = new long[measuredSamples];
    long nowMs = 0L;
    int measuredTicksPerSample = -1;
    int totalSamples = warmupSamples + measuredSamples;
    for (int sample = 0; sample < totalSamples; sample++) {
      nowMs += 1_000L;
      long startedAt = System.nanoTime();
      long sink = ledger.consumeAllExpirations(nowMs).size();
      int processedTicks = 0;
      for (int target = 0; target < targetCount; target++) {
        for (NekrotoxinRuntimeLedger.DueTick tick : ledger.consumeDueTicks(target, nowMs)) {
          processedTicks++;
          sink += Float.floatToIntBits(NekrotoxinDamageSystem.damageForTick(
              2.0f, tick.stacks(), true, 1.15));
          sink += NekrotoxinDamageSystem.shouldApplyAstralEcho(
              tick.allowAstralEcho(), true, 16, 0) ? 1L : 0L;
        }
      }
      long elapsed = System.nanoTime() - startedAt;
      blackhole = sink;
      if (sample >= warmupSamples) {
        if (measuredTicksPerSample < 0) {
          measuredTicksPerSample = processedTicks;
        } else if (measuredTicksPerSample != processedTicks) {
          throw new IllegalStateException("Inconsistent measured tick workload.");
        }
        samples[sample - warmupSamples] = elapsed;
      }
    }
    Arrays.sort(samples);
    return new Result(ownerCount * targetCount, measuredTicksPerSample, measuredSamples,
        percentile(samples, 0.95), percentile(samples, 0.99));
  }

  private static long percentile(long[] sorted, double percentile) {
    int index = Math.max(0, (int) Math.ceil(sorted.length * percentile) - 1);
    return sorted[Math.min(index, sorted.length - 1)];
  }

  record Result(int ownerTargetStatesPerSample, int processedTicksPerSample,
      int sampleCount, long p95Nanos, long p99Nanos) { }
}
