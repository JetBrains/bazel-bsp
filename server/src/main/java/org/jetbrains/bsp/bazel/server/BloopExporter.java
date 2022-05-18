package org.jetbrains.bsp.bazel.server;

import bloop.config.Config;
import bloop.config.ConfigCodecs;
import bloop.config.package$;
import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.DidChangeBuildTarget;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.ShowMessageParams;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskProgressParams;
import ch.epfl.scala.bsp4j.TaskStartParams;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import io.grpc.ServerBuilder;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collections;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider;
import scala.Some;
import scala.collection.immutable.List$;
import scala.jdk.javaapi.CollectionConverters;

public class BloopExporter {
  private static final Set<String> BAD_JAVAC_OPTS =
      HashSet.of(
          "-XepAllErrorsAsWarnings", "-Xep:PreconditionsInvalidPlaceholder:OFF", "-Werror:-path");
  private static final Set<Path> IGNORED_SOURCES =
      HashSet.of(
          Paths.get(
              "tools/src/main/scala/com/twitter/bazel/resources_workaround/placeholder.scala"));
  private static final SourceSet EMPTY_SOURCE_SET = new SourceSet(HashSet.empty(), HashSet.empty());
  private final BspInfo bspInfo;
  private final WorkspaceContextProvider workspaceContextProvider;
  private final Path workspaceRoot;
  private Map<URI, URI> localArtifacts;
  private final List<String> extraJvmOptions;

  public BloopExporter(
      BspInfo bspInfo, Path workspaceRoot, WorkspaceContextProvider workspaceContextProvider) {
    this.bspInfo = bspInfo;
    this.workspaceContextProvider = workspaceContextProvider;
    this.workspaceRoot = workspaceRoot;
    this.extraJvmOptions = List.of("-Duser.dir=" + workspaceRoot.toAbsolutePath());
  }

