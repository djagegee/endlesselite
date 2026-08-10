package de.shadow.nachtweber;

record DangerSenseRules(double radiusBlocks,long warningCooldownMs,int maximumCandidates){
  DangerSenseRules{
    if(!Double.isFinite(radiusBlocks)||radiusBlocks<=0||radiusBlocks>16.0||warningCooldownMs<1||maximumCandidates<1||maximumCandidates>64)
      throw new IllegalArgumentException("invalid danger sense rules");
  }
}
