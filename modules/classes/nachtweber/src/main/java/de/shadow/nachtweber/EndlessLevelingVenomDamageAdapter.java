package de.shadow.nachtweber;

import com.airijko.endlessleveling.systems.PlayerCombatSystem;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import java.util.UUID;

final class EndlessLevelingVenomDamageAdapter implements VenomDamagePort {
  private static final String POISON_CAUSE="Poison";
  private final UUID expectedOwner;
  private final int expectedTarget;
  private final Ref<EntityStore> ownerRef;
  private final Ref<EntityStore> targetRef;
  private final CommandBuffer<EntityStore> commandBuffer;

  EndlessLevelingVenomDamageAdapter(UUID expectedOwner,int expectedTarget,Ref<EntityStore> ownerRef,
      Ref<EntityStore> targetRef,CommandBuffer<EntityStore> commandBuffer){
    this.expectedOwner=Objects.requireNonNull(expectedOwner);
    this.expectedTarget=expectedTarget;
    this.ownerRef=Objects.requireNonNull(ownerRef);
    this.targetRef=Objects.requireNonNull(targetRef);
    this.commandBuffer=Objects.requireNonNull(commandBuffer);
  }

  @Override public boolean emit(UUID owner,int target,float damage,VenomDamageCause cause){
    if(!expectedOwner.equals(owner)||target!=expectedTarget||cause!=VenomDamageCause.VENOM_TICK||
        !Float.isFinite(damage)||damage<=0.0f)return false;
    try {
      if(!ownerRef.isValid()||!targetRef.isValid()||targetRef.getIndex()!=target||
          ownerRef.getStore()!=commandBuffer.getStore()||targetRef.getStore()!=commandBuffer.getStore())return false;
      Damage event=createVerifiedDamage(ownerRef,damage);
      if(event==null)return false;
      DamageSystems.executeDamage(targetRef,commandBuffer,event);
      return true;
    } catch(RuntimeException | LinkageError invalidRuntimeState){
      return false;
    }
  }

  static Damage createVerifiedDamage(Ref<EntityStore> ownerRef,float damage){
    if(ownerRef==null||!Float.isFinite(damage)||damage<=0.0f)return null;
    try {
      Damage event=PlayerCombatSystem.createAbilityDotDamage(ownerRef,damage,POISON_CAUSE);
      if(event==null||!(event.getSource() instanceof Damage.EntitySource source)||source.getRef()!=ownerRef||
          !PlayerCombatSystem.isAugmentDotDamage(event)||
          !PlayerCombatSystem.isAbilityOriginProcDamage(event)||
          !PlayerCombatSystem.shouldBypassOutgoingAugmentMath(event))return null;
      return event;
    } catch(RuntimeException | LinkageError unavailableContract){
      return null;
    }
  }
}
