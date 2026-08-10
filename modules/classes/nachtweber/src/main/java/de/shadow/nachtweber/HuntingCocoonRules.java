package de.shadow.nachtweber;

record HuntingCocoonRules(double rangeBlocks,int requiredEntanglementStacks,int venomStacks,long venomDurationMs,long immobilizeDurationMs,long cooldownMs) {
  HuntingCocoonRules(double rangeBlocks,int requiredEntanglementStacks,int venomStacks,long venomDurationMs,long cooldownMs) {
    this(rangeBlocks, requiredEntanglementStacks, venomStacks, venomDurationMs, 2_000L, cooldownMs);
  }
  HuntingCocoonRules {
    if(!Double.isFinite(rangeBlocks)||rangeBlocks<=0.0||rangeBlocks>64.0)throw new IllegalArgumentException("rangeBlocks must be finite in (0,64]");
    if(requiredEntanglementStacks<1||requiredEntanglementStacks>16)throw new IllegalArgumentException("requiredEntanglementStacks must be in [1,16]");
    if(venomStacks<1||venomStacks>16)throw new IllegalArgumentException("venomStacks must be in [1,16]");
    if(venomDurationMs<1L||venomDurationMs>120_000L)throw new IllegalArgumentException("venomDurationMs must be in [1,120000]");
    if(immobilizeDurationMs<1L||immobilizeDurationMs>10_000L)throw new IllegalArgumentException("immobilizeDurationMs must be in [1,10000]");
    if(cooldownMs<0L||cooldownMs>120_000L)throw new IllegalArgumentException("cooldownMs must be in [0,120000]");
  }
}
