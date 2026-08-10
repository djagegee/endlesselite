/*
 * Decompiled with CFR 0.152.
 */
package de.shadow.hymann;

import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import de.shadow.hymann.HymannConfig;
import de.shadow.hymann.HymannCriticalProfile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class HymannRegistrar {
    private static final double BASE_EVOLUTION_POWER = 0.07;
    private static final Ascension[] ASCENSIONS = new Ascension[]{new Ascension("hymann", "Hymann, Storm Heir", "base", 30, "hymann_thunderforged", 2.2, 2.0, 1.2), new Ascension("hymann_thunderforged", "Hymann, Thunderforged", "elite", 45, "hymann_aegis_vanguard", 2.85, 2.55, 1.75), new Ascension("hymann_aegis_vanguard", "Hymann, Aegis Vanguard", "master", 65, "hymann_skybreaker", 3.6, 3.25, 2.35), new Ascension("hymann_skybreaker", "Hymann, Skybreaker", "legendary", 85, "hymann_eternal_paragon", 4.55, 4.1, 3.15), new Ascension("hymann_eternal_paragon", "Hymann, Eternal Paragon", "exalted", 100, null, 5.75, 5.15, 4.2)};

    private HymannRegistrar() {
    }

    static int registerAll() throws Exception {
        int registered = 0;
        for (Ascension ascension : ASCENSIONS) {
            if (!HymannRegistrar.register(ascension)) continue;
            ++registered;
        }
        return registered;
    }

    static void unregisterAll() {
        try {
            Object api = HymannRegistrar.api();
            Method unregister = api.getClass().getMethod("unregisterClass", String.class);
            for (Ascension ascension : ASCENSIONS) {
                unregister.invoke(api, ascension.classId());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static boolean register(Ascension ascension) throws Exception {
        CharacterClassDefinition definition = HymannRegistrar.buildDefinition(ascension);
        Class<?> definitionType = Class.forName("com.airijko.endlessleveling.classes.CharacterClassDefinition");
        Object api = HymannRegistrar.api();
        Method register = api.getClass().getMethod("registerClass", definitionType, Boolean.TYPE);
        return Boolean.TRUE.equals(register.invoke(api, definition, true));
    }

    static List<CharacterClassDefinition> buildDefinitions() throws Exception {
        ArrayList<CharacterClassDefinition> definitions = new ArrayList<>();
        for (Ascension ascension : ASCENSIONS) {
            definitions.add(HymannRegistrar.buildDefinition(ascension));
        }
        return List.copyOf(definitions);
    }

    private static CharacterClassDefinition buildDefinition(Ascension ascension) throws Exception {
        Map<String, Double> weapons = HymannRegistrar.buildWeaponMultipliers(ascension);
        List<Map<String, Object>> rawPassives = HymannRegistrar.buildRawPassives(ascension);
        List<Object> passiveDefinitions = HymannRegistrar.buildPassiveDefinitions(rawPassives);
        Class<?> ascensionType = Class.forName("com.airijko.endlessleveling.races.RaceAscensionDefinition");
        Class<?> definitionType = Class.forName("com.airijko.endlessleveling.classes.CharacterClassDefinition");
        Constructor<?> definitionConstructor = definitionType.getConstructor(String.class, String.class, String.class, List.class, String.class, String.class, String.class, Boolean.TYPE, String.class, Map.class, List.class, List.class, ascensionType, String.class);
        return (CharacterClassDefinition)definitionConstructor.newInstance(ascension.classId(), ascension.displayName(), HymannRegistrar.descriptionFor(ascension), List.of("Vanguard"), "Hybrid", "melee", "vanguard", true, "Weapon_Mjolnir_Starky", weapons, rawPassives, passiveDefinitions, HymannRegistrar.buildAscension(ascension), "shadow.hymann.worthy");
    }

    private static Map<String, Double> buildWeaponMultipliers(Ascension ascension) {
        LinkedHashMap<String, Double> multipliers = new LinkedHashMap<String, Double>();
        multipliers.put("hammer", HymannRegistrar.scaledWeaponMultiplier(ascension.hammerMultiplier()));
        multipliers.put("shield", HymannRegistrar.scaledWeaponMultiplier(ascension.shieldMultiplier()));
        multipliers.put("mace", HymannRegistrar.scaledWeaponMultiplier(ascension.maceMultiplier()));
        return multipliers;
    }

    private static String descriptionFor(Ascension ascension) {
        HymannCriticalProfile.Values crit = HymannCriticalProfile.forClassId(ascension.classId());
        HymannConfig.Values config = HymannConfig.values();
        int defenseBreak = Math.round((config.bossAuraDamageTakenMultiplier - 1.0f) * 100.0f);
        return "A cosmic storm champion who channels a worthy hammer and an unyielding shield. Ascension passive: " + Math.round(crit.chance() * 100.0) + "% critical chance and +" + Math.round(crit.bonusDamage() * 100.0) + "% critical damage. Stormbreaker Aura: while Mjolnir and a Captain shield are equipped, bosses within " + Math.round(config.bossAuraRadiusBlocks) + " blocks are struck by lightning and suffer -" + defenseBreak + "% defense for all players.";
    }

    private static List<Map<String, Object>> buildRawPassives(Ascension ascension) {
        double power = ascension.power();
        ArrayList<Map<String, Object>> passives = new ArrayList<Map<String, Object>>();
        passives.add(HymannRegistrar.innate("life_force", HymannRegistrar.scaled(1.35 * power)));
        passives.add(HymannRegistrar.innate("strength", HymannRegistrar.scaled(0.82 * power)));
        passives.add(HymannRegistrar.innate("defense", HymannRegistrar.scaled(2.5 * power)));
        passives.add(HymannRegistrar.innate("stamina", HymannRegistrar.scaled(0.9 * power)));
        passives.add(HymannRegistrar.simple("HEALTH_REGEN", HymannRegistrar.scaled(0.045 * power)));
        passives.add(HymannRegistrar.simple("STAMINA_GAIN_BONUS", HymannRegistrar.scaled(4.0 * power)));
        passives.add(HymannRegistrar.simple("KNOCKBACK_RESISTANCE", Math.min(1.0, HymannRegistrar.scaled(0.3 * power))));
        return passives;
    }

    private static List<Object> buildPassiveDefinitions(List<Map<String, Object>> rawPassives) throws Exception {
        ArrayList<Object> definitions = new ArrayList<Object>();
        Class<?> passiveType = Class.forName("com.airijko.endlessleveling.races.RacePassiveDefinition");
        Class<?> archetypeType = Class.forName("com.airijko.endlessleveling.enums.ArchetypePassiveType");
        Class<?> attributeType = Class.forName("com.airijko.endlessleveling.enums.SkillAttributeType");
        Class<?> damageLayerType = Class.forName("com.airijko.endlessleveling.enums.DamageLayer");
        Class<?> categoryType = Class.forName("com.airijko.endlessleveling.enums.PassiveCategory");
        Class<?> stackingType = Class.forName("com.airijko.endlessleveling.enums.PassiveStackingStyle");
        Class<?> tierType = Class.forName("com.airijko.endlessleveling.enums.PassiveTier");
        Method passiveFromConfig = archetypeType.getMethod("fromConfigKey", String.class);
        Method attributeFromConfig = attributeType.getMethod("fromConfigKey", String.class);
        Enum commonTier = Enum.valueOf(tierType.asSubclass(Enum.class), "COMMON");
        Constructor<?> constructor = passiveType.getConstructor(archetypeType, Double.TYPE, Map.class, attributeType, damageLayerType, String.class, categoryType, stackingType, tierType, Map.class);
        for (Map<String, Object> raw : rawPassives) {
            Object type = passiveFromConfig.invoke(null, String.valueOf(raw.get("type")));
            Object attribute = raw.containsKey("attribute") ? attributeFromConfig.invoke(null, String.valueOf(raw.get("attribute"))) : null;
            definitions.add(constructor.newInstance(type, HymannRegistrar.toDouble(raw.get("value")), raw, attribute, null, null, null, null, commonTier, Collections.emptyMap()));
        }
        return definitions;
    }

    private static Object buildAscension(Ascension ascension) throws Exception {
        List<Object> nextPaths;
        Class<?> ascensionType = Class.forName("com.airijko.endlessleveling.races.RaceAscensionDefinition");
        Class<?> requirementType = Class.forName("com.airijko.endlessleveling.races.RaceAscensionRequirements");
        Class<?> pathLinkType = Class.forName("com.airijko.endlessleveling.races.RaceAscensionPathLink");
        if (ascension.nextClassId() == null) {
            nextPaths = Collections.emptyList();
        } else {
            Object link = pathLinkType.getConstructor(String.class, String.class).newInstance("class_" + ascension.nextClassId(), HymannRegistrar.displayName(ascension.nextClassId()));
            nextPaths = List.of(link);
        }
        Object requirements = ascension.requiredPrestige() == 0 ? requirementType.getMethod("none", new Class[0]).invoke(null, new Object[0]) : requirementType.getConstructor(Integer.TYPE, Map.class, Map.class, List.class, List.class, List.class, List.class).newInstance(ascension.requiredPrestige(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return ascensionType.getConstructor(String.class, String.class, String.class, Boolean.TYPE, Boolean.TYPE, requirementType, List.class).newInstance("class_" + ascension.classId(), ascension.stage(), "hymann", ascension.nextClassId() == null, false, requirements, nextPaths);
    }

    private static Object api() throws Exception {
        Class<?> apiType = Class.forName("com.airijko.endlessleveling.api.EndlessLevelingAPI");
        return apiType.getMethod("get", new Class[0]).invoke(null, new Object[0]);
    }

    private static Map<String, Object> innate(String attribute, double value) {
        Map<String, Object> passive = HymannRegistrar.simple("INNATE_ATTRIBUTE_GAIN", value);
        passive.put("attribute", attribute);
        passive.put("stat_type", "basestat");
        return passive;
    }

    private static Map<String, Object> simple(String type, double value) {
        LinkedHashMap<String, Object> passive = new LinkedHashMap<String, Object>();
        passive.put("type", type);
        passive.put("value", value);
        return passive;
    }

    private static double toDouble(Object value) {
        double d;
        if (value instanceof Number) {
            Number number = (Number)value;
            d = number.doubleValue();
        } else {
            d = Double.parseDouble(String.valueOf(value));
        }
        return d;
    }

    private static double scaled(double value) {
        return value * 0.07 * HymannConfig.values().evolutionBonusScale;
    }

    private static double scaledWeaponMultiplier(double originalMultiplier) {
        return 1.0 + (originalMultiplier - 1.0) * 0.07 * HymannConfig.values().evolutionBonusScale;
    }

    private static String displayName(String classId) {
        for (Ascension ascension : ASCENSIONS) {
            if (!ascension.classId().equals(classId)) continue;
            return ascension.displayName();
        }
        return classId;
    }

    private record Ascension(String classId, String displayName, String stage, int requiredPrestige, String nextClassId, double hammerMultiplier, double shieldMultiplier, double maceMultiplier) {
        double power() {
            return switch (this.stage) {
                case "base" -> 1.0;
                case "elite" -> 1.55;
                case "master" -> 2.2;
                case "legendary" -> 3.1;
                default -> 4.2;
            };
        }
    }
}
