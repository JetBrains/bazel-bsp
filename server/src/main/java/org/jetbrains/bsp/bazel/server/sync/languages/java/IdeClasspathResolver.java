package org.jetbrains.bsp.bazel.server.sync.languages.java;

import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.util.regex.Pattern;

public class IdeClasspathResolver {
  private static final Pattern JAR_PATTERN = Pattern.compile("-[hi]jar\\.jar$");

  private final Set<String> runtimeJars;
  private final Set<String> runtimeMavenJarSuffixes;
  private final Seq<String> compileJars;

  public IdeClasspathResolver(Seq<URI> runtimeClasspath, Seq<URI> compileClasspath) {
    this.runtimeJars = runtimeClasspath.map(URI::toString).toSet();
    this.runtimeMavenJarSuffixes = runtimeJars.flatMap(this::toMavenSuffix);
    this.compileJars = compileClasspath.map(URI::toString);
  }

  public Seq<URI> resolve() {
    return compileJars.iterator().map(this::findRuntimeEquivalent).map(URI::create).toArray();
  }

  private String findRuntimeEquivalent(String compileJar) {
    var runtimeJar = JAR_PATTERN.matcher(compileJar).replaceAll(".jar");
    if (runtimeJars.contains(runtimeJar)) {
      return runtimeJar;
    }

    var headerSuffix = toMavenSuffix(compileJar);
    var mavenJarSuffix = headerSuffix.map(s -> s.replaceAll("/header_([^/]+)\\.jar$", "/$1.jar"));
    if (mavenJarSuffix.exists(runtimeMavenJarSuffixes::contains)) {
      return runtimeJars.find(jar -> jar.endsWith(mavenJarSuffix.get())).get();
    }
    return compileJar;
  }

  private Option<String> toMavenSuffix(String uri) {
    var indicator = "/maven2/";
    int index = uri.lastIndexOf(indicator);
    if (index < 0) {
      return Option.none();
    } else {
      return Option.some(uri.substring(index + indicator.length()));
    }
  }
}
