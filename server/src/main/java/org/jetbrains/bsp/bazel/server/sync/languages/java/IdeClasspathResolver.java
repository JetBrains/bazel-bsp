package org.jetbrains.bsp.bazel.server.sync.languages.java;

import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IdeClasspathResolver {

  private final Set<String> runtimeJars;
  private final Set<String> runtimeMavenJarSuffixes;
  private final Seq<String> compileJars;

  public IdeClasspathResolver(Seq<Path> runtimeClasspath, Seq<Path> compileClasspath) {
    this.runtimeJars = runtimeClasspath.map(Path::toString).toSet();
    this.runtimeMavenJarSuffixes = runtimeJars.flatMap(this::toMavenSuffix);
    this.compileJars = compileClasspath.map(Path::toString);
  }

  public Seq<Path> resolve() {
    return compileJars.iterator().map(this::findRuntimeEquivalent).map(Paths::get).toArray();
  }

  private String findRuntimeEquivalent(String compileJar) {
    var runtimeJar = compileJar.replaceAll("-[hi]jar\\.jar$", ".jar");
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
