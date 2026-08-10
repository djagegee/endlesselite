package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class PassiveVenomService {
  private record Key(UUID owner,int target) { }
  private final EntanglementLedger entanglement;
  private final VenomLedger venom;
  private final PassiveVenomRules rules;
  private final Map<Key,Long> readyAt=new HashMap<>();

  PassiveVenomService(EntanglementLedger entanglement,VenomLedger venom,PassiveVenomRules rules){
    this.entanglement=Objects.requireNonNull(entanglement);
    this.venom=Objects.requireNonNull(venom);
    this.rules=Objects.requireNonNull(rules);
  }

  synchronized void cleanseOwner(UUID owner){
    if(owner!=null)readyAt.keySet().removeIf(key->key.owner().equals(owner));
  }

  synchronized PassiveVenomOutcome onDirectHit(PassiveVenomRequest request){
    if(request==null||request.owner()==null||request.target()<0||request.cause()==null||
        request.targetImmune()==null||request.nowMs()<0L)return PassiveVenomOutcome.rejected(PassiveVenomStatus.INVALID_REQUEST);
    if(!request.serverVerified())return PassiveVenomOutcome.rejected(PassiveVenomStatus.NOT_AUTHORIZED);
    if(!request.toxicGlandsUnlocked())return PassiveVenomOutcome.rejected(PassiveVenomStatus.NOT_UNLOCKED);
    if(!request.cause().mayApplyVenom())return PassiveVenomOutcome.rejected(PassiveVenomStatus.RECURSIVE_CAUSE);
    boolean immune;
    try { immune=request.targetImmune().getAsBoolean(); }
    catch(RuntimeException unavailableImmunity){ immune=true; }
    if(immune)return PassiveVenomOutcome.rejected(PassiveVenomStatus.IMMUNE);
    Key key=new Key(request.owner(),request.target());
    long currentReadyAt=readyAt.getOrDefault(key,0L);
    if(request.nowMs()<currentReadyAt)return PassiveVenomOutcome.rejected(PassiveVenomStatus.COOLDOWN);
    int added=rules.toxicGlandsStacks();
    if(request.huntingInstinctUnlocked()&&entanglement.stacks(request.target(),request.owner(),request.nowMs())>0)
      added+=rules.huntingInstinctBonusStacks();
    int resultingStacks=venom.apply(request.target(),request.owner(),added,rules.venomDurationMs(),request.nowMs(),()->false);
    if(resultingStacks<1)return PassiveVenomOutcome.rejected(PassiveVenomStatus.INVALID_REQUEST);
    long nextReadyAt=TimeMath.saturatedAdd(request.nowMs(),rules.internalCooldownMs());
    readyAt.put(key,nextReadyAt);
    return new PassiveVenomOutcome(PassiveVenomStatus.APPLIED,added,nextReadyAt);
  }
}
