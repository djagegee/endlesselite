/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.airijko.endlessguilds.guild.claim.ClaimCell
 *  com.airijko.endlessguilds.guild.claim.ClaimService
 *  com.airijko.endlessguilds.guild.claim.ClaimService$ClaimResult
 *  com.airijko.endlessguilds.guild.claim.ClaimService$UnclaimResult
 *  com.airijko.endlessguilds.guild.claim.GuildClaims
 *  com.airijko.endlessguilds.ui.MapTerrainStreamer
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.KeyedCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.builder.BuilderCodec$Builder
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
 *  com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage
 *  com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
 *  com.hypixel.hytale.server.core.ui.builder.EventData
 *  com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
 *  com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.joml.Vector3d
 */
package de.shadow.endlessbook;

import com.airijko.endlessguilds.guild.claim.ClaimCell;
import com.airijko.endlessguilds.guild.claim.ClaimService;
import com.airijko.endlessguilds.guild.claim.GuildClaims;
import com.airijko.endlessguilds.guild.GuildService;
import com.airijko.endlessguilds.ui.MapTerrainStreamer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import java.util.UUID;
import org.joml.Vector3d;

public final class PersonalClaimsPage
extends InteractiveCustomUIPage<PersonalClaimsPage.ActionData> {
    private static final int GRID_SIZE = PersonalClaimsContract.GRID_SIZE;
    private static final int GRID_RADIUS = PersonalClaimsContract.GRID_RADIUS;
    private static final BuilderCodec<ActionData> ACTION_CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ActionData.class, ActionData::new).append(new KeyedCodec("Action", (Codec)Codec.STRING), (data, action) -> {
        data.action = action;
    }, data -> data.action).add()).build();
    private int centerX;
    private int centerZ;
    private int selectedX;
    private int selectedZ;
    private String selectedWorld;
    private boolean centered;
    private String status = "";

    public PersonalClaimsPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, ACTION_CODEC);
    }

    public void build(Ref<EntityStore> playerEntityRef, UICommandBuilder commands, UIEventBuilder events, Store<EntityStore> store) {
        boolean selectedIsFree;
        UUID playerId;
        ClaimService claims;
        GuildClaims ownClaims;
        commands.append("EndlessBook/PersonalClaimsPage.ui");
        Player player = (Player)store.getComponent(playerEntityRef, Player.getComponentType());
        World world = ((EntityStore)store.getExternalData()).getWorld();
        if (player == null || world == null) {
            return;
        }
        UiLanguage language = PersonalClaimsPage.language(player.getPlayerRef());
        ClaimCell playerCell = PersonalClaimsPage.currentCell(playerEntityRef, store, world);
        if (!this.centered && playerCell != null) {
            this.centerX = this.selectedX = playerCell.x();
            this.centerZ = this.selectedZ = playerCell.z();
            this.selectedWorld = playerCell.worldName();
            this.centered = true;
        }
        if (this.selectedWorld == null) {
            this.selectedWorld = world.getName();
        }
        int used = (ownClaims = (claims = ClaimService.get()).claimsOf(playerId = this.playerRef.getUuid())) == null ? 0 : ownClaims.cellCount();
        int cap = ClaimService.personalCap();
        PersonalClaimsPage.setText(commands, "#PageTitle.Text", PersonalClaimsPage.text(language, "page_title"));
        PersonalClaimsPage.setText(commands, "#PageSubtitle.Text", PersonalClaimsPage.text(language, "page_subtitle"));
        PersonalClaimsPage.setText(commands, "#OwnerName.Text", this.playerRef.getUsername());
        PersonalClaimsPage.setText(commands, "#OwnerRole.Text", PersonalClaimsPage.text(language, "owner_role"));
        PersonalClaimsPage.setText(commands, "#InfoLandLabel.Text", PersonalClaimsPage.text(language, "land_label"));
        PersonalClaimsPage.setText(commands, "#InfoLimitLabel.Text", PersonalClaimsPage.text(language, "limit_label"));
        PersonalClaimsPage.setText(commands, "#InfoWorldLabel.Text", PersonalClaimsPage.text(language, "world_label"));
        PersonalClaimsPage.setText(commands, "#InfoLandValue.Text", used + " / " + cap);
        PersonalClaimsPage.setText(commands, "#InfoLimitValue.Text", Integer.toString(cap));
        PersonalClaimsPage.setText(commands, "#InfoWorldValue.Text", world.getName());
        PersonalClaimsPage.setText(commands, "#InfoHint.Text", PersonalClaimsPage.text(language, "info_hint"));
        PersonalClaimsPage.setText(commands, "#ClaimsTitleLine.Text", PersonalClaimsPage.text(language, "map_title"));
        PersonalClaimsPage.setText(commands, "#ClaimsHintLine.Text", PersonalClaimsPage.text(language, "map_hint"));
        PersonalClaimsPage.setText(commands, "#PanWestButton.Text", "< W");
        PersonalClaimsPage.setText(commands, "#PanNorthButton.Text", "^ N");
        PersonalClaimsPage.setText(commands, "#PanSouthButton.Text", "v S");
        PersonalClaimsPage.setText(commands, "#PanEastButton.Text", "E >");
        PersonalClaimsPage.setText(commands, "#RecenterButton.Text", PersonalClaimsPage.text(language, "center"));
        PersonalClaimsPage.setText(commands, "#LegendOwn.Text", PersonalClaimsPage.text(language, "legend_own"));
        PersonalClaimsPage.setText(commands, "#LegendAvailable.Text", PersonalClaimsPage.text(language, "legend_available"));
        PersonalClaimsPage.setText(commands, "#LegendOther.Text", PersonalClaimsPage.text(language, "legend_other"));
        PersonalClaimsPage.setText(commands, "#LegendSelected.Text", PersonalClaimsPage.text(language, "legend_selected"));
        PersonalClaimsPage.setText(commands, "#CloseButton.Text", PersonalClaimsPage.text(language, "close"));
        PersonalClaimsPage.setText(commands, "#SelectionTitle.Text", PersonalClaimsPage.text(language, "selection_title"));
        UUID selectedOwner = claims.ownerOfCell(this.selectedWorld, ClaimCell.index((int)this.selectedX, (int)this.selectedZ));
        boolean selectedIsOwn = playerId.equals(selectedOwner);
        boolean bl = selectedIsFree = selectedOwner == null;
        String selectedState = selectedIsOwn ? PersonalClaimsPage.text(language, "selected_owned") : (selectedIsFree ? PersonalClaimsPage.text(language, "selected_available") : PersonalClaimsPage.text(language, "selected_other"));
        PersonalClaimsPage.setText(commands, "#SelectionChunk.Text", String.format(Locale.ROOT, "%s  (%d, %d)", this.selectedWorld, this.selectedX, this.selectedZ));
        PersonalClaimsPage.setText(commands, "#SelectionState.Text", selectedState);
        PersonalClaimsPage.setText(commands, "#ClaimButton.Text", PersonalClaimsPage.text(language, "claim"));
        PersonalClaimsPage.setText(commands, "#ReleaseButton.Text", PersonalClaimsPage.text(language, "release"));
        PersonalClaimsPage.setText(commands, "#StatusLine.Text", this.status.isBlank() ? PersonalClaimsPage.text(language, "status_hint") : this.status);
        commands.set("#ClaimButton.Disabled", !selectedIsFree);
        commands.set("#ReleaseButton.Disabled", !selectedIsOwn);
        PersonalClaimsPage.bind(events, "#CloseButton", "close");
        PersonalClaimsPage.bind(events, "#PanWestButton", "pan:w");
        PersonalClaimsPage.bind(events, "#PanNorthButton", "pan:n");
        PersonalClaimsPage.bind(events, "#PanSouthButton", "pan:s");
        PersonalClaimsPage.bind(events, "#PanEastButton", "pan:e");
        PersonalClaimsPage.bind(events, "#RecenterButton", "recenter");
        PersonalClaimsPage.bind(events, "#ClaimButton", "claim");
        PersonalClaimsPage.bind(events, "#ReleaseButton", "release");
        this.buildGrid(commands, events, claims, playerId, world.getName(), playerCell);
        MapTerrainStreamer.warmTerrainAndRefresh((World)world, (int)this.centerX, (int)this.centerZ, (int)9, (int)4, (terrainCommands, complete) -> this.sendUpdate((UICommandBuilder)terrainCommands, (boolean)complete));
    }

    public void handleDataEvent(Ref<EntityStore> playerEntityRef, Store<EntityStore> store, ActionData data) {
        if (data == null || data.action == null) {
            return;
        }
        Player player = (Player)store.getComponent(playerEntityRef, Player.getComponentType());
        World world = ((EntityStore)store.getExternalData()).getWorld();
        if (player == null || world == null) {
            return;
        }
        UiLanguage language = PersonalClaimsPage.language(player.getPlayerRef());
        if (data.action.equals("close")) {
            this.close();
            return;
        }
        if (data.action.startsWith("pan:")) {
            switch (data.action.substring(4)) {
                case "w": {
                    --this.centerX;
                    break;
                }
                case "e": {
                    ++this.centerX;
                    break;
                }
                case "n": {
                    --this.centerZ;
                    break;
                }
                case "s": {
                    ++this.centerZ;
                    break;
                }
            }
            this.status = "";
            this.rebuild();
            return;
        }
        if (data.action.equals("recenter")) {
            ClaimCell cell = PersonalClaimsPage.currentCell(playerEntityRef, store, world);
            if (cell != null) {
                this.centerX = this.selectedX = cell.x();
                this.centerZ = this.selectedZ = cell.z();
                this.selectedWorld = cell.worldName();
            }
            this.status = "";
            this.rebuild();
            return;
        }
        if (data.action.startsWith("cell:")) {
            String[] parts = data.action.split(":", 3);
            if (parts.length == 3) {
                try {
                    this.selectedX = Integer.parseInt(parts[1]);
                    this.selectedZ = Integer.parseInt(parts[2]);
                    this.selectedWorld = world.getName();
                    this.status = "";
                }
                catch (NumberFormatException ignored) {
                    this.status = PersonalClaimsPage.text(language, "invalid_selection");
                }
            }
            this.rebuild();
            return;
        }
        if (data.action.equals("claim")) {
            if (!PersonalClaimsAccess.mayCreateClaim(this.playerRef.getUuid(), id -> GuildService.get().isInGuild(id))) {
                this.status = PersonalClaimsPage.text(language, "claim_guild_member");
                this.playerRef.sendMessage(Message.raw((String)this.status).color("#ff8b80"));
                this.rebuild();
                return;
            }
            ClaimService claimService = ClaimService.get();
            UUID owner = claimService.ownerOfCell(this.selectedWorld, ClaimCell.index((int)this.selectedX, (int)this.selectedZ));
            if (owner != null) {
                this.status = this.playerRef.getUuid().equals(owner) ? PersonalClaimsPage.text(language, "claim_already") : PersonalClaimsPage.text(language, "claim_other");
                this.playerRef.sendMessage(Message.raw((String)this.status).color("#ffd58a"));
                this.rebuild();
                return;
            }
            ClaimService.ClaimResult result = claimService.claimPersonal(this.playerRef.getUuid(), this.playerRef.getUsername(), this.selectedWorld, ChunkUtil.minBlock((int)this.selectedX), ChunkUtil.minBlock((int)this.selectedZ));
            this.status = PersonalClaimsPage.claimResultText(language, result);
            this.playerRef.sendMessage(Message.raw((String)this.status).color(result == ClaimService.ClaimResult.OK ? "#88dd88" : "#ff8b80"));
            this.rebuild();
            return;
        }
        if (data.action.equals("release")) {
            ClaimService.UnclaimResult result = ClaimService.get().unclaim(this.playerRef.getUuid(), this.selectedWorld, ChunkUtil.minBlock((int)this.selectedX), ChunkUtil.minBlock((int)this.selectedZ));
            this.status = PersonalClaimsPage.unclaimResultText(language, result);
            this.playerRef.sendMessage(Message.raw((String)this.status).color(result == ClaimService.UnclaimResult.OK ? "#ffd58a" : "#ff8b80"));
            this.rebuild();
        }
    }

    private void buildGrid(UICommandBuilder commands, UIEventBuilder events, ClaimService claims, UUID playerId, String worldName, ClaimCell playerCell) {
        for (int row = 0; row < 9; ++row) {
            String rowSelector = "#GridRow" + row;
            for (int col = 0; col < 9; ++col) {
                commands.append(rowSelector, PersonalClaimsContract.CELL_RESOURCE);
                String selector = rowSelector + "[" + col + "]";
                int x = this.centerX + col - 4;
                int z = this.centerZ + row - 4;
                long index = ClaimCell.index((int)x, (int)z);
                UUID owner = claims.ownerOfCell(worldName, index);
                boolean own = playerId.equals(owner);
                boolean selected = this.selectedWorld.equals(worldName) && x == this.selectedX && z == this.selectedZ;
                boolean here = playerCell != null && playerCell.x() == x && playerCell.z() == z;
                commands.set(selector + " #StateOwn.Visible", own);
                commands.set(selector + " #StateOther.Visible", owner != null && !own);
                commands.set(selector + " " + PersonalClaimsContract.AVAILABLE_SELECTOR + ".Visible", owner == null);
                commands.set(selector + " #PlayerHere.Visible", here);
                commands.set(selector + " " + PersonalClaimsContract.SELECTED_SELECTOR + ".Visible", selected);
                PersonalClaimsPage.bind(events, selector + " " + PersonalClaimsContract.BUTTON_SELECTOR, "cell:" + x + ":" + z);
            }
        }
    }

    private static ClaimCell currentCell(Ref<EntityStore> playerEntityRef, Store<EntityStore> store, World world) {
        TransformComponent transform = (TransformComponent)store.getComponent(playerEntityRef, TransformComponent.getComponentType());
        if (transform == null || transform.getPosition() == null) {
            return null;
        }
        Vector3d position = transform.getPosition();
        return ClaimCell.fromBlock((String)world.getName(), (int)((int)Math.floor(position.x)), (int)((int)Math.floor(position.z)));
    }

    private static void bind(UIEventBuilder events, String selector, String action) {
        events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of((String)"Action", (String)action), false);
    }

    private static void setText(UICommandBuilder commands, String selector, String value) {
        commands.set(selector, value);
    }

    private static UiLanguage language(PlayerRef player) {
        String code = player == null ? null : player.getLanguage();
        return code != null && code.toLowerCase(Locale.ROOT).startsWith("de") ? UiLanguage.GERMAN : UiLanguage.ENGLISH;
    }

    private static String claimResultText(UiLanguage language, ClaimService.ClaimResult result) {
        return switch (result) {
            case ClaimService.ClaimResult.OK -> PersonalClaimsPage.text(language, "claim_ok");
            case ClaimService.ClaimResult.CAP_REACHED -> PersonalClaimsPage.text(language, "claim_cap");
            case ClaimService.ClaimResult.NOT_CONTIGUOUS -> PersonalClaimsPage.text(language, "claim_contiguous");
            case ClaimService.ClaimResult.ALREADY_OWNED -> PersonalClaimsPage.text(language, "claim_already");
            case ClaimService.ClaimResult.OWNED_BY_OTHER -> PersonalClaimsPage.text(language, "claim_other");
            case ClaimService.ClaimResult.NOT_CLAIMABLE_WORLD, ClaimService.ClaimResult.WRONG_WORLD, ClaimService.ClaimResult.DISABLED -> PersonalClaimsPage.text(language, "claim_world");
            default -> PersonalClaimsPage.text(language, "claim_failed") + " (" + String.valueOf(result) + ")";
        };
    }

    private static String unclaimResultText(UiLanguage language, ClaimService.UnclaimResult result) {
        return switch (result) {
            case OK -> PersonalClaimsPage.text(language, "release_ok");
            case NOT_OWNED -> PersonalClaimsPage.text(language, "release_empty");
            case NOT_YOUR_CLAIM -> PersonalClaimsPage.text(language, "release_other");
            case WOULD_DISCONNECT -> PersonalClaimsPage.text(language, "release_disconnect");
            default -> PersonalClaimsPage.text(language, "release_other");
        };
    }

    private static String text(UiLanguage language, String key) {
        boolean de = language == UiLanguage.GERMAN;
        return switch (key) {
            case "page_title" -> {
                if (de) {
                    yield "PERS\u00d6NLICHES LAND";
                }
                yield "PERSONAL LAND";
            }
            case "page_subtitle" -> {
                if (de) {
                    yield "ENDLESS GUILDS \u00b7 GESCH\u00dcTZTE EINZEL-CLAIMS";
                }
                yield "ENDLESS GUILDS \u00b7 PROTECTED SOLO CLAIMS";
            }
            case "owner_role" -> {
                if (de) {
                    yield "EINZEL-ABENTEURER";
                }
                yield "SOLO ADVENTURER";
            }
            case "land_label" -> {
                if (de) {
                    yield "LAND";
                }
                yield "LAND";
            }
            case "limit_label" -> {
                if (de) {
                    yield "LIMIT";
                }
                yield "LIMIT";
            }
            case "world_label" -> {
                if (de) {
                    yield "WELT";
                }
                yield "WORLD";
            }
            case "info_hint" -> {
                if (de) {
                    yield "Deine Claims werden vollst\u00e4ndig von Endless Guilds gesch\u00fctzt.";
                }
                yield "Your land stays fully protected by Endless Guilds.";
            }
            case "map_title" -> {
                if (de) {
                    yield "DEINE GEBIETSKARTE";
                }
                yield "YOUR TERRITORY MAP";
            }
            case "map_hint" -> {
                if (de) {
                    yield "W\u00e4hle einen Chunk. Gr\u00fcn ist frei, Blau geh\u00f6rt dir und Rot ist bereits vergeben.";
                }
                yield "Select a chunk. Green is free, blue is yours, and red is already claimed.";
            }
            case "center" -> {
                if (de) {
                    yield "Zentrieren";
                }
                yield "Center";
            }
            case "legend_own" -> {
                if (de) {
                    yield "Dein Land";
                }
                yield "Your land";
            }
            case "legend_available" -> {
                if (de) {
                    yield "Beanspruchbar";
                }
                yield "Claimable";
            }
            case "legend_other" -> {
                if (de) {
                    yield "Belegt";
                }
                yield "Claimed";
            }
            case "legend_selected" -> {
                if (de) {
                    yield "Ausgew\u00e4hlt";
                }
                yield "Selected";
            }
            case "close" -> {
                if (de) {
                    yield "Schlie\u00dfen";
                }
                yield "Close";
            }
            case "selection_title" -> {
                if (de) {
                    yield "AUSGEW\u00c4HLTER CHUNK";
                }
                yield "SELECTED CHUNK";
            }
            case "selected_owned" -> {
                if (de) {
                    yield "Dein gesch\u00fctztes Land";
                }
                yield "Your protected land";
            }
            case "selected_available" -> {
                if (de) {
                    yield "F\u00fcr die Beanspruchung ausw\u00e4hlen";
                }
                yield "Select to claim";
            }
            case "selected_other" -> {
                if (de) {
                    yield "Bereits von einem anderen Spieler beansprucht";
                }
                yield "Already claimed by another player";
            }
            case "claim" -> {
                if (de) {
                    yield "Chunk beanspruchen";
                }
                yield "Claim chunk";
            }
            case "release" -> {
                if (de) {
                    yield "Chunk freigeben";
                }
                yield "Release chunk";
            }
            case "status_hint" -> {
                if (de) {
                    yield "W\u00e4hle einen Chunk auf der Karte aus.";
                }
                yield "Select a chunk on the map.";
            }
            case "invalid_selection" -> {
                if (de) {
                    yield "Ung\u00fcltige Chunkauswahl.";
                }
                yield "Invalid chunk selection.";
            }
            case "claim_ok" -> {
                if (de) {
                    yield "Chunk erfolgreich beansprucht.";
                }
                yield "Chunk claimed successfully.";
            }
            case "claim_guild_member" -> {
                if (de) {
                    yield "Gildenmitglieder können keine persönlichen Claims erstellen.";
                }
                yield "Guild members cannot create personal claims.";
            }
            case "claim_cap" -> {
                if (de) {
                    yield "Dein pers\u00f6nliches Claim-Limit ist erreicht.";
                }
                yield "Your personal claim limit has been reached.";
            }
            case "claim_contiguous" -> {
                if (de) {
                    yield "Neue Claims m\u00fcssen an dein bestehendes Land angrenzen.";
                }
                yield "New claims must connect to your existing land.";
            }
            case "claim_already" -> {
                if (de) {
                    yield "Dieser Chunk geh\u00f6rt bereits dir.";
                }
                yield "This chunk already belongs to you.";
            }
            case "claim_other" -> {
                if (de) {
                    yield "Dieser Chunk geh\u00f6rt bereits jemand anderem.";
                }
                yield "This chunk is already claimed by someone else.";
            }
            case "claim_world" -> {
                if (de) {
                    yield "In dieser Welt k\u00f6nnen keine pers\u00f6nlichen Claims erstellt werden.";
                }
                yield "Personal claims are not available in this world.";
            }
            case "claim_failed" -> {
                if (de) {
                    yield "Chunk konnte nicht beansprucht werden";
                }
                yield "Could not claim chunk";
            }
            case "release_ok" -> {
                if (de) {
                    yield "Chunk wurde freigegeben.";
                }
                yield "Chunk released.";
            }
            case "release_empty" -> {
                if (de) {
                    yield "Dieser Chunk ist nicht beansprucht.";
                }
                yield "This chunk is not claimed.";
            }
            case "release_other" -> {
                if (de) {
                    yield "Dieser Chunk geh\u00f6rt dir nicht.";
                }
                yield "This chunk does not belong to you.";
            }
            case "release_disconnect" -> {
                if (de) {
                    yield "Dieser Chunk kann nicht freigegeben werden, weil dein Land sonst getrennt w\u00e4re.";
                }
                yield "This chunk cannot be released because it would split your land.";
            }
            default -> key;
        };
    }

    private static enum UiLanguage {
        GERMAN,
        ENGLISH;

    }

    public static final class ActionData {
        private String action;
    }
}