  private void initializeClient(
      BazelBspServer.ServerContainer serverContainer, BloopBuildClient client) {
    serverContainer.bspClientLogger.initialize(client);
    var bepServer = new BepServer(client, new DiagnosticsService(serverContainer.bazelInfo));
    serverContainer.compilationManager.setBepServer(bepServer);

    var grpcServer = ServerBuilder.forPort(0).addService(bepServer).build();
    try {
      grpcServer.start();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    serverContainer.bazelRunner.setBesBackendPort(grpcServer.getPort());
  }

  public void export() throws BazelExportFailedException {
    var serverContainer =
        BazelBspServer.makeProjectProvider(
            bspInfo,
            workspaceContextProvider,
            Option.of(this.workspaceRoot),
            Option.of(new NoopProjectStorage()));
    var projectProvider = serverContainer.projectProvider;
    var client = new BloopBuildClient(System.out);
    initializeClient(serverContainer, client);

    Project project = projectProvider.refreshAndGet();

    var failedTargets = client.getFailedTargets();
    var failedTransitiveTargets =
        failedTargets.removeAll(
            project.modules().map(m -> new BuildTargetIdentifier(m.label().getValue())));

    if (failedTransitiveTargets.nonEmpty()) {
      throw new BazelExportFailedException(failedTransitiveTargets);
    }

    serverContainer.bspClientLogger.timed(
        "Exporting to bloop",
        () -> {
          var bloopPath = bspInfo.bspProjectRoot().resolve(".bloop");
          var writtenFiles = writeBloops(project, bloopPath);
          cleanUpBloopDirectory(writtenFiles, bloopPath);
        });
  }

  private Set<URI> artifactFromLanguageData(LanguageData languageData) {
    return javaModuleFromLanguageData(languageData).map(JavaModule::mainOutput).toSet();
  }

  private URI classesOutputForModule(Module mod, Path bloopRoot) {
    return bloopRoot.resolve(makeBloopNameForTarget(mod.label())).resolve("classes").toUri();
  }

  private Set<Path> writeBloops(Project project, Path bloopRoot) {
    java.util.Map<URI, URI> localArtifacts = Maps.newHashMap();
    for (var mod : project.modules()) {
      var moduleOutput = classesOutputForModule(mod, bloopRoot);
      for (var ld : mod.languageData()) {
        for (var art : artifactFromLanguageData(ld)) {
          localArtifacts.put(art, moduleOutput);
        }
      }
    }

    this.localArtifacts = HashMap.ofAll(localArtifacts);

    return project.modules().map(m -> exportModule(project, m, bloopRoot)).toSet();
  }

  private void cleanUpBloopDirectory(Set<Path> expected, Path bloopRoot) {
    try (var listResult = Files.list(bloopRoot)) {
      var existingFiles =
          listResult
              .filter(name -> name.toString().endsWith(".config.json"))
              .collect(HashSet.collector());

      var extraFiles = existingFiles.diff(expected);
      extraFiles.forEach(p -> p.toFile().delete());
    } catch (Exception e) {
      // it's fine
    }
  }

  private Option<JavaModule> javaModuleFromLanguageData(LanguageData languageData) {
    if (languageData instanceof JavaModule) {
      return Option.of((JavaModule) languageData);
    } else if (languageData instanceof ScalaModule) {
      return ((ScalaModule) languageData).javaModule();
    } else {
      return Option.none();
    }
  }

  private Option<Seq<URI>> extractClassPathFromLanguage(LanguageData languageData) {
    var javaModule = javaModuleFromLanguageData(languageData);
    return javaModule.map(JavaModule::compileClasspath);
  }

  private String makeBloopNameForTarget(Label label) {
    try {
      var digest = MessageDigest.getInstance("MD5");
      digest.update(label.toString().getBytes(StandardCharsets.UTF_8));
      return "z_" + BaseEncoding.base16().encode(digest.digest()).substring(0, 12);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Option<Config.Scala> extractScalaModuleFromLanguageData(LanguageData data) {
    if (!(data instanceof ScalaModule)) {
      return Option.none();
    }

    var scalaMod = (ScalaModule) data;
    var config =
        new Config.Scala(
            scalaMod.sdk().organization(),
            "scala-compiler",
            scalaMod.sdk().version(),
            CollectionConverters.asScala(scalaMod.scalacOpts()).toList(),
            CollectionConverters.asScala(scalaMod.sdk().compilerJars().map(Paths::get)).toList(),
            scala.Option.empty(),
            scala.Some.apply(
                Config.CompileSetup$.MODULE$.apply(
                    Config.Mixed$.MODULE$, true, false, false, true, true)));
    return Option.some(config);
  }

  private Seq<String> sanitizeJavacOpts(Seq<String> opts) {
    return opts.filter(p -> !BAD_JAVAC_OPTS.contains(p));
  }

  private Option<Config.Java> extractJavaModuleFromLanguageData(LanguageData languageData) {
    var javaModule = javaModuleFromLanguageData(languageData);
    return javaModule.map(
        mod ->
            new Config.Java(
                CollectionConverters.asScala(sanitizeJavacOpts(mod.javacOpts())).toList()));
  }

  private <T> scala.Option<T> toScalaOption(Option<T> opt) {
    return opt.fold(scala.Option::empty, Some::apply);
  }

  private Seq<Path> fixupClassPath(Seq<URI> input) {
    return input
        .iterator()
        .map(p -> this.localArtifacts.getOrElse(p, p))
        .distinct()
        .map(Paths::get)
        .toArray();
  }

  private Option<Config.Platform> extractPlatformFromLanguageData(LanguageData languageData) {
    var javaModule = javaModuleFromLanguageData(languageData);
    return javaModule.map(
        mod -> {
          var runtimeJdk = mod.runtimeJdk().getOrElse(mod.jdk());
          var jvmConfig =
              new Config.JvmConfig(
                  toScalaOption(runtimeJdk.javaHome().map(Paths::get)),
                  CollectionConverters.asScala(mod.jvmOps().prependAll(extraJvmOptions)).toList());
          var classPath =
              CollectionConverters.asScala(fixupClassPath(mod.runtimeClasspath())).toList();

          return new bloop.config.Config$Platform$Jvm(
              jvmConfig,
              toScalaOption(mod.mainClass()),
              scala.Some.apply(jvmConfig),
              scala.Some.apply(classPath),
              scala.Option.empty());
        });
  }

  private Config.Test bloopTestFramework() {
    var framework =
        new Config.TestFramework(
            CollectionConverters.asScala(
                    Collections.singleton("munit.internal.junitinterface.PantsFramework"))
                .toList());
    return new Config.Test(
        CollectionConverters.asScala(Collections.singleton(framework)).toList(),
        new Config.TestOptions(List$.MODULE$.empty(), List$.MODULE$.empty()));
  }

  private scala.collection.immutable.List<String> convertTags(io.vavr.collection.Set<Tag> tags) {
    Set<String> ret;
    if (tags.contains(Tag.TEST)) {
      ret = HashSet.of("test");
    } else {
      ret = HashSet.of("library");
    }

    return CollectionConverters.asScala(ret).toList();
  }

  private Config.Resolution extractResolution(Module module) {
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
                      "",
                      "",
                      "",
                      scala.Option.empty(),
                      CollectionConverters.asScala(Collections.singleton(artifact)).toList());
                });
    return new Config.Resolution(CollectionConverters.asScala(resolutionModules).toList());
  }

