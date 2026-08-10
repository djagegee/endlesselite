package de.shadow.hymann;

import com.airijko.endlessleveling.classes.CharacterClassDefinition;
import com.ziggfreed.mmoskilltree.skilltree.SkillTreeNode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HymannProgressionContractTest {
    @Test
    void pomAndPluginManifestExposeTheSameReleaseVersion() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
        String manifest = Files.readString(
                Path.of("src", "main", "resources", "manifest.json"), StandardCharsets.UTF_8);
        String projectVersion = java.util.regex.Pattern.compile("<artifactId>hymann</artifactId>\\s*<version>([^<]+)</version>")
                .matcher(pom).results().findFirst().orElseThrow().group(1);
        String manifestVersion = java.util.regex.Pattern.compile("\\\"Version\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                .matcher(manifest).results().findFirst().orElseThrow().group(1);

        assertEquals(projectVersion, manifestVersion);
        assertFalse(projectVersion.contains("SNAPSHOT"));
    }

    @Test
    void buildUsesTheCurrentlyDeployedCoreModArtifacts() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);

        assertTrue(pom.contains("<version>11.6.1-local</version>"));
        assertTrue(pom.contains("<systemPath>${hytale.mods.root}/EndlessLeveling.jar</systemPath>"));
        assertTrue(pom.contains("<systemPath>${hytale.mods.root}/MMOSkillTree-1.5.2.jar</systemPath>"));
        assertFalse(pom.contains("${hytale.mod.stash}"));
    }

    @Test
    void prestigeThirtyThroughOneHundredFormsACompleteOrderedClassPath() throws Exception {
        Method buildDefinitions = HymannRegistrar.class.getDeclaredMethod("buildDefinitions");
        buildDefinitions.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<CharacterClassDefinition> definitions =
                (List<CharacterClassDefinition>) buildDefinitions.invoke(null);

        assertEquals(List.of(
                "hymann",
                "hymann_thunderforged",
                "hymann_aegis_vanguard",
                "hymann_skybreaker",
                "hymann_eternal_paragon"
        ), definitions.stream().map(CharacterClassDefinition::getId).toList());
        assertEquals(List.of(30, 45, 65, 85, 100), definitions.stream()
                .map(definition -> definition.getAscension().getRequirements().getRequiredPrestige())
                .toList());
        assertEquals(List.of("base", "elite", "master", "legendary", "exalted"), definitions.stream()
                .map(definition -> definition.getAscension().getStage()).toList());
        assertTrue(definitions.subList(0, 4).stream().noneMatch(
                definition -> definition.getAscension().isFinalForm()));
        assertTrue(definitions.subList(0, 4).stream().allMatch(
                definition -> definition.getAscension().getNextPaths().size() == 1));
        assertTrue(definitions.getLast().getAscension().isFinalForm());
        assertTrue(definitions.getLast().getAscension().getNextPaths().isEmpty());
        assertTrue(definitions.stream().allMatch(
                definition -> HymannAccess.PERMISSION.equals(definition.getRequiredPermission())));
    }

    @Test
    void pluginRegistersOneProfileScopedProgressOwnerAndHydrationReconciler() throws Exception {
        String plugin = Files.readString(
                Path.of("src", "main", "java", "de", "shadow", "hymann", "HymannPlugin.java"),
                StandardCharsets.UTF_8);

        assertTrue(plugin.contains("private HymannProfileProgressSystem profileProgressSystem;"));
        assertTrue(plugin.contains("private HymannSkillLifecycleSystem skillLifecycleSystem;"));
        assertTrue(plugin.contains("new HymannProfileProgressSystem("));
        assertTrue(plugin.contains("profile-progress.properties"));
        assertTrue(plugin.contains("profileProgressSystem.register("));
        assertTrue(plugin.contains("new HymannSkillLifecycleSystem("));
        assertTrue(plugin.contains("skillLifecycleSystem.register("));
        assertTrue(plugin.contains("profileProgressSystem.clear()"));
        assertTrue(plugin.contains("skillLifecycleSystem.clear()"));
        assertFalse(plugin.contains("new HymannAbilityBindingPersistenceSystem("));
    }

    @Test
    void firstProfileMigrationPreservesLegacyProgressInsteadOfClearingIt() throws Exception {
        String progress = Files.readString(
                Path.of("src", "main", "java", "de", "shadow", "hymann", "HymannProfileProgressSystem.java"),
                StandardCharsets.UTF_8);

        assertTrue(progress.contains("Progress initialProgress = HymannProfileProgressSystem.capture(skills);"));
        assertTrue(progress.contains("this.saveProfile(uuid, slot, initialProgress);"));
        assertTrue(progress.contains("Migrated legacy account-wide Hymann progression to Endless profile"));
        assertTrue(progress.contains("Initialized empty Hymann progression for new Endless profile"));
        assertTrue(progress.contains("private Store<EntityStore> storeIdentity;"));
        assertTrue(progress.contains("context.storeIdentity != store"));
        assertFalse(progress.contains("System.identityHashCode(store)"));
        assertTrue(progress.contains("classId.startsWith(\"hymann\")"));
        assertFalse(progress.contains("classId.equals(\"hymann_eternal_paragon\")"));
    }

    @Test
    void armamentTreeCoversAllMilestonesThroughLevelOneHundred() throws Exception {
        Method buildTree = HymannMmoBridge.class.getDeclaredMethod("buildTree");
        buildTree.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<SkillTreeNode> nodes = (List<SkillTreeNode>) buildTree.invoke(null);

        assertEquals(38, nodes.size());
        assertEquals(2, nodes.getFirst().getLevelRequired());
        assertEquals(100, nodes.getLast().getLevelRequired());
        assertEquals(38, nodes.stream().map(SkillTreeNode::getLevelRequired).distinct().count());
        assertTrue(nodes.stream().allMatch(node -> !node.getChoices().isEmpty()));
    }
}
