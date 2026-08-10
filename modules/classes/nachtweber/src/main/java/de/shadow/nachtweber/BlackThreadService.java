package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class BlackThreadService {
  enum CastStatus { APPLIED, REJECTED_NOT_SERVER, REJECTED_INVALID_TARGET, REJECTED_OUT_OF_RANGE, REJECTED_COOLDOWN }
  record CastRequest(UUID owner,int target,double distanceBlocks,ControlProfile.TargetKind targetKind,long nowMs,boolean serverVerified) { }
  record CastOutcome(CastStatus status,int stacks,boolean bound,long controlDurationMs,long nextReadyAtMs) {
    static CastOutcome rejected(CastStatus status,long nextReadyAtMs){return new CastOutcome(status,0,false,0,nextReadyAtMs);}
  }

  private final EntanglementLedger ledger;
  private final BlackThreadRules rules;
  private final ControlProfile controlProfile;
  private final Map<UUID,Long> readyAtByOwner=new HashMap<>();

  BlackThreadService(EntanglementLedger ledger,BlackThreadRules rules,ControlProfile controlProfile) {
    this.ledger=Objects.requireNonNull(ledger,"ledger"); this.rules=Objects.requireNonNull(rules,"rules"); this.controlProfile=Objects.requireNonNull(controlProfile,"controlProfile");
  }

  synchronized CastOutcome cast(CastRequest request) {
    Objects.requireNonNull(request,"request");
    if (!request.serverVerified()) return CastOutcome.rejected(CastStatus.REJECTED_NOT_SERVER, 0L);
    if (request.owner() == null || request.target() < 0 || request.targetKind() == null
        || request.nowMs() < 0L || !Double.isFinite(request.distanceBlocks()) || request.distanceBlocks() < 0.0) {
      return CastOutcome.rejected(CastStatus.REJECTED_INVALID_TARGET, 0L);
    }
    if (request.distanceBlocks() > rules.rangeBlocks()) {
      return CastOutcome.rejected(CastStatus.REJECTED_OUT_OF_RANGE, 0L);
    }
    long readyAt=readyAtByOwner.getOrDefault(request.owner(),0L);
    if (request.nowMs() < readyAt) return CastOutcome.rejected(CastStatus.REJECTED_COOLDOWN, readyAt);
    int stacks=ledger.apply(request.target(),request.owner(),rules.stacksPerCast(),rules.stateDurationMs(),request.nowMs());
    long nextReadyAt=TimeMath.saturatedAdd(request.nowMs(),rules.cooldownMs()); readyAtByOwner.put(request.owner(),nextReadyAt);
    boolean bound=ledger.isBound(request.target(),request.owner(),request.nowMs());
    long control=bound?Math.round(rules.boundControlDurationMs()*controlProfile.factor(request.targetKind())):0L;
    return new CastOutcome(CastStatus.APPLIED,stacks,bound,control,nextReadyAt);
  }

  synchronized void cleanseOwner(UUID owner) {
    if (owner != null) readyAtByOwner.remove(owner);
  }
}
