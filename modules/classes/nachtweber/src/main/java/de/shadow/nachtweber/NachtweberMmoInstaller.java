package de.shadow.nachtweber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** Additive, backup-safe staging installer. The live plugin does not invoke it until gameplay effects exist. */
final class NachtweberMmoInstaller {
  private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
  private NachtweberMmoInstaller() { }

  static int installAbilities(Path file) throws IOException {
    JsonObject root=Files.exists(file)?JsonParser.parseString(Files.readString(file,StandardCharsets.UTF_8)).getAsJsonObject():new JsonObject();
    if(!root.has("schemaVersion")) root.addProperty("schemaVersion",24);
    if(!root.has("enabled")) root.addProperty("enabled",true);
    JsonObject abilities=root.has("abilities")?root.getAsJsonObject("abilities"):new JsonObject();
    root.add("abilities",abilities);
    abilities.add("nachtweber_black_thread",blackThread());
    abilities.add("nachtweber_shadow_swing",shadowSwing());
    abilities.add("nachtweber_hunting_cocoon",huntingCocoon());
    abilities.add("nachtweber_passive_danger_sense",dangerSense());
    abilities.add("nachtweber_passive_wall_hunter",wallHunter());
    abilities.add("nachtweber_passive_toxic_glands",toxicGlands());
    abilities.add("nachtweber_passive_hunting_instinct",huntingInstinct());
    writeWithSingleBackup(file,root,".nachtweber-before.bak");
    return 7;
  }

  static int installLocalization(Path directory) throws IOException {
    Files.createDirectories(directory);
    Map<String,String> german=messages(true);
    Map<String,String> english=messages(false);
    installLocale(directory.resolve("messages-de-DE.json"),german);
    installLocale(directory.resolve("messages-en-US.json"),english);
    return german.size();
  }

  private static JsonObject blackThread() {
    JsonObject value=common("NACHTWEBER_BLACK_THREAD","Black Thread","Binds a server-selected hostile target with black thread.");
    value.addProperty("cooldownMs",3_000);
    JsonObject params=new JsonObject();
    params.addProperty("useCustomCardDescription",true);
    params.addProperty("range",12.0);
    params.addProperty("stacks",2);
    params.addProperty("stateDurationMs",8_000);
    params.addProperty("boundControlDurationMs",1_200);
    value.add("params",params);
    return value;
  }

  private static JsonObject shadowSwing() {
    JsonObject value=common("NACHTWEBER_SHADOW_SWING","Shadow Swing","Pulls the Nachtweber toward a server-confirmed fixed anchor.");
    value.addProperty("cooldownMs",5_000);
    JsonObject params=new JsonObject(); params.addProperty("useCustomCardDescription",true);
    params.addProperty("range",18.0); params.addProperty("minimumAnchorDistance",2.0);
    params.addProperty("horizontalImpulse",12.0); params.addProperty("verticalImpulse",4.0);
    value.add("params",params); return value;
  }

  private static JsonObject huntingCocoon() {
    JsonObject value=common("NACHTWEBER_HUNTING_COCOON","Hunting Cocoon","Consumes only the caster's own entanglement and creates owner-bound venom.");
    value.addProperty("cooldownMs",7_000);
    JsonObject params=new JsonObject();
    params.addProperty("useCustomCardDescription",true);
    params.addProperty("range",8.0);
    params.addProperty("requiredEntanglementStacks",3);
    params.addProperty("venomStacks",2);
    params.addProperty("venomDurationMs",8_000);
    value.add("params",params);
    return value;
  }


  private static JsonObject dangerSense() {
    JsonObject value=common("NEXT_HIT_BUFF","Danger Sense","Warns of the nearest visible hostile combat target within limited server range.");
    value.addProperty("passive",true); value.addProperty("cooldownMs",0);
    JsonObject params=new JsonObject(); params.addProperty("useCustomCardDescription",true);
    params.addProperty("radiusBlocks",16.0); params.addProperty("warningCooldownMs",2_000); params.addProperty("maximumCandidatesPerScan",64);
    JsonObject trigger=new JsonObject(); trigger.addProperty("event","nachtweber_server_scan_unregistered"); params.add("passiveTrigger",trigger); value.add("params",params); return value;
  }

