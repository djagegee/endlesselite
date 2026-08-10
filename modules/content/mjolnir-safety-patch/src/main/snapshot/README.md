# Mjolnir Safety Patch

This is a Patchly asset patch for Starky's Mjolnir 1.6.1.

It removes only the `Mjolnir_Held_Passive_Pulse` branch from the item asset. The
original Mjolnir JAR, its active abilities, and its charging-spin loop remain
untouched. The patch exists because the server log shows the held passive
throwing `Entity ref was null or invalid during chain tick` when a player enters
the world.

Remove this folder if a future Mjolnir update fixes the held-passive crash.
