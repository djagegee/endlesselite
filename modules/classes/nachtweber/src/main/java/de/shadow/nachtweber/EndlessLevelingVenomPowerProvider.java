package de.shadow.nachtweber;

import com.airijko.endlessleveling.api.EndlessLevelingAPI;
import com.airijko.endlessleveling.enums.SkillAttributeType;
import java.util.UUID;

final class EndlessLevelingVenomPowerProvider implements VenomPowerProvider {
  @Override public double sorcery(UUID owner){
    if(owner==null)return Double.NaN;
    try {
      double value=EndlessLevelingAPI.get().getDisplayedAttributeTotal(owner,SkillAttributeType.SORCERY,0.0);
      return Double.isFinite(value)?value:Double.NaN;
    } catch(RuntimeException | LinkageError unavailableRuntime){
      return Double.NaN;
    }
  }
}
