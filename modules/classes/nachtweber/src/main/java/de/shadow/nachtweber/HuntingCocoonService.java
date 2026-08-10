package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;

final class HuntingCocoonService {
  enum CastStatus { APPLIED, REJECTED_NOT_SERVER, REJECTED_INVALID_TARGET, REJECTED_OUT_OF_RANGE, REJECTED_COOLDOWN, REJECTED_RECURSIVE_CAUSE, REJECTED_IMMUNE, REJECTED_INSUFFICIENT_ENTANGLEMENT }
  record CastRequest(UUID owner,int target,double distanceBlocks,long nowMs,boolean serverVerified,VenomDamageCause cause,BooleanSupplier immune) { }
  record CastOutcome(CastStatus status,int consumedEntanglementStacks,int venomStacks,long immobilizeDurationMs,long nextReadyAtMs) {
    static CastOutcome rejected(CastStatus status,long readyAt){return new CastOutcome(status,0,0,0,readyAt);}
  }
  private final EntanglementLedger entanglement;
  private final VenomLedger venom;
  private final HuntingCocoonRules rules;
  private final Map<UUID,Long> readyAtByOwner=new HashMap<>();
  HuntingCocoonService(EntanglementLedger entanglement,VenomLedger venom,HuntingCocoonRules rules){this.entanglement=Objects.requireNonNull(entanglement);this.venom=Objects.requireNonNull(venom);this.rules=Objects.requireNonNull(rules);}
  synchronized CastOutcome cast(CastRequest request){
    Objects.requireNonNull(request,"request");
    if(!request.serverVerified())return CastOutcome.rejected(CastStatus.REJECTED_NOT_SERVER,0L);
    if(request.owner()==null||request.target()<0||request.nowMs()<0L||request.cause()==null||request.immune()==null
        ||!Double.isFinite(request.distanceBlocks())||request.distanceBlocks()<0.0){
      return CastOutcome.rejected(CastStatus.REJECTED_INVALID_TARGET,0L);
    }
    if(request.distanceBlocks()>rules.rangeBlocks())return CastOutcome.rejected(CastStatus.REJECTED_OUT_OF_RANGE,0L);
    if(!request.cause().mayApplyVenom())return CastOutcome.rejected(CastStatus.REJECTED_RECURSIVE_CAUSE,0L);
    long readyAt=readyAtByOwner.getOrDefault(request.owner(),0L);
    if(request.nowMs()<readyAt)return CastOutcome.rejected(CastStatus.REJECTED_COOLDOWN,readyAt);
    boolean immune=request.immune().getAsBoolean();
    if(immune)return CastOutcome.rejected(CastStatus.REJECTED_IMMUNE,0L);
    int before=entanglement.stacks(request.target(),request.owner(),request.nowMs());
    if(before<rules.requiredEntanglementStacks())return CastOutcome.rejected(CastStatus.REJECTED_INSUFFICIENT_ENTANGLEMENT,0L);
    int consumed=entanglement.consumeAllIfAtLeast(request.target(),request.owner(),rules.requiredEntanglementStacks(),request.nowMs());
    int venomStacks=venom.apply(request.target(),request.owner(),rules.venomStacks(),rules.venomDurationMs(),request.nowMs(),()->false);
    long next=TimeMath.saturatedAdd(request.nowMs(),rules.cooldownMs());readyAtByOwner.put(request.owner(),next);
    return new CastOutcome(CastStatus.APPLIED,consumed,venomStacks,rules.immobilizeDurationMs(),next);
  }

  synchronized void cleanseOwner(UUID owner) {
    if (owner != null) readyAtByOwner.remove(owner);
  }
}
