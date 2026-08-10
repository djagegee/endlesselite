package de.shadow.nachtweber;

import com.ziggfreed.mmoskilltree.ability.ParamSpec;

final class HuntingCocoonAbilityContracts {
  static final String EFFECT_ID="NACHTWEBER_HUNTING_COCOON";
  private static final ParamSpec PARAMS=ParamSpec.of(
      required("range",8.0,"Maximum server-verified hostile-target range in blocks"),
      required("requiredEntanglementStacks",3.0,"Owner entanglement required and consumed"),
      required("venomStacks",2.0,"Owner-isolated venom stacks applied after consumption"),
      required("venomDurationMs",8000.0,"Server-authoritative venom lifetime"));
  private HuntingCocoonAbilityContracts() { }
  static ParamSpec paramSpec(){return PARAMS;}
  private static ParamSpec.ParamEntry required(String key,double value,String description){return ParamSpec.ParamEntry.required(key,ParamSpec.ParamType.NUMBER,value,description);}
}
