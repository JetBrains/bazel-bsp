package org.jetbrains.bsp.bazel.server.sync.languages.java;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JvmBuildTarget;
import ch.epfl.scala.bsp4j.JvmEnvironmentItem;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JavaTargetInfo;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.model.Module;

public class JavaLanguagePlugin extends LanguagePlugin<JavaModule> {
  private final BazelPathsResolver bazelPathsResolver;
  private final BazelData bazelData;
  private final java.util.Map<String, String> environment = System.getenv();
  private Option<Jdk> jdk;

  public JavaLanguagePlugin(BazelPathsResolver bazelPathsResolver, BazelData bazelData) {
    this.bazelPathsResolver = bazelPathsResolver;
    this.bazelData = bazelData;
  }

  @Override
  public void prepareSync(Seq<TargetInfo> targets) {
    this.jdk = new JdkResolver(bazelPathsResolver).resolve(targets);
  }

  @Override
  public Option<JavaModule> resolveModule(TargetInfo targetInfo) {
    if (!targetInfo.hasJavaTargetInfo()) {
      return Option.none();
    }

    var javaTargetInfo = targetInfo.getJavaTargetInfo();
    var javacOpts = List.ofAll(javaTargetInfo.getJavacOptsList());
    var jvmOpts = List.ofAll(javaTargetInfo.getJvmFlagsList());
    var mainOutput = bazelPathsResolver.resolveUri(javaTargetInfo.getJars(0).getBinaryJars(0));
    var mainClass = getMainClass(javaTargetInfo);
    var args = List.ofAll(javaTargetInfo.getArgsList());
    var runtimeClasspath = bazelPathsResolver.resolveUris(javaTargetInfo.getRuntimeClasspathList());
    var compileClasspath = bazelPathsResolver.resolveUris(javaTargetInfo.getCompileClasspathList());
    var sourcesClasspath = bazelPathsResolver.resolveUris(javaTargetInfo.getSourceClasspathList());
    var ideClasspath = resolveIdeClasspath(runtimeClasspath, compileClasspath);
    var module =
        new JavaModule(
            getJdk(),
            javacOpts,
            jvmOpts,
            mainOutput,
            mainClass,
            args,
            runtimeClasspath,
            compileClasspath,
            sourcesClasspath,
            ideClasspath);
    return Option.some(module);
  }

  private Option<String> getMainClass(JavaTargetInfo javaTargetInfo) {
    return javaTargetInfo.getMainClass().isBlank()
        ? Option.none()
        : Option.some(javaTargetInfo.getMainClass());
  }

  private Jdk getJdk() {
    return jdk.getOrElseThrow(() -> new RuntimeException("Failed to resolve JDK for project"));
  }

  private List<URI> resolveIdeClasspath(List<URI> runtimeClasspath, List<URI> compileClasspath) {
    return new IdeClasspathResolver(runtimeClasspath, compileClasspath).resolve();
  }

  @Override
  public Set<URI> dependencySources(TargetInfo targetInfo) {
    if (!targetInfo.hasJavaTargetInfo()) {
      return HashSet.empty();
    }

    var allSourceJars =
        Stream.concat(
                targetInfo.getJavaTargetInfo().getJarsList().stream(),
                targetInfo.getJavaTargetInfo().getGeneratedJarsList().stream())
            .flatMap(outputs -> outputs.getSourceJarsList().stream());

    return HashSet.ofAll(allSourceJars).map(bazelPathsResolver::resolveUri);
  }

  @Override
  protected void applyModuleData(JavaModule javaModule, BuildTarget buildTarget) {
    JvmBuildTarget jvmBuildTarget = toJvmBuildTarget(javaModule);
    buildTarget.setDataKind(BuildTargetDataKind.JVM);
    buildTarget.setData(jvmBuildTarget);
  }

  public JvmBuildTarget toJvmBuildTarget(JavaModule javaModule) {
    var jdk = javaModule.jdk();
    var javaHome = jdk.javaHome().map(URI::toString).getOrNull();
    return new JvmBuildTarget(javaHome, jdk.javaVersion());
  }

  public JvmEnvironmentItem toJvmEnvironmentItem(Module module, JavaModule javaModule) {
    return new JvmEnvironmentItem(
        toBspId(module),
        javaModule.runtimeClasspath().map(URI::toString).asJava(),
        javaModule.jvmOps().asJava(),
        bazelData.getWorkspaceRoot(),
        environment);
  }

  public JavacOptionsItem toJavacOptionsItem(Module module, JavaModule javaModule) {
    return new JavacOptionsItem(
        toBspId(module),
        javaModule.javacOpts().toJavaList(),
        javaModule.ideClasspath().map(URI::toString).toJavaList(),
        javaModule.mainOutput().toString());
  }
}
