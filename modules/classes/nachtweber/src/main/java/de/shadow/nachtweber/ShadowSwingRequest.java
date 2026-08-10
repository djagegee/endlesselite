package de.shadow.nachtweber;
import java.util.UUID;
record ShadowSwingRequest(UUID owner,boolean serverVerified,double anchorDistanceBlocks,double directionX,double directionY,double directionZ,long nowMs){}