  private <T> scala.collection.immutable.List<T> emptyList() {
    return List$.MODULE$.empty();
  }

  private Option<scala.collection.immutable.List<Config.SourcesGlobs>> reGlob(
      URI baseDirectory, Set<URI> sources) {
    var basePath = Paths.get(baseDirectory);
    var sourcePaths = sources.map(Paths::get);
    var relativeLevels = 0;
    var extensions = new java.util.HashSet<String>();

    for (var s : sourcePaths) {
      var rel = basePath.relativize(s);
      if (!rel.startsWith("..")) {
        relativeLevels = Math.max(relativeLevels, rel.getNameCount());
        var maybeExtension = com.google.common.io.Files.getFileExtension(s.toString());
        if (!maybeExtension.isEmpty()) {
          extensions.add(maybeExtension);
        }
      }
    }
    if (relativeLevels == 0) {
      return Option.none();
    } else {
      scala.Option<Object> walkDepth;
      String globPrefix;
      if (relativeLevels == 1) {
        walkDepth = scala.Option.apply(1);
        globPrefix = "glob:*.";
      } else {
        walkDepth = scala.Option.empty();
        globPrefix = "glob:**.";
      }
      var includes = extensions.stream().map(ext -> globPrefix + ext).iterator();
      var singleGlob =
          new Config.SourcesGlobs(
              basePath, walkDepth, CollectionConverters.asScala(includes).toList(), emptyList());
      return Option.of(CollectionConverters.asScala(List.of(singleGlob)).toList());
    }
  }

  private Path writeNoBuildModule(Project project, Module module, Path bloopRoot) {
    var out = bloopRoot.resolve(makeBloopNameForTarget(module.label()));
    var classesDir = out.resolve("classes");

    var resources = module.resources();
    var bloopProject =
        new Config.Project(
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
            scala.Option.apply(CollectionConverters.asScala(resources.map(Paths::get)).toList()),
            scala.Option.empty(),
            scala.Option.empty(),
            scala.Option.empty(),
            scala.Option.empty(),
            scala.Option.empty(),
            scala.Option.empty(),
            scala.Option.empty());
    return writeBloopProject(bloopProject, module.label(), bloopRoot);
  }

