package de.shadow.nachtweber;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

interface NachtweberGameplayRuntime {
  Optional<BlackThreadService.CastOutcome> castBlackThread(
      BlackThreadService.CastRequest request);

  Optional<HuntingCocoonService.CastOutcome> castHuntingCocoon(
      HuntingCocoonService.CastRequest request);

  Optional<PassiveVenomOutcome> applyPassiveVenom(PassiveVenomRequest request);

  Optional<VenomTickService.TickOutcome> tickVenom(
      int target, UUID owner, long nowMs, VenomDamagePort port);

  List<UUID> activeVenomOwners(int target, long nowMs);

  Optional<ShadowSwingReconciliationController.StartOutcome> startShadowSwing(
      ShadowSwingReconciliationController.StartRequest request);

  Optional<ShadowSwingReconciliationController.StepOutcome> stepShadowSwing(
      ShadowSwingReconciliationController.StepRequest request);

  boolean cancelShadowSwing(UUID owner);

  boolean cleanupOwner(UUID owner);
}
