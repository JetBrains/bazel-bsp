package org.jetbrains.bsp.bazel.server.sync.languages.scala;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Objects;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;

public class ScalaModule {
  private final ScalaSdk sdk;
  private final List<String> scalacOpts;
  private final Option<JavaModule> javaModule;

  public ScalaModule(ScalaSdk sdk, List<String> scalacOpts, Option<JavaModule> javaModule) {
    this.sdk = sdk;
    this.scalacOpts = scalacOpts;
    this.javaModule = javaModule;
  }

  public ScalaSdk sdk() {
    return sdk;
  }

  public List<String> scalacOpts() {
    return scalacOpts;
  }

  public Option<JavaModule> javaModule() {
    return javaModule;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ScalaModule that = (ScalaModule) o;
    return Objects.equals(sdk, that.sdk)
        && Objects.equals(scalacOpts, that.scalacOpts)
        && Objects.equals(javaModule, that.javaModule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sdk, scalacOpts, javaModule);
  }
}
