package org.jetbrains.bsp.bazel.server.sync.languages.java;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JvmBuildTarget;
import ch.epfl.scala.bsp4j.JvmEnvironmentItem;
import io.vavr.collection.Array;
import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JavaTargetInfo;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.model.Module;

public class JavaLanguagePlugin extends LanguagePlugin<JavaModule> {
  private final BazelPathsResolver bazelPathsResolver;
  private final BazelInfo bazelInfo;
  private final java.util.Map<String, String> environment = System.getenv();
  private Option<Jdk> jdk;

  private JdkResolver jdkResolver;

  public JavaLanguagePlugin(BazelPathsResolver bazelPathsResolver, BazelInfo bazelInfo) {
    this.bazelPathsResolver = bazelPathsResolver;
    this.bazelInfo = bazelInfo;
    this.jdkResolver = new JdkResolver(bazelPathsResolver);
  }

  @Override
  public void prepareSync(Seq<TargetInfo> targets) {
    this.jdk = jdkResolver.resolve(targets);
  }

  @Override
  public Option<JavaModule> resolveModule(TargetInfo targetInfo) {
    if (!targetInfo.hasJavaTargetInfo()) {
      return Option.none();
    }

    var javaTargetInfo = targetInfo.getJavaTargetInfo();
    var javacOpts = Array.ofAll(javaTargetInfo.getJavacOptsList());
    var jvmOpts = Array.ofAll(javaTargetInfo.getJvmFlagsList());
    var mainOutput = bazelPathsResolver.resolve(javaTargetInfo.getJars(0).getBinaryJars(0));
    var mainClass = getMainClass(javaTargetInfo);
    var args = Array.ofAll(javaTargetInfo.getArgsList());
    var runtimeClasspath =
        bazelPathsResolver.resolvePaths(javaTargetInfo.getRuntimeClasspathList());
    var compileClasspath =
        bazelPathsResolver.resolvePaths(javaTargetInfo.getCompileClasspathList());
    var sourcesClasspath = bazelPathsResolver.resolvePaths(javaTargetInfo.getSourceClasspathList());
    var ideClasspath = resolveIdeClasspath(runtimeClasspath, compileClasspath);
    var runtimeJdk = jdkResolver.resolveJdk(targetInfo).flatMap(JdkResolver.JdkCandidate::asJdk);

    var module =
        new JavaModule(
            getJdk(),
            runtimeJdk,
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

  private Seq<Path> resolveIdeClasspath(Seq<Path> runtimeClasspath, Seq<Path> compileClasspath) {
    return new IdeClasspathResolver(runtimeClasspath, compileClasspath).resolve();
  }

  @Override
  public Set<Path> dependencySources(TargetInfo targetInfo, DependencyTree dependencyTree) {
    if (!targetInfo.hasJavaTargetInfo()) {
      return HashSet.empty();
    }
    var sourceJars = targetInfo.getJavaTargetInfo().getSourceClasspathList();
    return HashSet.ofAll(sourceJars).map(bazelPathsResolver::resolve);
  }

  @Override
  protected void applyModuleData(JavaModule javaModule, BuildTarget buildTarget) {
    JvmBuildTarget jvmBuildTarget = toJvmBuildTarget(javaModule);
    buildTarget.setDataKind(BuildTargetDataKind.JVM);
    buildTarget.setData(jvmBuildTarget);
  }

  public JvmBuildTarget toJvmBuildTarget(JavaModule javaModule) {
    var jdk = javaModule.jdk();
    var javaHome = jdk.javaHome().map(Path::toString).getOrNull();
    return new JvmBuildTarget(javaHome, jdk.javaVersion());
  }

  public JvmEnvironmentItem toJvmEnvironmentItem(Module module, JavaModule javaModule) {
    return new JvmEnvironmentItem(
        toBspId(module),
        javaModule.runtimeClasspath().map(Path::toString).asJava(),
        javaModule.jvmOps().asJava(),
        bazelInfo.getWorkspaceRoot().toString(),
        environment);
  }

  public JavacOptionsItem toJavacOptionsItem(Module module, JavaModule javaModule) {
    return new JavacOptionsItem(
        toBspId(module),
        javaModule.javacOpts().toJavaList(),
        javaModule.ideClasspath().map(Path::toString).toJavaList(),
        javaModule.mainOutput().toString());
  }
}
