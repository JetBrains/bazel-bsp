package com.illicitonion.bazelbsp;

import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.BuildServerCapabilities;
import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CleanCacheParams;
import ch.epfl.scala.bsp4j.CleanCacheResult;
import ch.epfl.scala.bsp4j.CompileParams;
import ch.epfl.scala.bsp4j.CompileProvider;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.DependencySourcesItem;
import ch.epfl.scala.bsp4j.DependencySourcesParams;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InitializeBuildParams;
import ch.epfl.scala.bsp4j.InitializeBuildResult;
import ch.epfl.scala.bsp4j.InverseSourcesParams;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.RunParams;
import ch.epfl.scala.bsp4j.RunResult;
import ch.epfl.scala.bsp4j.ScalaBuildServer;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourceItemKind;
import ch.epfl.scala.bsp4j.SourcesItem;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TestParams;
import ch.epfl.scala.bsp4j.TestResult;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BazelBspServer implements BuildServer, ScalaBuildServer {

  private final String bazel;

  private final Map<BuildTargetIdentifier, List<SourceItem>> targetsToSources = new HashMap<>();
  private String execRoot = null;
  private String workspaceRoot = null;

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
    return CompletableFuture.completedFuture(
        new WorkspaceBuildTargetsResult(
            runBazelLines("query", "//...").stream()
                .map(line -> getBuildTarget(new BuildTargetIdentifier(line)))
                .collect(Collectors.toList())));
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
      } else if (source.getUri().endsWith(".java")) {
        extensions.add("java");
      }
    }
    BuildTarget target =
        new BuildTarget(
            label,
            new ArrayList<>(),
            new ArrayList<>(extensions),
            deps,
            new BuildTargetCapabilities(true, false, false));
    target.setBaseDirectory(Uri.packageDirFromLabel(label.getUri(), getWorkspaceRoot()).toString());
    target.setDisplayName(label.getUri());
    target.setLanguageIds(Lists.newArrayList("scala"));
    // TODO: Parse this out of the toolchain or something
    target.setDataKind("scala");
    target.setTags(Lists.newArrayList("library"));
    target.setData(new ScalaBuildTarget("org.scala-lang", "2.12.10", "2.12", ScalaPlatform.JVM, Lists.newArrayList()));
    return target;
  }

  private List<String> runBazelLines(String... args) {
    List<String> lines =
        Splitter.on("\n").omitEmptyStrings().splitToList(new String(runBazelBytes(args), StandardCharsets.UTF_8));
    System.out.printf("Returning: %s%n", lines);
    return lines;
  }

  private byte[] runBazelBytes(String... args) {
    try {
      List<String> argv = new ArrayList<>(args.length + 1);
      argv.add(bazel);
      for (String arg : args) {
        argv.add(arg);
      }
      System.out.printf("Running: %s%n", argv);
      Process process = new ProcessBuilder(argv).start();

      return ByteStreams.toByteArray(process.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
      System.out.printf("Returning: %s%n", output);
      return output;
    } catch (IOException e) {
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
    System.out.printf("DWH: Got buildTargetResources: %s%n", resourcesParams);
    return null;
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams compileParams) {
    List<String> args =
        Lists.newArrayList(
            bazel,
            "build",
            "--bes_backend=grpc://localhost:5001",
            "--bes_lifecycle_events",
            "--build_event_publish_all_actions");
    args.addAll(
        compileParams.getTargets().stream()
            .map(target -> target.getUri())
            .collect(Collectors.toList()));
    int exitCode = -1;
    try {
      Process process = new ProcessBuilder(args).start();
      exitCode = process.waitFor();
    } catch (InterruptedException | IOException e) {
      System.out.println("Failed to run bazel: " + e);
    }
    return CompletableFuture.completedFuture(
        new CompileResult(exitCode == 0 ? StatusCode.OK : StatusCode.ERROR));
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
                  "mnemonic(Scalac, " + Joiner.on(" + ").join(targets) + ")"));
      ActionGraphParser parser = new ActionGraphParser(actionGraph);
      return CompletableFuture.completedFuture(
          new ScalacOptionsResult(
              targets.stream()
                  .map(target -> collectScalacOptionsResult(parser, options, getExecRoot(), target))
                  .collect(Collectors.toList())));
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }

  private ScalacOptionsItem collectScalacOptionsResult(
      ActionGraphParser actionGraphParser,
      ArrayList<String> options,
      String execRoot,
      String target) {
    return new ScalacOptionsItem(
        new BuildTargetIdentifier(target),
        options,
        actionGraphParser.getInputs(target, ".jar").stream()
            .map(exec_path -> Uri.fromExecPath(exec_path, execRoot).toString())
            .collect(Collectors.toList()),
            // TODO: Handle multiple outputs
            Uri.fromExecPath("exec-root://" + Iterables.getOnlyElement(actionGraphParser.getOutputs(target, ".jar")), execRoot).toString());
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
}
