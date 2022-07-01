package org.jetbrains.bsp.bazel.server.sync.languages.scala;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaMainClass;
import ch.epfl.scala.bsp4j.ScalaMainClassesItem;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import ch.epfl.scala.bsp4j.ScalaTestClassesItem;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.BiFunction;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree;
import org.jetbrains.bsp.bazel.server.sync.languages.JVMLanguagePluginParser;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;
import org.jetbrains.bsp.bazel.server.sync.model.Language;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

public class ScalaLanguagePlugin extends LanguagePlugin<ScalaModule> {
  private final JavaLanguagePlugin javaLanguagePlugin;
  private final BazelPathsResolver bazelPathsResolver;
  private Option<ScalaSdk> scalaSdk;

  public ScalaLanguagePlugin(
      JavaLanguagePlugin javaLanguagePlugin, BazelPathsResolver bazelPathsResolver) {
    this.javaLanguagePlugin = javaLanguagePlugin;
    this.bazelPathsResolver = bazelPathsResolver;
  }

  @Override
  public void prepareSync(Seq<TargetInfo> targets) {
    this.scalaSdk = new ScalaSdkResolver(bazelPathsResolver).resolve(targets);
  }

  @Override
  public Option<ScalaModule> resolveModule(TargetInfo targetInfo) {
    if (!targetInfo.hasScalaTargetInfo()) {
      return Option.none();
    }

    var scalaTargetInfo = targetInfo.getScalaTargetInfo();
    var sdk = getScalaSdk();
    var scalacOpts = Array.ofAll(scalaTargetInfo.getScalacOptsList());
    var module = new ScalaModule(sdk, scalacOpts, javaLanguagePlugin.resolveModule(targetInfo));
    return Option.some(module);
  }

  private ScalaSdk getScalaSdk() {
    return scalaSdk.getOrElseThrow(
        () -> new RuntimeException("Failed to resolve Scala SDK for project"));
  }

  @Override
  public Set<URI> dependencySources(TargetInfo targetInfo, DependencyTree dependencyTree) {
    return javaLanguagePlugin.dependencySources(targetInfo, dependencyTree);
  }

  @Override
  protected void applyModuleData(ScalaModule scalaModule, BuildTarget buildTarget) {
    var sdk = scalaModule.sdk();

    var scalaBuildTarget =
        new ScalaBuildTarget(
            sdk.organization(),
            sdk.version(),
            sdk.binaryVersion(),
            ScalaPlatform.JVM,
            sdk.compilerJars().map(URI::toString).toJavaList());

    scalaModule
        .javaModule()
        .map(javaLanguagePlugin::toJvmBuildTarget)
        .forEach(scalaBuildTarget::setJvmBuildTarget);

    buildTarget.setDataKind(BuildTargetDataKind.SCALA);
    buildTarget.setData(scalaBuildTarget);
  }

  @Override
  public Option<Path> calculateSourceRoot(Path source) {
    return JVMLanguagePluginParser.calculateJVMSourceRoot(source, true);
  }

  public Option<ScalacOptionsItem> toScalacOptionsItem(Module module) {
    return withScalaAndJavaModules(
        module,
        (scalaModule, javaModule) -> {
          var javacOptions = javaLanguagePlugin.toJavacOptionsItem(module, javaModule);
          return new ScalacOptionsItem(
              javacOptions.getTarget(),
              scalaModule.scalacOpts().toJavaList(),
              javacOptions.getClasspath(),
              javacOptions.getClassDirectory());
        });
  }

  public Option<ScalaTestClassesItem> toScalaTestClassesItem(Module module) {
    if (!module.tags().contains(Tag.TEST) || !module.languages().contains(Language.SCALA)) {
      return Option.none();
    }

    return withScalaAndJavaModules(
        module,
        (scalaModule, javaModule) -> {
          var mainClasses = javaModule.mainClass().toJavaList();
          var id = toBspId(module);
          return new ScalaTestClassesItem(id, mainClasses);
        });
  }

  public Option<ScalaMainClassesItem> toScalaMainClassesItem(Module module) {
    if (!module.tags().contains(Tag.APPLICATION) || !module.languages().contains(Language.SCALA)) {
      return Option.none();
    }

    return withScalaAndJavaModulesOpt(
        module,
        (scalaModule, javaModule) ->
            javaModule
                .mainClass()
                .map(
                    mainClass -> {
                      var id = toBspId(module);
                      var args = javaModule.args().asJava();
                      var jvmOpts = javaModule.jvmOps().asJava();
                      var scalaMainClass = new ScalaMainClass(mainClass, args, jvmOpts);
                      var mainClasses = Collections.singletonList(scalaMainClass);
                      return new ScalaMainClassesItem(id, mainClasses);
                    }));
  }

  private <T> Option<T> withScalaAndJavaModules(
      Module module, BiFunction<ScalaModule, JavaModule, T> f) {
    return getScalaAndJavaModules(module).map(t -> t.apply(f));
  }

  private <T> Option<T> withScalaAndJavaModulesOpt(
      Module module, BiFunction<ScalaModule, JavaModule, Option<T>> f) {
    return getScalaAndJavaModules(module).flatMap(t -> t.apply(f));
  }

  private Option<Tuple2<ScalaModule, JavaModule>> getScalaAndJavaModules(Module module) {
    return module
        .languageData()
        .flatMap(m -> m instanceof ScalaModule ? Option.some((ScalaModule) m) : Option.none())
        .flatMap(
            scalaModule ->
                scalaModule.javaModule().map(javaModule -> new Tuple2<>(scalaModule, javaModule)));
  }
}
