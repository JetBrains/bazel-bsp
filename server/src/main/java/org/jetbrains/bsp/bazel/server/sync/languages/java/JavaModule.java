package org.jetbrains.bsp.bazel.server.sync.languages.java;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.net.URI;
import java.util.Objects;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;

public class JavaModule implements LanguageData {
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
      @JsonProperty("jdk") Jdk jdk,
      @JsonProperty("javacOpts") List<String> javacOpts,
      @JsonProperty("jvmOps") List<String> jvmOps,
      @JsonProperty("mainOutput") URI mainOutput,
      @JsonProperty("mainClass") Option<String> mainClass,
      @JsonProperty("args") List<String> args,
      @JsonProperty("runtimeClasspath") List<URI> runtimeClasspath,
      @JsonProperty("compileClasspath") List<URI> compileClasspath,
      @JsonProperty("sourcesClasspath") List<URI> sourcesClasspath,
      @JsonProperty("ideClasspath") List<URI> ideClasspath) {
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

  @Override
  public String toString() {
    return Format.object(
        "JavaModule",
        Format.entry("jdk", jdk),
        Format.entry("javacOpts", Format.iterableShort(javacOpts)),
        Format.entry("jvmOps", Format.iterableShort(jvmOps)),
        Format.entry("mainOutput", mainOutput),
        Format.entry("mainClass", mainClass),
        Format.entry("args", Format.iterableShort(args)),
        Format.entry("runtimeClasspath", Format.iterable(runtimeClasspath)),
        Format.entry("compileClasspath", Format.iterable(compileClasspath)),
        Format.entry("sourcesClasspath", Format.iterable(sourcesClasspath)),
        Format.entry("ideClasspath", Format.iterable(ideClasspath)));
  }
}
