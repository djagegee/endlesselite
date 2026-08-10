package de.shadow.nachtweber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class DangerSenseService {
  private final DangerSenseRules rules;
  private final Map<UUID,Long> readyAt=new HashMap<>();

  DangerSenseService(DangerSenseRules rules){this.rules=Objects.requireNonNull(rules);}

  synchronized DangerSenseOutcome preflight(UUID owner,boolean serverVerified,boolean unlocked,long nowMs){
    if(owner==null||nowMs<0)return DangerSenseOutcome.of(DangerSenseStatus.INVALID_REQUEST);
    if(!serverVerified)return DangerSenseOutcome.of(DangerSenseStatus.NOT_AUTHORIZED);
    if(!unlocked)return DangerSenseOutcome.of(DangerSenseStatus.NOT_UNLOCKED);
    if(nowMs<readyAt.getOrDefault(owner,0L))return DangerSenseOutcome.of(DangerSenseStatus.COOLDOWN);
    return DangerSenseOutcome.of(DangerSenseStatus.QUIET);
  }

  synchronized DangerSenseOutcome scan(UUID owner,boolean serverVerified,boolean unlocked,long nowMs,List<DangerCandidate> candidates){
    if(candidates==null)return DangerSenseOutcome.of(DangerSenseStatus.INVALID_REQUEST);
    DangerSenseOutcome gate=preflight(owner,serverVerified,unlocked,nowMs);
    if(gate.status()!=DangerSenseStatus.QUIET)return gate;
    DangerCandidate nearest=null;
    int limit=Math.min(rules.maximumCandidates(),candidates.size());
    for(int i=0;i<limit;i++){
      DangerCandidate candidate=candidates.get(i);
      if(candidate==null||candidate.target()<0||!candidate.hostile()||!candidate.visible()||!candidate.sameStore()||
          !Double.isFinite(candidate.distanceBlocks())||candidate.distanceBlocks()<0||candidate.distanceBlocks()>rules.radiusBlocks())continue;
      if(nearest==null||candidate.distanceBlocks()<nearest.distanceBlocks())nearest=candidate;
    }
    if(nearest==null)return DangerSenseOutcome.of(DangerSenseStatus.QUIET);
    long next=TimeMath.saturatedAdd(nowMs,rules.warningCooldownMs());
    readyAt.put(owner,next);
    return new DangerSenseOutcome(DangerSenseStatus.ALERT,nearest.target(),nearest.distanceBlocks(),next);
  }

  synchronized void cleanseOwner(UUID owner){if(owner!=null)readyAt.remove(owner);}
}
