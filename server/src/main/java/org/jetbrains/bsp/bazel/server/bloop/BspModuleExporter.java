package org.jetbrains.bsp.bazel.server.bloop;

import static org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList;
import static org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toList;
import static org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toOption;

import bloop.config.Config;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

class BspModuleExporter {
  private static final Set<String> BAD_JAVAC_OPTS =
      HashSet.of(
          "-XepAllErrorsAsWarnings", "-Xep:PreconditionsInvalidPlaceholder:OFF", "-Werror:-path");

  private final Project project;
  private final Module module;
  private final Path bloopRoot;
  private final ClasspathRewriter classpathRewriter;
  private final SourceSetRewriter sourceSetRewriter;
  private final List<String> extraJvmOptions;
  private final Option<ScalaModule> defaultScalaModule;

  public BspModuleExporter(
      Project project,
      Module module,
      Path bloopRoot,
      ClasspathRewriter classpathRewriter,
      SourceSetRewriter sourceSetRewriter,
      Option<ScalaModule> defaultScalaModule) {
    this.project = project;
    this.module = module;
    this.bloopRoot = bloopRoot;
    this.classpathRewriter = classpathRewriter;
    this.sourceSetRewriter = sourceSetRewriter;
    this.extraJvmOptions =
        List.of("-Duser.dir=" + Paths.get(project.workspaceRoot()).toAbsolutePath());
    this.defaultScalaModule = defaultScalaModule;
  }

  public Config.Project export() {
    if (module.tags().contains(Tag.NO_BUILD)) {
      return createNoBuildModule();
    }

    String name = Naming.safeName(module.label());
    Path directory = Paths.get(module.baseDirectory());
    var adjustedSourceSet = sourceSetRewriter.rewrite(module.sourceSet());
    var workspaceDir = project.workspaceRoot();
    var sourceRoots = toList(adjustedSourceSet.sourceRoots());
    var reGlobbed = ReGlobber.reGlob(module.baseDirectory(), adjustedSourceSet);

    var dependencies =
        toList(
            module.directDependencies().filter(this::isIncludedDependency).map(Naming::safeName));
    var compileClassPath =
        module
            .languageData()
            .flatMap(this::extractCompileClassPathFromLanguage)
            .map(classpathRewriter::rewrite)
            .getOrElse(List.empty());
    var out = bloopRoot.resolve(Naming.compilerOutputNameFor(module.label()));
    var classesDir = out.resolve("classes");
    var resources = module.resources().iterator().map(SourceRootGuesser::getSourcesRoot).toSet();
    var scalaMod = module.languageData().flatMap(this::createScalaConfig);
    var javaMod = module.languageData().flatMap(this::createJavaConfig);
    var platform = module.languageData().flatMap(this::createPlatform);
    var resolution = createResolution();
    var testFramework = createTestFramework();
    var tags = toBloopTags(module.tags());

    var bloopProject =
        new Config.Project(
            name,
            directory,
            scala.Option.apply(Paths.get(workspaceDir)),
            reGlobbed.sources,
            toOption(reGlobbed.globs),
            scala.Option.apply(sourceRoots.map(Paths::get)),
            dependencies,
            toList(compileClassPath),
            out,
            classesDir,
            scala.Option.apply(toList(resources.map(Paths::get))),
            toOption(scalaMod),
            toOption(javaMod),
            scala.Option.empty(),
            toOption(testFramework),
            toOption(platform),
            scala.Option.apply(resolution),
            scala.Option.apply(toList(tags)));

    return bloopProject;
  }

  private Config.Project createNoBuildModule() {
    var out = bloopRoot.resolve(Naming.compilerOutputNameFor(module.label()));
    var classesDir = out.resolve("classes");

    var resources = module.resources();
    return new Config.Project(
        module.label().toString(),
        Paths.get(module.baseDirectory()),
        scala.Option.apply(Paths.get(project.workspaceRoot())),
        emptyList(),
        scala.Option.empty(),
        scala.Option.empty(),
        emptyList(),
        emptyList(),
        out,
        classesDir,
        scala.Option.apply(toList(resources.map(Paths::get))),
        scala.Option.empty(),
        scala.Option.empty(),
        scala.Option.empty(),
        scala.Option.empty(),
        scala.Option.empty(),
        scala.Option.empty(),
        scala.Option.empty());
  }

