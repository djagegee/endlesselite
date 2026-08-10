package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeuchenweberCorePerformanceProbeTest {
  @Test void eightOwnersAcrossSixteenTargetsStayWithinCoreTickBudget() {
    SeuchenweberCorePerformanceProbe.Result result =
        SeuchenweberCorePerformanceProbe.run(8, 16, 500, 2_000);

    assertEquals(128, result.ownerTargetStatesPerSample());
    assertEquals(128, result.processedTicksPerSample());
    assertEquals(2_000, result.sampleCount());
    System.out.printf("SEUCHENWEBER_CORE_PERF states=%d samples=%d p95_ms=%.6f p99_ms=%.6f%n",
        result.ownerTargetStatesPerSample(), result.sampleCount(),
        result.p95Nanos() / 1_000_000.0, result.p99Nanos() / 1_000_000.0);
    assertTrue(result.p95Nanos() <= 2_500_000L,
        () -> "Core p95 exceeded 2.5 ms: " + result.p95Nanos() + " ns");
    assertTrue(result.p99Nanos() <= 5_000_000L,
        () -> "Core p99 exceeded 5.0 ms: " + result.p99Nanos() + " ns");
  }
}