  private Path writeBloopProject(Config.Project bloopProject, Label label, Path bloopRoot) {
    var configFile = new Config.File(Config.File$.MODULE$.LatestVersion(), bloopProject);
    var outputPath = bloopRoot.resolve(safeFileName(label) + ".config.json");
    if (outputPath.toFile().exists()) {
      var configString = ConfigCodecs.toStr(configFile);
      var hasher = Hashing.sha256();
      var newHash = hasher.hashString(configString, StandardCharsets.UTF_8);
      try {
        var existingHash =
            com.google.common.io.Files.asByteSource(outputPath.toFile()).hash(hasher);
        if (!newHash.equals(existingHash)) {
          Files.writeString(outputPath, configString, StandardCharsets.UTF_8);
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      package$.MODULE$.write(configFile, outputPath);
    }
    return outputPath;
  }

  private String safeName(Label label) {
    var labelName = label.getValue();
    if (labelName.startsWith("//")) {
      labelName = labelName.substring(2);
    }
    return labelName;
  }

  private String safeFileName(Label label) {
    return safeName(label).replace('/', '.');
  }

  private boolean isIncludedDependency(Project project, Label label) {
    return project.findModule(label).isDefined();
  }

  private SourceSet adjustSourceSet(SourceSet input) {
    if (input.sources().size() == 1) {
      var singleSource = Paths.get(input.sources().head());
      if (IGNORED_SOURCES.exists(singleSource::endsWith)) {
        return EMPTY_SOURCE_SET;
      }
    }
    return input;
  }

  private Path exportModule(Project project, Module module, Path bloopRoot) {
    if (module.tags().contains(Tag.NO_BUILD)) {
      return writeNoBuildModule(project, module, bloopRoot);
    }

    String name = safeName(module.label());
    Path directory = Paths.get(module.baseDirectory());
    var adjustedSourceSet = adjustSourceSet(module.sourceSet());
    var workspaceDir = project.workspaceRoot();
    var sourceRoots = CollectionConverters.asScala(adjustedSourceSet.sourceRoots()).toList();
    var sourceGlobs = reGlob(module.baseDirectory(), adjustedSourceSet.sources());
    scala.collection.immutable.List<URI> sources;
    if (sourceGlobs.isDefined()) {
      sources = emptyList();
    } else {
      sources = CollectionConverters.asScala(adjustedSourceSet.sources()).toList();
    }

    var dependencies =
        CollectionConverters.asScala(
                module
                    .directDependencies()
                    .filter(label -> isIncludedDependency(project, label))
                    .map(this::safeName))
            .toList();
    var classPath =
        module
            .languageData()
            .flatMap(this::extractClassPathFromLanguage)
            .map(this::fixupClassPath)
            .getOrElse(List.empty());
    var out = bloopRoot.resolve(makeBloopNameForTarget(module.label()));
    var classesDir = out.resolve("classes");
    var resources = module.resources().iterator().map(SourceRootGuesser::getSourcesRoot).toSet();
    var scalaMod = module.languageData().flatMap(this::extractScalaModuleFromLanguageData);
    var javaMod = module.languageData().flatMap(this::extractJavaModuleFromLanguageData);
    var platform = module.languageData().flatMap(this::extractPlatformFromLanguageData);

    var isTest = module.tags().contains(Tag.TEST);
    Option<Config.Test> test;
    if (isTest) {
      test = Option.of(bloopTestFramework());
    } else {
      test = Option.none();
    }

    var resolution = extractResolution(module);
    var tags = convertTags(module.tags());

    var bloopProject =
        new Config.Project(
            name,
            directory,
            scala.Option.apply(Paths.get(workspaceDir)),
            sources.map(Paths::get),
            toScalaOption(sourceGlobs),
            scala.Option.apply(sourceRoots.map(Paths::get)),
            dependencies,
            CollectionConverters.asScala(classPath).toList(),
            out,
            classesDir,
            scala.Option.apply(CollectionConverters.asScala(resources.map(Paths::get)).toList()),
            toScalaOption(scalaMod),
            toScalaOption(javaMod),
            scala.Option.empty(),
            toScalaOption(test),
            toScalaOption(platform),
            scala.Option.apply(resolution),
            scala.Option.apply(tags));

    return writeBloopProject(bloopProject, module.label(), bloopRoot);
  }

  public static class BazelExportFailedException extends RuntimeException {
    private final Set<BuildTargetIdentifier> failedTargets;

    public BazelExportFailedException(Set<BuildTargetIdentifier> failedTargets) {
      this.failedTargets = failedTargets;
    }

    public Set<BuildTargetIdentifier> getFailedTargets() {
      return this.failedTargets;
    }
  }

  private static class BloopBuildClient implements BuildClient {

    private final PrintStream out;
    private final java.util.Set<BuildTargetIdentifier> failedTargets = Sets.newHashSet();

    BloopBuildClient(PrintStream out) {
      this.out = out;
    }

    public Set<BuildTargetIdentifier> getFailedTargets() {
      return HashSet.ofAll(failedTargets);
    }

    @Override
    public void onBuildShowMessage(ShowMessageParams showMessageParams) {}

    @Override
    public void onBuildLogMessage(LogMessageParams logMessageParams) {
      out.println(logMessageParams.getMessage());
    }

    @Override
    public void onBuildTaskStart(TaskStartParams taskStartParams) {}

    @Override
    public void onBuildTaskProgress(TaskProgressParams taskProgressParams) {}

    @Override
    public void onBuildTaskFinish(TaskFinishParams taskFinishParams) {}

    @Override
    public void onBuildPublishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
      if (publishDiagnosticsParams.getDiagnostics().stream()
          .anyMatch(d -> d.getSeverity() == DiagnosticSeverity.ERROR)) {
        this.failedTargets.add(publishDiagnosticsParams.getBuildTarget());
      }
    }

    @Override
    public void onBuildTargetDidChange(DidChangeBuildTarget didChangeBuildTarget) {}
  }

  private static final class NoopProjectStorage implements ProjectStorage {

    @Override
    public Option<Project> load() {
      return Option.none();
    }

    @Override
    public void store(Project project) {}
  }
}
