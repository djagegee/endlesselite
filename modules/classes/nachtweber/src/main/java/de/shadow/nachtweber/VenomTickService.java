package de.shadow.nachtweber;

import java.util.Objects;
import java.util.UUID;

final class VenomTickService {
  enum TickStatus { EMITTED, NOT_DUE, INVALID_INPUT, DAMAGE_REJECTED }
  record TickOutcome(TickStatus status,int stacks,float damage,long nextTickAtMs) {
    static TickOutcome empty(TickStatus status){return new TickOutcome(status,0,0.0f,0L);}
  }
  private final VenomLedger ledger;
  private final VenomTickRules rules;
  private final VenomPowerProvider power;
  VenomTickService(VenomLedger ledger,VenomTickRules rules,VenomPowerProvider power){
    this.ledger=Objects.requireNonNull(ledger);this.rules=Objects.requireNonNull(rules);this.power=Objects.requireNonNull(power);
  }
  synchronized TickOutcome tick(int target,UUID owner,long nowMs,VenomDamagePort port){
    if(target<0||owner==null||nowMs<0L||port==null)return TickOutcome.empty(TickStatus.INVALID_INPUT);
    double sorcery=power.sorcery(owner);
    if(!Double.isFinite(sorcery))return TickOutcome.empty(TickStatus.INVALID_INPUT);
    VenomLedger.DueTick due=ledger.consumeOneDueTick(target,owner,nowMs);
    if(due.isEmpty())return TickOutcome.empty(TickStatus.NOT_DUE);
    double multiplier=1.0+Math.max(0.0,sorcery)/rules.sorceryPointsForDoubleDamage();
    double computed=rules.baseDamagePerStack()*due.stacks()*multiplier;
    float damage=(float)Math.min(rules.maximumTickDamage(),computed);
    boolean emitted;
    try {
      emitted=port.emit(owner,target,damage,VenomDamageCause.VENOM_TICK);
    } catch(RuntimeException runtimeFailure) {
      emitted=false;
    }
    return new TickOutcome(emitted?TickStatus.EMITTED:TickStatus.DAMAGE_REJECTED,due.stacks(),damage,due.nextTickAtMs());
  }
}
