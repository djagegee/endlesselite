package de.shadow.seuchenweber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class EndlessEliteConfigArtifactsTest {
  private static final Path EXAMPLES = Path.of("..", "config", "examples");
  private static final List<String> NAMES = List.of("default", "balanced", "easy", "hard", "pvp");

  @Test void allExamplesUseTheSameReadableSchema() throws Exception {
    Set<String> expectedKeys = yamlKeys(readExample("default"));
    assertEquals(jsonKeys(readBundledJson()), expectedKeys, "Default YAML and bundled JSON schema differ");
    for (String name : NAMES) {
      String yaml = readExample(name);
      assertEquals(expectedKeys, yamlKeys(yaml), name + " schema");
      assertFalse(yaml.toLowerCase().contains("milliseconds"), name);
      assertFalse(yaml.toLowerCase().contains("durationms"), name);
      assertFalse(yaml.toLowerCase().contains("_ms"), name);
      assertFalse(Pattern.compile("\\d+,\\d+").matcher(yaml).find(), name + " contains decimal comma");
      assertFalse(Pattern.compile("\\d+\\.\\d{3,}").matcher(yaml).find(), name + " contains a long decimal");
    }
  }

  @Test void bundledAndExampleNumbersStayOperatorReadable() throws Exception {
    String json = readBundledJson();
    String lower = json.toLowerCase();
    assertFalse(lower.contains("milliseconds"));
    assertFalse(lower.contains("_ms"));
    assertFalse(Pattern.compile("\\\"[^\\\"]*Percent\\\"\\s*:").matcher(json).find());
    assertFalse(Pattern.compile("\\d+\\.\\d{3,}").matcher(json).find(), "Bundled JSON contains a long decimal");
    for (String key : jsonKeys(json)) {
      String normalized = key.toLowerCase();
      if (normalized.contains("duration") || normalized.contains("cooldown") || normalized.contains("interval")) {
        assertTrue(key.endsWith("Seconds"), "Public time key must end in Seconds: " + key);
      }
    }
  }

  @Test void everyOperatorSettingIsDocumented() throws Exception {
    String documentation = Files.readString(Path.of("..", "docs", "CONFIGURATION.md"), StandardCharsets.UTF_8);
    for (String path : scalarYamlPaths(readExample("default"))) {
      if (!path.startsWith("class.")) assertTrue(documentation.contains("`" + path + "`"), "Missing docs: " + path);
    }
    for (String explanation : List.of("1.0 = 100 %", "0.9 = 90 %", "0.75 = 75 %", "0.5 = 50 %", "0.1 = 10 %")) {
      assertTrue(documentation.contains(explanation), explanation);
    }
  }

  private static String readExample(String name) throws IOException {
    return Files.readString(EXAMPLES.resolve("endlesselite-" + name + ".yml"), StandardCharsets.UTF_8);
  }

  private static String readBundledJson() throws IOException {
    return Files.readString(Path.of("src", "main", "resources", "config", "seuchenweber.json"), StandardCharsets.UTF_8);
  }

  private static Set<String> jsonKeys(String json) {
    Set<String> keys = new LinkedHashSet<>();
    Matcher matcher = Pattern.compile("\"([^\"]+)\"\\s*:").matcher(json);
    while (matcher.find()) keys.add(matcher.group(1));
    return keys;
  }

  private static Set<String> yamlKeys(String yaml) {
    Set<String> keys = new LinkedHashSet<>();
    for (String line : yaml.lines().toList()) {
      String stripped = line.strip();
      if (stripped.isEmpty() || stripped.startsWith("#")) continue;
      int colon = stripped.indexOf(':');
      if (colon > 0) keys.add(stripped.substring(0, colon));
    }
    return keys;
  }

  private static Set<String> scalarYamlPaths(String yaml) {
    Set<String> paths = new LinkedHashSet<>();
    Deque<Node> parents = new ArrayDeque<>();
    for (String line : yaml.lines().toList()) {
      String stripped = line.strip();
      if (stripped.isEmpty() || stripped.startsWith("#")) continue;
      int indent = line.indexOf(stripped);
      int colon = stripped.indexOf(':');
      if (colon <= 0) continue;
      while (!parents.isEmpty() && parents.peekLast().indent() >= indent) parents.removeLast();
      String key = stripped.substring(0, colon);
      String prefix = parents.stream().map(Node::key).reduce((left, right) -> left + "." + right).orElse("");
      String path = prefix.isEmpty() ? key : prefix + "." + key;
      if (stripped.substring(colon + 1).strip().isEmpty()) parents.addLast(new Node(indent, key));
      else paths.add(path);
    }
    return paths;
  }

  private record Node(int indent, String key) { }
}
