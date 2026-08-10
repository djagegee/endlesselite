package de.shadow.nachtweber;
import java.util.UUID;
@FunctionalInterface interface DangerSenseAlertPort{void alert(UUID owner,int target,double distanceBlocks);}