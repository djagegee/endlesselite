package de.shadow.nachtweber;
import com.airijko.endlessleveling.systems.PlayerCombatSystem;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.ziggfreed.mmoskilltree.ability.CasterContext;
import java.util.UUID;
final class PassiveVenomDamageAdapter{
 private PassiveVenomDamageAdapter(){}
 static PassiveVenomOutcome onVerifiedDirectHit(Store<EntityStore> store,Ref<EntityStore> target,Damage damage,long nowMs,boolean toxicUnlocked,boolean huntingUnlocked,VenomImmunityResolver immunity,PassiveVenomService service){
  if(store==null||target==null||!target.isValid()||target.getStore()!=store||damage==null||immunity==null||service==null)return PassiveVenomOutcome.rejected(PassiveVenomStatus.INVALID_REQUEST);
  if(PlayerCombatSystem.isAugmentDotDamage(damage)||PlayerCombatSystem.isAugmentProcDamage(damage)||PlayerCombatSystem.isAbilityOriginProcDamage(damage))return PassiveVenomOutcome.rejected(PassiveVenomStatus.RECURSIVE_CAUSE);
  if(!(damage.getSource() instanceof Damage.EntitySource source)||source.getRef()==null||!source.getRef().isValid()||source.getRef().getStore()!=store)return PassiveVenomOutcome.rejected(PassiveVenomStatus.NOT_AUTHORIZED);
  UUID owner=CasterContext.entityUuid(store,source.getRef());if(owner==null)return PassiveVenomOutcome.rejected(PassiveVenomStatus.NOT_AUTHORIZED);
  return service.onDirectHit(new PassiveVenomRequest(owner,target.getIndex(),true,toxicUnlocked,huntingUnlocked,VenomDamageCause.DIRECT_HIT,nowMs,()->immunity.isImmune(store,target)));
 }
}