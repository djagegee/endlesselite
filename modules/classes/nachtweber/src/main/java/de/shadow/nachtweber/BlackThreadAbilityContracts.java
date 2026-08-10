package de.shadow.nachtweber;

import com.ziggfreed.mmoskilltree.ability.ParamSpec;

final class BlackThreadAbilityContracts {
  static final String EFFECT_ID="NACHTWEBER_BLACK_THREAD";
  private static final ParamSpec PARAMS=ParamSpec.of(
      required("range",12.0,"Maximum server-verified hostile-target range in blocks"),
      required("stacks",2.0,"Owner-isolated entanglement stacks per accepted cast"),
      required("stateDurationMs",8000.0,"Server-authoritative entanglement lifetime"),
      required("boundControlDurationMs",1200.0,"Base bind control before target reduction"));
  private BlackThreadAbilityContracts() { }
  static ParamSpec paramSpec(){return PARAMS;}
  private static ParamSpec.ParamEntry required(String key,double value,String description){return ParamSpec.ParamEntry.required(key,ParamSpec.ParamType.NUMBER,value,description);}
}
