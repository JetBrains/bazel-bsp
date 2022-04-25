package org.jetbrains.bsp.bazel.server.sync.languages.java;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;

public class JavaModule implements LanguageData {
  private final Jdk jdk;

  private final Option<Jdk> runtimeJdk;

  private final Seq<String> javacOpts;
  private final Seq<String> jvmOps;
  private final Path mainOutput;
  private final Option<String> mainClass;
  private final Seq<String> args;
  private final Seq<Path> runtimeClasspath;
  private final Seq<Path> compileClasspath;
  private final Seq<Path> sourcesClasspath;
  private final Seq<Path> ideClasspath;

  public JavaModule(
      @JsonProperty("jdk") Jdk jdk,
      @JsonProperty("runtimeJdk") Option<Jdk> runtimeJdk,
      @JsonProperty("javacOpts") Seq<String> javacOpts,
      @JsonProperty("jvmOps") Seq<String> jvmOps,
      @JsonProperty("mainOutput") Path mainOutput,
      @JsonProperty("mainClass") Option<String> mainClass,
      @JsonProperty("args") Seq<String> args,
      @JsonProperty("runtimeClasspath") Seq<Path> runtimeClasspath,
      @JsonProperty("compileClasspath") Seq<Path> compileClasspath,
      @JsonProperty("sourcesClasspath") Seq<Path> sourcesClasspath,
      @JsonProperty("ideClasspath") Seq<Path> ideClasspath) {
    this.jdk = jdk;
    this.runtimeJdk = runtimeJdk;
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

  public Option<Jdk> runtimeJdk() {
    return runtimeJdk;
  }

  public Seq<String> javacOpts() {
    return javacOpts;
  }

  public Seq<String> jvmOps() {
    return jvmOps;
  }

  public Path mainOutput() {
    return mainOutput;
  }

  public Option<String> mainClass() {
    return mainClass;
  }

  public Seq<String> args() {
    return args;
  }

  public Seq<Path> runtimeClasspath() {
    return runtimeClasspath;
  }

  public Seq<Path> compileClasspath() {
    return compileClasspath;
  }

  public Seq<Path> sourcesClasspath() {
    return sourcesClasspath;
  }

  public Seq<Path> ideClasspath() {
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
