package org.jetbrains.bsp.bazel.server.sync.languages.java;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.net.URI;
import java.util.Objects;

public class JavaModule {
  private final Jdk jdk;
  private final List<String> javacOpts;
  private final List<String> jvmOps;
  private final URI mainOutput;
  private final Option<String> mainClass;
  private final List<String> args;
  private final List<URI> runtimeClasspath;
  private final List<URI> compileClasspath;
  private final List<URI> sourcesClasspath;
  private final List<URI> ideClasspath;

  public JavaModule(
      Jdk jdk,
      List<String> javacOpts,
      List<String> jvmOps,
      URI mainOutput,
      Option<String> mainClass,
      List<String> args,
      List<URI> runtimeClasspath,
      List<URI> compileClasspath,
      List<URI> sourcesClasspath,
      List<URI> ideClasspath) {
    this.jdk = jdk;
    this.javacOpts = javacOpts;
    this.jvmOps = jvmOps;
    this.mainOutput = mainOutput;
    this.mainClass = mainClass;
    this.args = args;
    this.runtimeClasspath = runtimeClasspath;
    this.compileClasspath = compileClasspath;
    this.sourcesClasspath = sourcesClasspath;
    this.ideClasspath = ideClasspath;
  }

  public Jdk jdk() {
    return jdk;
  }

  public List<String> javacOpts() {
    return javacOpts;
  }

  public List<String> jvmOps() {
    return jvmOps;
  }

  public URI mainOutput() {
    return mainOutput;
  }

  public Option<String> mainClass() {
    return mainClass;
  }

  public List<String> args() {
    return args;
  }

  public List<URI> runtimeClasspath() {
    return runtimeClasspath;
  }

  public List<URI> compileClasspath() {
    return compileClasspath;
  }

  public List<URI> sourcesClasspath() {
    return sourcesClasspath;
  }

  public List<URI> ideClasspath() {
    return ideClasspath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JavaModule that = (JavaModule) o;
    return jdk.equals(that.jdk)
        && javacOpts.equals(that.javacOpts)
        && jvmOps.equals(that.jvmOps)
        && mainOutput.equals(that.mainOutput)
        && runtimeClasspath.equals(that.runtimeClasspath)
        && compileClasspath.equals(that.compileClasspath)
        && sourcesClasspath.equals(that.sourcesClasspath)
        && ideClasspath.equals(that.ideClasspath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        jdk,
        javacOpts,
        jvmOps,
        mainOutput,
        runtimeClasspath,
        compileClasspath,
        sourcesClasspath,
        ideClasspath);
  }
}
