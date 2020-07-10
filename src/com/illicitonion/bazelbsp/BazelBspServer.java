package com.illicitonion.bazelbsp;

import ch.epfl.scala.bsp4j.*;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.devtools.build.lib.analysis.AnalysisProtos;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BazelBspServer implements BuildServer, ScalaBuildServer {

  private final String bazel;
  private final String BES_BACKEND = "--bes_backend=grpc://localhost:5001";
  private final String PUBLISH_ALL_ACTIONS = "--build_event_publish_all_actions";

  private final Map<BuildTargetIdentifier, List<SourceItem>> targetsToSources = new HashMap<>();

  public BepServer bepServer = null;
  private String execRoot = null;
  private String workspaceRoot = null;
  private ScalaBuildTarget scalacClasspath = null;
  private BuildClient buildClient;
  private boolean isScalaProject = false;

  public BazelBspServer(String pathToBazel) {
    this.bazel = pathToBazel;
  }

  @Override
  public CompletableFuture<InitializeBuildResult> buildInitialize(
      InitializeBuildParams initializeBuildParams) {
    BuildServerCapabilities capabilities = new BuildServerCapabilities();
    capabilities.setCompileProvider(new CompileProvider(Lists.newArrayList("scala", "java")));
    capabilities.setDependencySourcesProvider(true);
    capabilities.setInverseSourcesProvider(true);
    return CompletableFuture.completedFuture(
        new InitializeBuildResult(
            Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities));
  }

  @Override
  public void onBuildInitialized() {}

  @Override
  public CompletableFuture<Object> buildShutdown() {
    return CompletableFuture.completedFuture(new Object());
  }

  @Override
  public void onBuildExit() {}

  @Override
  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    // TODO: Parameterise this to allow importing a subset of //...
    // TODO: Run one batch query outputting a proto, rather than one per target
    try{
      return CompletableFuture.completedFuture(
          new WorkspaceBuildTargetsResult(
              runBazelLines("query", "//...").stream()
                  .map(line -> getBuildTarget(new BuildTargetIdentifier(line)))
                  .collect(Collectors.toList())));

    }catch (RuntimeException e){
      CompletableFuture<WorkspaceBuildTargetsResult> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
  }

  private BuildTarget getBuildTarget(BuildTargetIdentifier label) {
    List<BuildTargetIdentifier> deps =
        runBazelLines(
                "query",
                "--nohost_deps",
                "--noimplicit_deps",
                "kind(rule, deps(" + label.getUri() + ", 1))")
            .stream()
            .filter(l -> !l.equals(label.getUri()))
            .map(BuildTargetIdentifier::new)
            .collect(Collectors.toList());
    SortedSet<String> extensions = new TreeSet<>();
    for (SourceItem source : getSourceItems(label)) {
      if (source.getUri().endsWith(".scala")) {
        extensions.add("scala");
        isScalaProject = true;
      } else if (source.getUri().endsWith(".java")) {
        extensions.add("java");
      }
    }
    //TODO: Remove this whenever java binaries are natively supported
    extensions.add("scala");

    BuildTarget target =
        new BuildTarget(
            label,
            new ArrayList<>(),
            new ArrayList<>(extensions),
            deps,
            new BuildTargetCapabilities(true, false, false));
    target.setBaseDirectory(Uri.packageDirFromLabel(label.getUri(), getWorkspaceRoot()).toString());
    target.setDisplayName(label.getUri());
    if(extensions.contains("scala")){
      getScalaBuildTarget().ifPresent((buildTarget) -> {
        target.setDataKind("scala");
        target.setTags(Lists.newArrayList("library"));
        target.setData(buildTarget);
      });
    }
    return target;
  }

  private Optional<ScalaBuildTarget> getScalaBuildTarget() {
    if (scalacClasspath == null) {
      // Force-populate cache to avoid deadlock when looking up execRoot from BEP listener.
      getExecRoot();
      buildTargetsWithBep(
        Lists.newArrayList(
          new BuildTargetIdentifier("@io_bazel_rules_scala_scala_library//:io_bazel_rules_scala_scala_library"),
          new BuildTargetIdentifier("@io_bazel_rules_scala_scala_reflect//:io_bazel_rules_scala_scala_reflect"),
          new BuildTargetIdentifier("@io_bazel_rules_scala_scala_compiler//:io_bazel_rules_scala_scala_compiler")
        ),
        Lists.newArrayList("--aspects=@bazel_bsp//:aspects.bzl%scala_compiler_classpath_aspect", "--output_groups=scala_compiler_classpath_files")
      );
      List<String> classpath = bepServer.fetchScalacClasspath().stream().map(Uri::toString).collect(Collectors.toList());
      List<String> scalaVersions = classpath.stream().filter(uri -> uri.contains("scala-library")).collect(Collectors.toList());
      if(scalaVersions.size() != 1)
        return Optional.empty();
      String scalaVersion = scalaVersions.get(0).substring(scalaVersions.get(0).indexOf("scala-library-") + 14, scalaVersions.get(0).indexOf(".jar"));
      scalacClasspath = new ScalaBuildTarget("org.scala-lang",
              scalaVersion,  scalaVersion.substring(0, scalaVersion.lastIndexOf(".")), ScalaPlatform.JVM, classpath);
    }
    return Optional.of(scalacClasspath);
  }

  private List<String> runBazelLines(String... args) {
    List<String> lines =
        Splitter.on("\n").omitEmptyStrings().splitToList(new String(runBazelBytes(args), StandardCharsets.UTF_8));
    System.out.printf("Returning: %s%n", lines);
    return lines;
  }

  private byte[] runBazelBytes(String... args) {
    try {
      List<String> argv = new ArrayList<>(args.length + 3);
      argv.add(bazel);
      for (String arg : args) {
        argv.add(arg);
      }
      if(argv.size() > 1){
        argv.add(2, BES_BACKEND);
        argv.add(3, PUBLISH_ALL_ACTIONS);
      }

      System.out.printf("Running: %s%n", argv);
      Process process = new ProcessBuilder(argv).start();
      parseProcess(process);

      return ByteStreams.toByteArray(process.getInputStream());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private int parseProcess(Process process) throws IOException, InterruptedException {
    Set<String> messageBuilder = new HashSet<>();
    String line;
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    while ((line = reader.readLine()) != null)
      messageBuilder.add(line.trim());

    String message = String.join("\n", messageBuilder);
    int returnCode = process.waitFor();
    if(returnCode != 0)
      logError(message);
    else
      logMessage(message);
    return returnCode;
  }

  private void logError(String errorMessage) {
    LogMessageParams params = new LogMessageParams(MessageType.ERROR, errorMessage);
    buildClient.onBuildLogMessage(params);
    throw new RuntimeException(errorMessage);
  }

  private void logMessage(String message) {
    LogMessageParams params = new LogMessageParams(MessageType.LOG, message);
    buildClient.onBuildLogMessage(params);
  }

  @Override
  public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams sourcesParams) {
    // TODO: Use proto output of query, rather than per-target queries
    return CompletableFuture.completedFuture(
      new SourcesResult(
        sourcesParams.getTargets().stream()
          .map(target -> new SourcesItem(target, getSourceItems(target)))
          .collect(Collectors.toList())));
  }

  private List<SourceItem> getSourceItems(BuildTargetIdentifier label) {
    // TODO: Use proto output of query, rather than two queries
    String workspaceRoot = getWorkspaceRoot();

    List<SourceItem> sources =
        runBazelLines(
                "query",
                "--nohost_deps",
                "--noimplicit_deps",
                "kind(\"source file\", deps(" + label.getUri() + ", 1))")
            .stream()
            .map(fileLabel -> Uri.fromFileLabel(fileLabel, workspaceRoot).toString())
            .map(uri -> new SourceItem(uri, SourceItemKind.FILE, false))
            .collect(Collectors.toList());

    sources.addAll(
        runBazelLines(
                "query",
                "--nohost_deps",
                "--noimplicit_deps",
                "kind(\"generated file\", deps(" + label.getUri() + ", 1))")
            .stream()
            .map(fileLabel -> Uri.fromFileLabel(fileLabel, workspaceRoot).toString())
            .map(uri -> new SourceItem(uri, SourceItemKind.FILE, true))
            .collect(Collectors.toList()));

    targetsToSources.put(label, sources);

    return sources;
  }

  public synchronized String getWorkspaceRoot() {
    if (workspaceRoot == null) {
      workspaceRoot = Iterables.getOnlyElement(runBazelLines("info", "workspace"));
    }
    return workspaceRoot;
  }

  public synchronized String getExecRoot() {
    if (execRoot == null) {
      execRoot = Iterables.getOnlyElement(runBazelLines("info", "execution_root"));
    }
    return execRoot;
  }

  @Override
  public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams inverseSourcesParams) {
    String fileUri = inverseSourcesParams.getTextDocument().getUri();
    String workspaceRoot = getWorkspaceRoot();
    String prefix = Uri.fromWorkspacePath(getWorkspaceRoot(), "").toString();
    if (!inverseSourcesParams.getTextDocument().getUri().startsWith(prefix)) {
      throw new RuntimeException(
          "Could not resolve " + fileUri + " within workspace " + workspaceRoot);
    }
    List<String> targets =
        runBazelLines(
            "query", "kind(rule, rdeps(//..., " + fileUri.substring(prefix.length()) + ", 1))");
    return CompletableFuture.completedFuture(
        new InverseSourcesResult(
            targets.stream().map(BuildTargetIdentifier::new).collect(Collectors.toList())));
  }

  @Override
  public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams dependencySourcesParams) {
    List<String> targets =
        dependencySourcesParams.getTargets().stream()
            .map(BuildTargetIdentifier::getUri)
            .collect(Collectors.toList());

    return CompletableFuture.completedFuture(
        new DependencySourcesResult(
            targets.stream()
                .map(
                    target -> {
                      List<String> files =
                          lookupTransitiveSourceJars(target).stream()
                              .map(execPath -> Uri.fromExecPath(execPath, getExecRoot()).toString())
                              .collect(Collectors.toList());
                      return new DependencySourcesItem(new BuildTargetIdentifier(target), files);
                    })
                .collect(Collectors.toList())));
  }

  private List<String> runBazelStderr(String... args) {
    try {
      List<String> argv = new ArrayList<>(args.length + 1);
      argv.add(bazel);
      for (String arg : args) {
        argv.add(arg);
      }
      System.out.printf("Running: %s%n", argv);
      Process process = new ProcessBuilder(argv).start();
      List<String> output = new ArrayList<>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        output.add(line.trim());
      }

      if(process.waitFor() != 0)
        logError(String.join("\n", output));
      else
        logMessage(String.join("\n", output));
      System.out.printf("Returning: %s%n", output);
      return output;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  private List<String> lookupTransitiveSourceJars(String target) {
    // TODO: Use an aspect output group, rather than parsing stderr logging
    List<String> lines =
        runBazelStderr("build", "--aspects", "@bazel_bsp//:aspects.bzl%print_aspect", target);
    return lines.stream()
        .map(line -> Splitter.on(" ").splitToList(line))
        .filter(
            parts ->
                parts.size() == 3
                    && parts.get(0).equals("DEBUG:")
                    && parts.get(1).contains("external/bazel_bsp/aspects.bzl")
                    && parts.get(2).endsWith(".jar"))
        .map(parts -> "exec-root://" + parts.get(2))
        .collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams resourcesParams) {
    System.out.printf("DWH: Got buildTargetResources: %s - responding with stub empty reply%n", resourcesParams);
    return CompletableFuture.completedFuture(new ResourcesResult(new ArrayList<>()));
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams compileParams) {
    return buildTargetsWithBep(compileParams.getTargets(), new ArrayList<>());
  }

  private CompletableFuture<CompileResult> buildTargetsWithBep(List<BuildTargetIdentifier> targets, List<String> extraFlags) {
    List<String> args = Lists.newArrayList(
      bazel,
      "build",
      BES_BACKEND,
      PUBLISH_ALL_ACTIONS
    );
    args.addAll(
            targets.stream()
                    .map(BuildTargetIdentifier::getUri)
                    .collect(Collectors.toList()));
    args.addAll(extraFlags);
    int exitCode = -1;
    try {
      Process process = new ProcessBuilder(args).start();
      System.out.println("Building targets....");
      exitCode = parseProcess(process);
    } catch (InterruptedException | IOException e) {
      System.out.println("Failed to run bazel: " + e);
    }
    return CompletableFuture.completedFuture(new CompileResult(BepServer.convertExitCode(exitCode)));
  }

  @Override
  public CompletableFuture<TestResult> buildTargetTest(TestParams testParams) {
    System.out.printf("DWH: Got buildTargetTest: %s%n", testParams);
    return null;
  }

  @Override
  public CompletableFuture<RunResult> buildTargetRun(RunParams runParams) {
    System.out.printf("DWH: Got buildTargetRun: %s%n", runParams);
    return null;
  }

  @Override
  public CompletableFuture<CleanCacheResult> buildTargetCleanCache(
      CleanCacheParams cleanCacheParams) {
    System.out.printf("DWH: Got buildTargetCleanCache: %s%n", cleanCacheParams);
    return null;
  }

  @Override
  public CompletableFuture<ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {
    // TODO: Parse nested source roots out properly
    // TODO: Generate SemanticDBs somehow
    ArrayList<String> options = Lists.newArrayList();

    // TODO: Support non-scala deps
    List<String> targets =
        scalacOptionsParams.getTargets().stream()
            .map(BuildTargetIdentifier::getUri)
            .collect(Collectors.toList());
    try {
      AnalysisProtos.ActionGraphContainer actionGraph =
          AnalysisProtos.ActionGraphContainer.parseFrom(
              runBazelBytes(
                  "aquery",
                  "--output=proto",
                  "mnemonic(" + (isScalaProject ? "Scalac" :  "Javac") + ", " + Joiner.on(" + ").join(targets) + ")"));
      ActionGraphParser parser = new ActionGraphParser(actionGraph);
      return CompletableFuture.completedFuture(
          new ScalacOptionsResult(
              targets.stream()
                  .flatMap(target -> collectScalacOptionsResult(parser, options, getExecRoot(), target))
                  .collect(Collectors.toList())));
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }

  private Stream<ScalacOptionsItem> collectScalacOptionsResult(
      ActionGraphParser actionGraphParser,
      ArrayList<String> options,
      String execRoot,
      String target) {

    List<String> inputs = actionGraphParser.getInputs(target, ".jar").stream()
            .map(exec_path -> Uri.fromExecPath(exec_path, execRoot).toString())
            .collect(Collectors.toList());
    return actionGraphParser.getOutputs(target, ".jar")
            .stream().map(output ->
                    new ScalacOptionsItem(
                            new BuildTargetIdentifier(target),
                            options,
                            inputs,
                            Uri.fromExecPath("exec-root://" + output, execRoot).toString())
            );
  }

  @Override
  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaTestClasses: %s%n", scalaTestClassesParams);
    // TODO: Populate
    return CompletableFuture.completedFuture(new ScalaTestClassesResult(new ArrayList<>()));
  }

  @Override
  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    System.out.printf("DWH: Got buildTargetScalaMainClasses: %s%n", scalaMainClassesParams);
    // TODO: Populate
    return CompletableFuture.completedFuture(new ScalaMainClassesResult(new ArrayList<>()));
  }

  public Iterable<SourceItem> getCachedBuildTargetSources(BuildTargetIdentifier target) {
    return targetsToSources.getOrDefault(target, new ArrayList<>());
  }

  public void setBuildClient(BuildClient buildClient) {
    this.buildClient = buildClient;
  }
}
