/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.startup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.mock.env.MockEnvironment;

class PluginConflictDetectorTest {

  private static final String CORE_CLASS = "riv/clinicalprocess/healthcond/actoutcome/v4/PatientSummaryHeaderType.class";
  private static final String CORE_XSD = "schemas/core_components/clinicalprocess_healthcond_actoutcome_4.2.xsd";

  @TempDir
  Path dir;

  private final List<LogEvent> events = new ArrayList<>();
  private Logger logger;
  private AbstractAppender appender;
  private Level originalLevel;

  @BeforeEach
  void attachAppender() {
    logger = (Logger) LogManager.getLogger(PluginConflictDetector.class);
    originalLevel = logger.getLevel();
    appender = new AbstractAppender("capture", null, null, true, Property.EMPTY_ARRAY) {
      @Override
      public void append(LogEvent event) {
        events.add(event.toImmutable());
      }
    };
    appender.start();
    logger.addAppender(appender);
    logger.setLevel(Level.INFO);
  }

  @AfterEach
  void detachAppender() {
    logger.removeAppender(appender);
    logger.setLevel(originalLevel);
  }

  @Test
  void conflictingCoreClassesProduceWarning() throws IOException {
    jar("GetAggregatedA-main-1.0-all.jar", Map.of(CORE_CLASS, "v4.1", CORE_XSD, "xsd-4.1"));
    jar("GetAggregatedB-main-1.0-all.jar", Map.of(CORE_CLASS, "v4.2", CORE_XSD, "xsd-4.2"));

    detector(dir.toString()).onApplicationEvent(mock(ApplicationReadyEvent.class));

    List<String> warnings = warnings();
    assertEquals(3, warnings.size(), warnings.toString());
    assertTrue(warnings.get(0).contains("riv.clinicalprocess.healthcond.actoutcome.v4"));
    assertTrue(warnings.get(0).contains("GetAggregatedA-main-1.0-all.jar"));
    assertTrue(warnings.get(0).contains("GetAggregatedB-main-1.0-all.jar"));
    assertTrue(warnings.get(1).contains("schemas.core_components"));
    assertTrue(warnings.get(2).contains("2 conflicting class(es)/resource(s) in 2 package(s)"));
  }

  @Test
  void identicalContentProducesNoWarning() throws IOException {
    jar("a-all.jar", Map.of(CORE_CLASS, "same", CORE_XSD, "same"));
    jar("b-all.jar", Map.of(CORE_CLASS, "same", CORE_XSD, "same"));

    detector(dir.toString()).onApplicationEvent(mock(ApplicationReadyEvent.class));

    assertTrue(warnings().isEmpty(), warnings().toString());
  }

  @Test
  void irrelevantEntriesAreIgnored() throws IOException {
    jar("a-all.jar", Map.of("META-INF/MANIFEST.MF", "a", "application.properties", "a", "module-info.class", "a"));
    jar("b-all.jar", Map.of("META-INF/MANIFEST.MF", "b", "application.properties", "b", "module-info.class", "b"));

    assertTrue(PluginConflictDetector.findConflicts(PluginConflictDetector.resolveJars(dir.toString())).isEmpty());
  }

  @Test
  void nestedJarsAreScanned() throws IOException {
    jar("a-all.jar", Map.of(CORE_CLASS, "v4.1"));
    Map<String, byte[]> outer = new LinkedHashMap<>();
    outer.put("lib/schemas.jar", zip(Map.of(CORE_CLASS, "v4.2".getBytes(StandardCharsets.UTF_8))));
    writeZip(dir.resolve("b-all.jar"), outer);

    Map<String, Map<String, Set<String>>> conflicts =
        PluginConflictDetector.findConflicts(PluginConflictDetector.resolveJars(dir.toString()));

    assertEquals(Set.of(CORE_CLASS), conflicts.keySet());
    assertTrue(conflicts.get(CORE_CLASS).values().stream().anyMatch(s -> s.contains("b-all.jar!/lib/schemas.jar")));
  }

  @Test
  void resolvesCommaSeparatedDirsAndJars() throws IOException {
    Path sub = Files.createDirectory(dir.resolve("sub"));
    Path a = jar("a.jar", Map.of());
    Path b = jarIn(sub, "b.jar", Map.of());
    Files.writeString(dir.resolve("readme.txt"), "x");

    List<Path> jars = PluginConflictDetector.resolveJars(dir + ", file:" + b + ",missing/,x.jar!/lib/");

    assertEquals(List.of(a, b), jars);
  }

  @Test
  void missingLoaderPathIsSkipped() {
    new PluginConflictDetector(new MockEnvironment()).onApplicationEvent(mock(ApplicationReadyEvent.class));

    assertTrue(events.isEmpty());
  }

  private PluginConflictDetector detector(String loaderPath) {
    return new PluginConflictDetector(
        new MockEnvironment().withProperty(PluginConflictDetector.LOADER_PATH_PROPERTY, loaderPath));
  }

  private List<String> warnings() {
    return events.stream()
        .filter(e -> e.getLevel() == Level.WARN)
        .map(e -> e.getMessage().getFormattedMessage())
        .toList();
  }

  private Path jar(String name, Map<String, String> entries) throws IOException {
    return jarIn(dir, name, entries);
  }

  private Path jarIn(Path parent, String name, Map<String, String> entries) throws IOException {
    Map<String, byte[]> bytes = new LinkedHashMap<>();
    entries.forEach((k, v) -> bytes.put(k, v.getBytes(StandardCharsets.UTF_8)));
    Path path = parent.resolve(name);
    writeZip(path, bytes);
    return path;
  }

  private static void writeZip(Path path, Map<String, byte[]> entries) throws IOException {
    try (OutputStream out = Files.newOutputStream(path)) {
      out.write(zip(entries));
    }
  }

  private static byte[] zip(Map<String, byte[]> entries) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
      for (Map.Entry<String, byte[]> e : entries.entrySet()) {
        zip.putNextEntry(new ZipEntry(e.getKey()));
        zip.write(e.getValue());
        zip.closeEntry();
      }
    }
    return bytes.toByteArray();
  }
}