  private static JsonObject wallHunter() {
    JsonObject value=common("NEXT_HIT_BUFF","Wall Hunter","Produces upward movement only with confirmed horizontal wall contact and jump input.");
    value.addProperty("passive",true); value.addProperty("cooldownMs",0);
    JsonObject params=new JsonObject(); params.addProperty("useCustomCardDescription",true);
    params.addProperty("climbSpeed",4.0); params.addProperty("requiresHorizontalWallContact",true); params.addProperty("requiresUpwardInput",true);
    JsonObject trigger=new JsonObject(); trigger.addProperty("event","nachtweber_wall_tick_unregistered"); params.add("passiveTrigger",trigger); value.add("params",params); return value;
  }

  private static JsonObject toxicGlands() {
    JsonObject value=common("NEXT_HIT_BUFF","Toxic Glands","Confirmed direct hits create an owner-isolated venom stack.");
    value.addProperty("passive",true); value.addProperty("cooldownMs",0);
    JsonObject params=new JsonObject(); params.addProperty("useCustomCardDescription",true);
    params.addProperty("baseVenomStacks",1); params.addProperty("venomDurationMs",6_000); params.addProperty("internalCooldownMs",1_000);
    JsonObject trigger=new JsonObject(); trigger.addProperty("event","nachtweber_verified_direct_hit_unregistered"); params.add("passiveTrigger",trigger);
    value.add("params",params); return value;
  }

  private static JsonObject huntingInstinct() {
    JsonObject value=common("NEXT_HIT_BUFF","Hunting Instinct","Grants an additional venom stack only against the caster's own active entanglement.");
    value.addProperty("passive",true); value.addProperty("cooldownMs",0);
    JsonObject params=new JsonObject(); params.addProperty("useCustomCardDescription",true);
    params.addProperty("requiresOwnEntanglement",true); params.addProperty("bonusVenomStacks",1);
    JsonObject trigger=new JsonObject(); trigger.addProperty("event","nachtweber_verified_direct_hit_unregistered"); params.add("passiveTrigger",trigger);
    value.add("params",params); return value;
  }


  private static JsonObject common(String effect,String name,String description) {
    JsonObject value=new JsonObject(); value.addProperty("effect",effect); value.addProperty("displayName",name); value.addProperty("description",description); value.addProperty("icon","Nachtweber_Skill_Placeholder");
    JsonArray xp=new JsonArray(); xp.add(NachtweberMmoContract.SKILL_ID); value.add("xpSkills",xp); return value;
  }

  private static Map<String,String> messages(boolean german) {
    LinkedHashMap<String,String> out=new LinkedHashMap<>();
    out.put("skill.nachtweber_mastery",german?"Nachtweber":"Nightweaver");
    out.put("skill.nachtweber_mastery.desc",german?"Meisterschaft des Schwarzen Netzes und owner-gebundenen Fanggifts.":"Mastery of the Black Web and owner-bound venom.");
    for(NachtweberMmoContract.Unlock unlock:NachtweberMmoContract.unlocks()) {
      out.put("ability."+unlock.abilityId()+".name",german?unlock.germanName():unlock.englishName());
      String kind=unlock.passive()?(german?"Passiv":"Passive"):(german?"Aktiv":"Active");
      out.put("ability."+unlock.abilityId()+".flavor",kind+" · "+(german?"Freischaltung auf Stufe ":"Unlock at level ")+unlock.level()+".");
    }
    return out;
  }

  private static void installLocale(Path file,Map<String,String> additions) throws IOException {
    JsonObject root=Files.exists(file)?JsonParser.parseString(Files.readString(file,StandardCharsets.UTF_8)).getAsJsonObject():new JsonObject(); additions.forEach(root::addProperty); writeWithSingleBackup(file,root,".nachtweber-before.bak");
  }

  private static void writeWithSingleBackup(Path file,JsonObject content,String suffix) throws IOException {
    Files.createDirectories(file.getParent()); Path backup=file.resolveSibling(file.getFileName()+suffix); if(Files.exists(file)&&Files.notExists(backup)) Files.copy(file,backup);
    Path temporary=file.resolveSibling(file.getFileName()+".nachtweber.tmp"); Files.writeString(temporary,GSON.toJson(content)+System.lineSeparator(),StandardCharsets.UTF_8);
    try { Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
    catch(AtomicMoveNotSupportedException unsupported) { Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING); }
  }
}
