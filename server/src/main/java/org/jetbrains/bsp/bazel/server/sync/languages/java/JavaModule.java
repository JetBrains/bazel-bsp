package org.jetbrains.bsp.bazel.server.sync.languages.java;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.net.URI;
import java.util.Objects;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule;

public class JavaModule implements LanguageData {
  private final Jdk jdk;
  private final Option<Jdk> runtimeJdk;
  private final Seq<String> javacOpts;
  private final Seq<String> jvmOps;
  private final URI mainOutput;
  private final Option<String> mainClass;
  private final Seq<String> args;
  private final Seq<URI> runtimeClasspath;
  private final Seq<URI> compileClasspath;
  private final Seq<URI> sourcesClasspath;
  private final Seq<URI> ideClasspath;
  private final Seq<URI> allOutputs;

  public JavaModule(
      @JsonProperty("jdk") Jdk jdk,
      @JsonProperty("runtimeJdk") Option<Jdk> runtimeJdk,
      @JsonProperty("javacOpts") Seq<String> javacOpts,
      @JsonProperty("jvmOps") Seq<String> jvmOps,
      @JsonProperty("mainOutput") URI mainOutput,
      @JsonProperty("allOutputs") Seq<URI> allOutputs,
      @JsonProperty("mainClass") Option<String> mainClass,
      @JsonProperty("args") Seq<String> args,
      @JsonProperty("runtimeClasspath") Seq<URI> runtimeClasspath,
      @JsonProperty("compileClasspath") Seq<URI> compileClasspath,
      @JsonProperty("sourcesClasspath") Seq<URI> sourcesClasspath,
      @JsonProperty("ideClasspath") Seq<URI> ideClasspath) {
    this.jdk = jdk;
    this.runtimeJdk = runtimeJdk;
    this.javacOpts = javacOpts;
    this.jvmOps = jvmOps;
    this.mainOutput = mainOutput;
    this.allOutputs = allOutputs;
    this.mainClass = mainClass;
    this.args = args;
    this.runtimeClasspath = runtimeClasspath;
    this.compileClasspath = compileClasspath;
    this.sourcesClasspath = sourcesClasspath;
    this.ideClasspath = ideClasspath;
  }

  public static Option<JavaModule> fromLanguageData(LanguageData languageData) {
    if (languageData instanceof JavaModule) {
      return Option.of((JavaModule) languageData);
    } else if (languageData instanceof ScalaModule) {
      return ((ScalaModule) languageData).javaModule();
    } else {
      return Option.none();
    }
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

  public URI mainOutput() {
    return mainOutput;
  }

  public Option<String> mainClass() {
    return mainClass;
  }

  public Seq<String> args() {
    return args;
  }

  public Seq<URI> runtimeClasspath() {
    return runtimeClasspath;
  }

  public Seq<URI> compileClasspath() {
    return compileClasspath;
  }

  public Seq<URI> sourcesClasspath() {
    return sourcesClasspath;
  }

  public Seq<URI> ideClasspath() {
    return ideClasspath;
  }

  public Seq<URI> getAllOutputs() {
    return allOutputs;
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
        && ideClasspath.equals(that.ideClasspath)
        && allOutputs.equals(that.allOutputs);
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
        ideClasspath,
        allOutputs);
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
        Format.entry("ideClasspath", Format.iterable(ideClasspath)),
        Format.entry("allOutputs", Format.iterable(allOutputs)));
  }
}
