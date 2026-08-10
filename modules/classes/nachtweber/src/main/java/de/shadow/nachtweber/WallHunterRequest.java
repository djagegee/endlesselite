package de.shadow.nachtweber;
import java.util.UUID;
record WallHunterRequest(UUID owner,boolean serverVerified,boolean unlocked,boolean horizontalWallContact,boolean upwardInput){}