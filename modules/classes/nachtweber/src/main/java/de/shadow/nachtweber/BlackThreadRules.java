package de.shadow.nachtweber;

record BlackThreadRules(double rangeBlocks,int stacksPerCast,long stateDurationMs,long cooldownMs,long boundControlDurationMs) {
  BlackThreadRules {
    if(!Double.isFinite(rangeBlocks)||rangeBlocks<=0.0||rangeBlocks>64.0) throw new IllegalArgumentException("rangeBlocks must be finite in (0,64]");
    if(stacksPerCast<1||stacksPerCast>16) throw new IllegalArgumentException("stacksPerCast must be in [1,16]");
    if(stateDurationMs<1||stateDurationMs>120_000) throw new IllegalArgumentException("stateDurationMs must be in [1,120000]");
    if(cooldownMs<0||cooldownMs>120_000) throw new IllegalArgumentException("cooldownMs must be in [0,120000]");
    if(boundControlDurationMs<0||boundControlDurationMs>30_000) throw new IllegalArgumentException("boundControlDurationMs must be in [0,30000]");
  }
}