  private Option<Seq<URI>> extractCompileClassPathFromLanguage(LanguageData languageData) {
    var javaModule = JavaModule.fromLanguageData(languageData);
    return javaModule.map(JavaModule::compileClasspath);
  }

  private boolean isIncludedDependency(Label label) {
    return project.findModule(label).isDefined();
  }

  private Option<Config.Test> createTestFramework() {
    if (!module.tags().contains(Tag.TEST)) {
      return Option.none();
    }

    var framework =
        new Config.TestFramework(
            toList(Collections.singleton("munit.internal.junitinterface.PantsFramework")));
    return Option.of(
        new Config.Test(
            toList(Collections.singleton(framework)),
            new Config.TestOptions(emptyList(), emptyList())));
  }

  private Set<String> toBloopTags(Set<Tag> tags) {
    if (tags.contains(Tag.TEST)) {
      return HashSet.of("test");
    } else {
      return HashSet.of("library");
    }
  }

  private Option<Config.Java> createJavaConfig(LanguageData languageData) {
    var javaModule = JavaModule.fromLanguageData(languageData);
    return javaModule.map(mod -> new Config.Java(toList(sanitizeJavacOpts(mod.javacOpts()))));
  }

  private Seq<String> sanitizeJavacOpts(Seq<String> opts) {
    return opts.filter(p -> !BAD_JAVAC_OPTS.contains(p));
  }

  private Option<Config.Scala> createScalaConfig(LanguageData data) {
    if (!(data instanceof ScalaModule)) {
      return defaultScalaModule.map(this::createScalaConfig);
    }

    var scalaMod = (ScalaModule) data;
    return Option.of(createScalaConfig(scalaMod));
  }

  private Config.Scala createScalaConfig(ScalaModule scalaModule) {
    return new Config.Scala(
        scalaModule.sdk().organization(),
        "scala-compiler",
        scalaModule.sdk().version(),
        toList(scalaModule.scalacOpts()),
        toList(scalaModule.sdk().compilerJars().map(Paths::get)),
        scala.Option.empty(),
        scala.Some.apply(
            Config.CompileSetup$.MODULE$.apply(
                Config.Mixed$.MODULE$, true, false, false, true, true)));
  }

  private Option<Config.Platform> createPlatform(LanguageData languageData) {
    var javaModule = JavaModule.fromLanguageData(languageData);
    return javaModule.map(
        mod -> {
          var runtimeJdk = mod.runtimeJdk().getOrElse(mod.jdk());
          var jvmConfig =
              new Config.JvmConfig(
                  toOption(runtimeJdk.javaHome().map(Paths::get)),
                  toList(mod.jvmOps().prependAll(extraJvmOptions)));
          var runtimeClassPath = toList(classpathRewriter.rewrite(mod.runtimeClasspath()));

          return new bloop.config.Config$Platform$Jvm(
              jvmConfig,
              toOption(mod.mainClass()),
              scala.Some.apply(jvmConfig),
              scala.Some.apply(runtimeClassPath),
              scala.Option.empty());
        });
  }

  private Config.Resolution createResolution() {
    var resolutionModules =
        module
            .sourceDependencies()
            .map(
                sourceDep -> {
                  var artifact =
                      new Config.Artifact(
                          "",
                          scala.Option.apply("sources"),
                          scala.Option.empty(),
                          Paths.get(sourceDep));
                  return new Config.Module(
                      "", "", "", scala.Option.empty(), toList(Collections.singleton(artifact)));
                });
    return new Config.Resolution(toList(resolutionModules));
  }
}
