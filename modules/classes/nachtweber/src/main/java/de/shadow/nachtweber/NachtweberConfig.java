package de.shadow.nachtweber;

record NachtweberConfig(double blackThreadRangeBlocks,double entanglementDurationSeconds,double venomTickIntervalSeconds,double bossControlFactor) {
  NachtweberConfig { finiteRange("blackThreadRangeBlocks",blackThreadRangeBlocks,1.0,64.0); finiteRange("entanglementDurationSeconds",entanglementDurationSeconds,0.1,120.0); finiteRange("venomTickIntervalSeconds",venomTickIntervalSeconds,0.05,30.0); finiteRange("bossControlFactor",bossControlFactor,0.0,1.0); }
  static NachtweberConfig defaults(){return new NachtweberConfig(12.0,8.0,1.0,0.35);}
  NachtweberConfig withBlackThreadRangeBlocks(double value){return new NachtweberConfig(value,entanglementDurationSeconds,venomTickIntervalSeconds,bossControlFactor);}
  NachtweberConfig withVenomTickIntervalSeconds(double value){return new NachtweberConfig(blackThreadRangeBlocks,entanglementDurationSeconds,value,bossControlFactor);}
  NachtweberConfig withBossControlFactor(double value){return new NachtweberConfig(blackThreadRangeBlocks,entanglementDurationSeconds,venomTickIntervalSeconds,value);}
  private static void finiteRange(String key,double value,double min,double max){if(!Double.isFinite(value)||value<min||value>max)throw new IllegalArgumentException(key+" must be finite in ["+min+","+max+"]");}
}
