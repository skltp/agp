/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.startup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Scans the plugin jars on {@code loader.path} at startup and logs a warning when the same class or
 * schema resource is present in several jars with different content. Such conflicts typically arise
 * when aggregating services sharing a service domain's core_components are built from different
 * schema versions; which variant gets loaded is then decided by classpath order.
 */
@Log4j2
@Component
public class PluginConflictDetector implements ApplicationListener<ApplicationReadyEvent> {

  static final String LOADER_PATH_PROPERTY = "loader.path";
  private static final List<String> RELEVANT_SUFFIXES = List.of(".class", ".xsd", ".wsdl");
  private static final int MAX_EXAMPLES = 5;

  private final Environment environment;

  public PluginConflictDetector(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    String loaderPath = environment.getProperty(LOADER_PATH_PROPERTY);
    if (loaderPath == null || loaderPath.isBlank()) {
      log.debug("No {} configured, skipping plugin conflict check", LOADER_PATH_PROPERTY);
      return;
    }
    try {
      List<Path> jars = resolveJars(loaderPath);
      log.info("Checking {} plugin jar(s) on {} for conflicting classes/resources", jars.size(), LOADER_PATH_PROPERTY);
      logConflicts(findConflicts(jars));
    } catch (RuntimeException e) {
      log.warn("Plugin conflict check failed", e);
    }
  }

  static List<Path> resolveJars(String loaderPath) {
    Set<Path> jars = new LinkedHashSet<>();
    for (String raw : loaderPath.split(",")) {
      String entry = raw.trim();
      if (entry.startsWith("file:")) {
        entry = entry.substring("file:".length());
      }
      if (entry.isEmpty() || entry.contains("!")) {
        continue;
      }
      Path path = Paths.get(entry);
      if (Files.isDirectory(path)) {
        try (Stream<Path> files = Files.list(path)) {
          files.filter(PluginConflictDetector::isJar).sorted().forEach(jars::add);
        } catch (IOException e) {
          log.warn("Could not list {} entry {}", LOADER_PATH_PROPERTY, path, e);
        }
      } else if (isJar(path)) {
        jars.add(path);
      }
    }
    return new ArrayList<>(jars);
  }

  /**
   * @return entry name -> (content hash -> jars containing that variant), only for entries with
   * more than one distinct content.
   */
  static Map<String, Map<String, Set<String>>> findConflicts(List<Path> jars) {
    Map<String, Map<String, Set<String>>> entries = new TreeMap<>();
    for (Path jar : jars) {
      try (InputStream in = Files.newInputStream(jar)) {
        scanZip(new ZipInputStream(in), jar.getFileName().toString(), entries);
      } catch (IOException e) {
        log.warn("Could not scan {} for conflicts", jar, e);
      }
    }
    entries.values().removeIf(variants -> variants.size() < 2);
    return entries;
  }

  private static void scanZip(ZipInputStream zip, String source, Map<String, Map<String, Set<String>>> entries)
      throws IOException {
    ZipEntry entry;
    while ((entry = zip.getNextEntry()) != null) {
      String name = entry.getName();
      if (entry.isDirectory()) {
        continue;
      }
      if (name.toLowerCase(Locale.ROOT).endsWith(".jar")) {
        scanZip(new ZipInputStream(zip), source + "!/" + name, entries);
      } else if (isRelevant(name)) {
        entries.computeIfAbsent(name, k -> new TreeMap<>())
            .computeIfAbsent(sha256(zip), k -> new TreeSet<>())
            .add(source);
      }
    }
  }

  private static void logConflicts(Map<String, Map<String, Set<String>>> conflicts) {
    if (conflicts.isEmpty()) {
      log.info("No conflicting classes/resources found among plugin jars");
      return;
    }

    Map<String, List<String>> byPackage = new TreeMap<>();
    Map<String, Set<String>> jarsByPackage = new TreeMap<>();
    conflicts.forEach((name, variants) -> {
      String pkg = packageOf(name);
      byPackage.computeIfAbsent(pkg, k -> new ArrayList<>()).add(name);
      variants.values().forEach(sources -> jarsByPackage.computeIfAbsent(pkg, k -> new TreeSet<>()).addAll(sources));
      log.debug("Conflicting entry {}: {}", name, variants);
    });

    byPackage.forEach((pkg, names) -> log.warn(
        "Plugin conflict: package '{}' has {} class(es)/resource(s) with differing content in jars {}. Examples: {}",
        pkg, names.size(), jarsByPackage.get(pkg),
        names.stream().limit(MAX_EXAMPLES).collect(Collectors.joining(", "))));

    log.warn("Plugin conflict check found {} conflicting class(es)/resource(s) in {} package(s). "
            + "Which variant is used depends on classpath order. All aggregating services in the same "
            + "service domain and major version must be rebuilt and deployed together.",
        conflicts.size(), byPackage.size());
  }

  private static String packageOf(String entryName) {
    int idx = entryName.lastIndexOf('/');
    return idx < 0 ? "(default)" : entryName.substring(0, idx).replace('/', '.');
  }

  private static boolean isJar(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar");
  }

  private static boolean isRelevant(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return !lower.startsWith("meta-inf/")
        && !lower.endsWith("module-info.class")
        && RELEVANT_SUFFIXES.stream().anyMatch(lower::endsWith);
  }

  private static String sha256(InputStream in) throws IOException {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
    byte[] buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer)) != -1) {
      digest.update(buffer, 0, read);
    }
    return HexFormat.of().formatHex(digest.digest());
  }
}
