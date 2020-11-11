package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildServer;
import ch.epfl.scala.bsp4j.BuildServerCapabilities;
import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.BuildTargetTag;
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
import ch.epfl.scala.bsp4j.JavaBuildServer;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import ch.epfl.scala.bsp4j.JvmBuildTarget;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.ResourcesItem;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.RunParams;
import ch.epfl.scala.bsp4j.RunProvider;
import ch.epfl.scala.bsp4j.RunResult;
import ch.epfl.scala.bsp4j.ScalaBuildServer;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourceItemKind;
import ch.epfl.scala.bsp4j.SourcesItem;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TestParams;
import ch.epfl.scala.bsp4j.TestProvider;
import ch.epfl.scala.bsp4j.TestResult;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.common.Uri;
import org.jetbrains.bsp.bazel.server.logger.BuildClientLogger;
import org.jetbrains.bsp.bazel.server.resolvers.ActionGraphResolver;
import org.jetbrains.bsp.bazel.server.resolvers.ProcessResolver;
import org.jetbrains.bsp.bazel.server.resolvers.QueryResolver;
import org.jetbrains.bsp.bazel.server.resolvers.TargetsResolver;

public class BazelBspServer implements BuildServer, ScalaBuildServer, JavaBuildServer {

  public static final ImmutableSet<String> KNOWN_SOURCE_ROOTS =
      ImmutableSet.of("java", "scala", "kotlin", "javatests", "src", "test", "main", "testsrc");
  protected static final String SCALAC = "Scalac";
  protected static final String KOTLINC = "KotlinCompile";
  protected static final String JAVAC = "Javac";

  private final String bazel;
  private final String PUBLISH_ALL_ACTIONS = "--build_event_publish_all_actions";
  private final Map<BuildTargetIdentifier, List<SourceItem>> targetsToSources = new HashMap<>();
  private final CompletableFuture<Void> isInitialized = new CompletableFuture<>();
  private final CompletableFuture<Void> isFinished = new CompletableFuture<>();
  private final List<String> FILE_EXTENSIONS =
      ImmutableList.of(
          ".scala", ".java", ".kt", ".kts", ".sh", ".bzl", ".py", ".js", ".c", ".h", ".cpp",
          ".hpp");

  private final Semaphore processLock = new Semaphore(1, true);
  public BepServer bepServer = null;
  private String BES_BACKEND = "--bes_backend=grpc://localhost:";
  private String execRoot = null;
  private String workspaceRoot = null;
  private String binRoot = null;
  private String workspaceLabel = null;
  private ScalaBuildTarget scalacClasspath = null;


  private final ProcessResolver processResolver;
  private final QueryResolver queryResolver;
  private final TargetsResolver targetsResolver;
  private final ActionGraphResolver actionGraphResolver;

  private final ScalaBspServer scalaBspServer;
  private final JavaBspServer javaBspServer;

  // TODO: created in setter `setBuildClient`, HAVE TO BE moved to the constructor
  private BuildClientLogger buildClientLogger;

  // TODO: imho bsp server creation on the server side is too ambiguous (constructor + setters)
  public BazelBspServer(String pathToBazel) {
    this.bazel = pathToBazel;
    this.processResolver = new ProcessResolver(processLock, bazel, BES_BACKEND, PUBLISH_ALL_ACTIONS);
    this.queryResolver = new QueryResolver(processResolver, processLock);
    this.targetsResolver = new TargetsResolver(queryResolver);
    this.actionGraphResolver = new ActionGraphResolver(processResolver);

    this.scalaBspServer = new ScalaBspServer(targetsResolver, actionGraphResolver, SCALAC, JAVAC, getExecRoot());
    this.javaBspServer = new JavaBspServer(targetsResolver, actionGraphResolver, JAVAC, KOTLINC, getExecRoot());
  }

  public void setBackendPort(int port) {
    this.BES_BACKEND += port;
  }

  public void setBuildClientLogger(BuildClientLogger buildClientLogger) {
    this.buildClientLogger = buildClientLogger;
  }

  @Override
  public CompletableFuture<InitializeBuildResult> buildInitialize(
      InitializeBuildParams initializeBuildParams) {
    return handleBuildInitialize(
        () -> {
          BuildServerCapabilities capabilities = new BuildServerCapabilities();
          capabilities.setCompileProvider(new CompileProvider(Constants.SUPPORTED_LANGUAGES));
          capabilities.setRunProvider(new RunProvider(Constants.SUPPORTED_LANGUAGES));
          capabilities.setTestProvider(new TestProvider(Constants.SUPPORTED_LANGUAGES));
          capabilities.setDependencySourcesProvider(true);
          capabilities.setInverseSourcesProvider(true);
          capabilities.setResourcesProvider(true);
          checkBazelInstallation();
          return Either.forRight(
              new InitializeBuildResult(
                  Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities));
        });
  }

  private void checkBazelInstallation() {
    try {
      Process process = processResolver.startProcess();
      parseProcess(process);
      // Force-populate cache to avoid deadlock when looking up information from BEP listener.
      getExecRoot();
      getWorkspaceRoot();
      getBinRoot();
      getWorkspaceLabel();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onBuildInitialized() {
    isInitialized.complete(null);
  }

  @Override
  public CompletableFuture<Object> buildShutdown() {
    return handleBuildShutdown(
        () -> {
          isFinished.complete(null);
          return Either.forRight(new Object());
        });
  }

  @Override
  public void onBuildExit() {
    try {
      isFinished.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      System.exit(1);
    }

    System.exit(0);
  }

  @Override
  public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
    // TODO(illicitonion): Parameterise this to allow importing a subset of //...
    return executeCommand(
        () -> {
          try {
            Build.QueryResult queryResult =
                queryResolver.getQuery(
                    "query",
                    "--output=proto",
                    "--nohost_deps",
                    "--noimplicit_deps",
                    "kind(binary, //...) union kind(library, //...) union kind(test, //...)");
            List<BuildTarget> targets =
                queryResult.getTargetList().stream()
                    .map(Build.Target::getRule)
                    .filter(rule -> !rule.getRuleClass().equals("filegroup"))
                    .map(this::getBuildTarget)
                    .collect(Collectors.toList());
            return Either.forRight(new WorkspaceBuildTargetsResult(targets));
          } catch (IOException e) {
            return Either.forLeft(
                new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
          }
        });
  }

  private BuildTarget getBuildTarget(Build.Rule rule) {
    String name = rule.getName();
    System.out.println("Getting targets for rule: " + name);
    List<BuildTargetIdentifier> deps =
        rule.getAttributeList().stream()
            .filter(attribute -> attribute.getName().equals("deps"))
            .flatMap(srcDeps -> srcDeps.getStringListValueList().stream())
            .map(BuildTargetIdentifier::new)
            .collect(Collectors.toList());
    BuildTargetIdentifier label = new BuildTargetIdentifier(name);

    List<SourceItem> sources = getSourceItems(rule, label);

    Set<String> extensions = new TreeSet<>();

    for (SourceItem source : sources) {
      if (source.getUri().endsWith(".scala")) {
        extensions.add("scala");
      } else if (source.getUri().endsWith(".java")) {
        extensions.add("java");
      } else if (source.getUri().endsWith(".kt")) {
        extensions.add("kotlin");
        extensions.add("java"); // TODO(andrefmrocha): Remove this when kotlin is natively supported
      }
    }

    String ruleClass = rule.getRuleClass();
    BuildTarget target =
        new BuildTarget(
            label,
            new ArrayList<>(),
            new ArrayList<>(extensions),
            deps,
            new BuildTargetCapabilities(
                true, ruleClass.endsWith("_test"), ruleClass.endsWith("_binary")));
    target.setBaseDirectory(Uri.packageDirFromLabel(label.getUri(), getWorkspaceRoot()).toString());
    target.setDisplayName(label.getUri());
    if (extensions.contains("scala")) {
      getScalaBuildTarget()
          .ifPresent(
              (buildTarget) -> {
                target.setDataKind(BuildTargetDataKind.SCALA);
                target.setTags(Lists.newArrayList(getRuleType(rule)));
                target.setData(buildTarget);
              });
    } else if (extensions.contains("java") || extensions.contains("kotlin")) {
      target.setDataKind(BuildTargetDataKind.JVM);
      target.setTags(Lists.newArrayList(getRuleType(rule)));
      target.setData(getJVMBuildTarget());
    }
    return target;
  }

  private String getRuleType(Build.Rule rule) {
    String ruleClass = rule.getRuleClass();
    if (ruleClass.contains("library")) {
      return BuildTargetTag.LIBRARY;
    }
    if (ruleClass.contains("binary")) {
      return BuildTargetTag.APPLICATION;
    }
    if (ruleClass.contains("test")) {
      return BuildTargetTag.TEST;
    }

    return BuildTargetTag.NO_IDE;
  }

  private Optional<ScalaBuildTarget> getScalaBuildTarget() {
    if (scalacClasspath == null) {
      buildTargetsWithBep(
          Lists.newArrayList(
              new BuildTargetIdentifier(
                  "@io_bazel_rules_scala_scala_library//:io_bazel_rules_scala_scala_library"),
              new BuildTargetIdentifier(
                  "@io_bazel_rules_scala_scala_reflect//:io_bazel_rules_scala_scala_reflect"),
              new BuildTargetIdentifier(
                  "@io_bazel_rules_scala_scala_compiler//:io_bazel_rules_scala_scala_compiler")),
          Lists.newArrayList(
              "--aspects=@//.bazelbsp:aspects.bzl%scala_compiler_classpath_aspect",
              "--output_groups=scala_compiler_classpath_files"));
      List<String> classpath =
          bepServer.fetchScalacClasspath().stream().map(Uri::toString).collect(Collectors.toList());
      List<String> scalaVersions =
          classpath.stream()
              .filter(uri -> uri.contains("scala-library"))
              .collect(Collectors.toList());
      if (scalaVersions.size() != 1) {
        return Optional.empty();
      }
      String scalaVersion =
          scalaVersions
              .get(0)
              .substring(
                  scalaVersions.get(0).indexOf("scala-library-") + 14,
                  scalaVersions.get(0).indexOf(".jar"));
      scalacClasspath =
          new ScalaBuildTarget(
              "org.scala-lang",
              scalaVersion,
              scalaVersion.substring(0, scalaVersion.lastIndexOf(".")),
              ScalaPlatform.JVM,
              classpath);
      scalacClasspath.setJvmBuildTarget(getJVMBuildTarget());
    }

    return Optional.of(scalacClasspath);
  }

  private JvmBuildTarget getJVMBuildTarget() {
    // TODO(andrefmrocha): Properly determine jdk path
    return new JvmBuildTarget(null, getJavaVersion());
  }

  private String getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(0, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version = version.substring(0, dot);
      }
    }
    return version;
  }

  @Override
  public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams sourcesParams) {
    return executeCommand(
        () -> {
          try {
            Build.QueryResult queryResult =
                queryResolver.getQuery(
                    "query",
                    "--output=proto",
                    "("
                        + sourcesParams.getTargets().stream()
                            .map(BuildTargetIdentifier::getUri)
                            .collect(Collectors.joining("+"))
                        + ")");

            List<SourcesItem> sources =
                queryResult.getTargetList().stream()
                    .map(Build.Target::getRule)
                    .map(
                        rule -> {
                          BuildTargetIdentifier label = new BuildTargetIdentifier(rule.getName());
                          List<SourceItem> items = getSourceItems(rule, label);
                          List<String> roots =
                              Lists.newArrayList(
                                  Uri.fromAbsolutePath(getSourcesRoot(rule.getName())).toString());
                          SourcesItem item = new SourcesItem(label, items);
                          item.setRoots(roots);
                          return item;
                        })
                    .collect(Collectors.toList());
            return Either.forRight(new SourcesResult(sources));
          } catch (IOException e) {
            return Either.forLeft(
                new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
          }
        });
  }

  private List<SourceItem> getSourceItems(Build.Rule rule, BuildTargetIdentifier label) {
    List<SourceItem> srcs = getSrcs(rule, false);
    srcs.addAll(getSrcs(rule, true));
    targetsToSources.put(label, srcs);
    return srcs;
  }

  private List<SourceItem> getSrcs(Build.Rule rule, boolean isGenerated) {
    String srcType = isGenerated ? "generated_srcs" : "srcs";
    return getSrcsPaths(rule, srcType).stream()
        .map(uri -> new SourceItem(uri.toString(), SourceItemKind.FILE, isGenerated))
        .collect(Collectors.toList());
  }

  private List<Uri> getSrcsPaths(Build.Rule rule, String srcType) {
    return rule.getAttributeList().stream()
        .filter(attribute -> attribute.getName().equals(srcType))
        .flatMap(srcsSrc -> srcsSrc.getStringListValueList().stream())
        .flatMap(
            dep -> {
              if (isSourceFile(dep)) {
                return Lists.newArrayList(Uri.fromFileLabel(dep, getWorkspaceRoot())).stream();
              }
              try {
                Build.QueryResult queryResult = queryResolver.getQuery("query", "--output=proto", dep);
                return queryResult.getTargetList().stream()
                    .map(Build.Target::getRule)
                    .flatMap(queryRule -> getSrcsPaths(queryRule, srcType).stream())
                    .collect(Collectors.toList())
                    .stream();
              } catch (IOException e) {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private boolean isSourceFile(String dep) {
    return FILE_EXTENSIONS.stream().anyMatch(dep::endsWith) && !dep.startsWith("@");
  }

  private String getSourcesRoot(String uri) {
    List<String> root =
        KNOWN_SOURCE_ROOTS.stream().filter(uri::contains).collect(Collectors.toList());
    return getWorkspaceRoot()
        + (root.size() == 0
            ? ""
            : uri.substring(1, uri.indexOf(root.get(0)) + root.get(0).length()));
  }

  public synchronized String getWorkspaceRoot() {
    if (workspaceRoot == null) {
      workspaceRoot = Iterables.getOnlyElement(processResolver.runBazelLines("info", "workspace"));
    }
    return workspaceRoot;
  }

  public synchronized String getBinRoot() {
    if (binRoot == null) {
      binRoot = Iterables.getOnlyElement(processResolver.runBazelLines("info", "bazel-bin"));
    }
    return binRoot;
  }

  public synchronized String getExecRoot() {
    if (execRoot == null) {
      execRoot = Iterables.getOnlyElement(processResolver.runBazelLines("info", "execution_root"));
    }
    return execRoot;
  }

  public synchronized String getWorkspaceLabel() {
    if (workspaceLabel == null) {
      Path workspacePath = Paths.get(getExecRoot());
      workspaceLabel = workspacePath.toFile().getName();
    }
    return workspaceLabel;
  }

  @Override
  public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(
      InverseSourcesParams inverseSourcesParams) {
    return executeCommand(
        () -> {
          String fileUri = inverseSourcesParams.getTextDocument().getUri();
          String workspaceRoot = getWorkspaceRoot();
          String prefix = Uri.fromWorkspacePath("", workspaceRoot).toString();
          if (!inverseSourcesParams.getTextDocument().getUri().startsWith(prefix)) {
            throw new RuntimeException(
                "Could not resolve " + fileUri + " within workspace " + prefix);
          }
          try {
            Build.QueryResult result =
                queryResolver.getQuery(
                    "query",
                    "--output=proto",
                    "kind(rule, rdeps(//..., " + fileUri.substring(prefix.length()) + ", 1))");
            List<BuildTargetIdentifier> targets =
                result.getTargetList().stream()
                    .map(Build.Target::getRule)
                    .map(Build.Rule::getName)
                    .map(BuildTargetIdentifier::new)
                    .collect(Collectors.toList());

            return Either.forRight(new InverseSourcesResult(targets));
          } catch (IOException e) {
            return Either.forLeft(
                new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
          }
        });
  }

  @Override
  public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(
      DependencySourcesParams dependencySourcesParams) {
    return executeCommand(
        () -> {
          List<String> targets =
              dependencySourcesParams.getTargets().stream()
                  .map(BuildTargetIdentifier::getUri)
                  .collect(Collectors.toList());

          DependencySourcesResult result =
              new DependencySourcesResult(
                  targets.stream()
                      .sorted()
                      .map(
                          target -> {
                            List<String> files =
                                lookupTransitiveSourceJars(target).stream()
                                    .map(
                                        execPath ->
                                            Uri.fromExecPath(execPath, getExecRoot()).toString())
                                    .collect(Collectors.toList());
                            return new DependencySourcesItem(
                                new BuildTargetIdentifier(target), files);
                          })
                      .collect(Collectors.toList()));
          return Either.forRight(result);
        });
  }

  private List<String> lookupTransitiveSourceJars(String target) {
    // TODO(illicitonion): Use an aspect output group, rather than parsing stderr logging
    List<String> lines =
        processResolver.runBazelStderr("build", "--aspects", "@//.bazelbsp:aspects.bzl%print_aspect", target);
    return lines.stream()
        .map(line -> Splitter.on(" ").splitToList(line))
        .filter(
            parts ->
                parts.size() == 3
                    && parts.get(0).equals("DEBUG:")
                    && parts.get(1).contains(".bazelbsp/aspects.bzl")
                    && parts.get(2).endsWith(".jar"))
        .map(parts -> "exec-root://" + parts.get(2))
        .collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams resourcesParams) {
    return executeCommand(
        () -> {
          try {
            Build.QueryResult query = queryResolver.getQuery("query", "--output=proto", "//...");
            System.out.println("Resources query result " + query);
            ResourcesResult resourcesResult =
                new ResourcesResult(
                    query.getTargetList().stream()
                        .map(Build.Target::getRule)
                        .filter(
                            rule ->
                                resourcesParams.getTargets().stream()
                                    .anyMatch(target -> target.getUri().equals(rule.getName())))
                        .filter(
                            rule ->
                                rule.getAttributeList().stream()
                                    .anyMatch(
                                        attribute ->
                                            attribute.getName().equals("resources")
                                                && attribute.hasExplicitlySpecified()
                                                && attribute.getExplicitlySpecified()))
                        .map(
                            rule ->
                                new ResourcesItem(
                                    new BuildTargetIdentifier(rule.getName()),
                                    getResources(rule, query)))
                        .collect(Collectors.toList()));
            return Either.forRight(resourcesResult);
          } catch (IOException e) {
            return Either.forLeft(
                new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
          }
        });
  }

  private List<String> getResources(Build.Rule rule, Build.QueryResult queryResult) {
    return rule.getAttributeList().stream()
        .filter(
            attribute ->
                attribute.getName().equals("resources")
                    && attribute.hasExplicitlySpecified()
                    && attribute.getExplicitlySpecified())
        .flatMap(
            attribute -> {
              List<Build.Target> targetsRule =
                  attribute.getStringListValueList().stream()
                      .map(label -> isPackage(queryResult, label))
                      .filter(targets -> !targets.isEmpty())
                      .flatMap(Collection::stream)
                      .collect(Collectors.toList());
              List<String> targetsResources = getResourcesOutOfRule(targetsRule);

              List<String> resources =
                  attribute.getStringListValueList().stream()
                      .filter(label -> isPackage(queryResult, label).isEmpty())
                      .map(label -> Uri.fromFileLabel(label, getWorkspaceRoot()).toString())
                      .collect(Collectors.toList());

              return Stream.concat(targetsResources.stream(), resources.stream());
            })
        .collect(Collectors.toList());
  }

  private List<? extends Build.Target> isPackage(Build.QueryResult queryResult, String label) {
    return queryResult.getTargetList().stream()
        .filter(target -> target.hasRule() && target.getRule().getName().equals(label))
        .collect(Collectors.toList());
  }

  private List<String> getResourcesOutOfRule(List<Build.Target> rules) {
    return rules.stream()
        .flatMap(resourceRule -> resourceRule.getRule().getAttributeList().stream())
        .filter((srcAttribute) -> srcAttribute.getName().equals("srcs"))
        .flatMap(resourceAttribute -> resourceAttribute.getStringListValueList().stream())
        .map(src -> Uri.fromFileLabel(src, getWorkspaceRoot()).toString())
        .collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<CompileResult> buildTargetCompile(CompileParams compileParams) {
    return executeCommand(() -> buildTargetsWithBep(compileParams.getTargets(), new ArrayList<>()));
  }

  private Either<ResponseError, CompileResult> buildTargetsWithBep(
      List<BuildTargetIdentifier> targets, List<String> extraFlags) {
    List<String> args = Lists.newArrayList(bazel, "build", BES_BACKEND, PUBLISH_ALL_ACTIONS);
    args.addAll(targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList()));
    args.addAll(extraFlags);
    int exitCode = -1;

    final Map<String, String> diagnosticsProtosLocations =
        bepServer.getDiagnosticsProtosLocations();
    try {
      Build.QueryResult queryResult =
          queryResolver.getQuery(
              "query",
              "--output=proto",
              "("
                  + targets.stream()
                      .map(BuildTargetIdentifier::getUri)
                      .collect(Collectors.joining("+"))
                  + ")");

      for (Build.Target target : queryResult.getTargetList()) {
        target.getRule().getRuleOutputList().stream()
            .filter(output -> output.contains("diagnostics"))
            .forEach(
                output ->
                    diagnosticsProtosLocations.put(
                        target.getRule().getName(), convertOutputToPath(output, getBinRoot())));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      if (targetsToSources.isEmpty()) {
        workspaceBuildTargets().wait();
      }

      Process process = new ProcessBuilder(args).start();
      exitCode = parseProcess(process);
    } catch (InterruptedException | IOException e) {
      System.out.println("Failed to run bazel: " + e);
    }

    for (Map.Entry<String, String> diagnostics : diagnosticsProtosLocations.entrySet()) {
      String target = diagnostics.getKey();
      String diagnosticsPath = diagnostics.getValue();
      Map<Uri, List<PublishDiagnosticsParams>> filesToDiagnostics = new HashMap<>();
      try {
        BuildTargetIdentifier targetIdentifier = new BuildTargetIdentifier(target);
        bepServer.getDiagnostics(filesToDiagnostics, targetIdentifier, diagnosticsPath);
        bepServer.emitDiagnostics(filesToDiagnostics, targetIdentifier);
      } catch (IOException e) {
        System.err.println("Failed to get diagnostics for " + target);
      }
    }

    return Either.forRight(new CompileResult(BepServer.convertExitCode(exitCode)));
  }

  private String convertOutputToPath(String output, String prefix) {
    String pathToFile = output.replaceAll("(//|:)", "/");
    return prefix + pathToFile;
  }

  @Override
  public CompletableFuture<TestResult> buildTargetTest(TestParams testParams) {
    return executeCommand(
        () -> {
          Either<ResponseError, CompileResult> build =
              buildTargetsWithBep(Lists.newArrayList(testParams.getTargets()), new ArrayList<>());
          if (build.isLeft()) {
            return Either.forLeft(build.getLeft());
          }

          CompileResult result = build.getRight();
          if (result.getStatusCode() != StatusCode.OK) {
            return Either.forRight(new TestResult(result.getStatusCode()));
          }

          try {
            Process process =
                processResolver.startProcess(
                    Lists.asList(
                            "test",
                            "("
                                + Joiner.on("+")
                                    .join(
                                        testParams.getTargets().stream()
                                            .map(BuildTargetIdentifier::getUri)
                                            .collect(Collectors.toList()))
                                + ")",
                            testParams.getArguments().toArray(new String[0]))
                        .toArray(new String[0]));

            int returnCode = parseProcess(process);
            return Either.forRight(new TestResult(BepServer.convertExitCode(returnCode)));
          } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Override
  public CompletableFuture<RunResult> buildTargetRun(RunParams runParams) {
    return executeCommand(
        () -> {
          Either<ResponseError, CompileResult> build =
              buildTargetsWithBep(Lists.newArrayList(runParams.getTarget()), new ArrayList<>());
          if (build.isLeft()) {
            return Either.forLeft(build.getLeft());
          }

          CompileResult result = build.getRight();
          if (result.getStatusCode() != StatusCode.OK) {
            return Either.forRight(new RunResult(result.getStatusCode()));
          }

          try {
            Process process =
                processResolver.startProcess(
                    Lists.asList(
                            "run",
                            runParams.getTarget().getUri(),
                            runParams.getArguments().toArray(new String[0]))
                        .toArray(new String[0]));

            int returnCode = parseProcess(process);
            return Either.forRight(new RunResult(BepServer.convertExitCode(returnCode)));
          } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private int parseProcess(Process process) throws IOException, InterruptedException {
    Set<String> messageBuilder = new HashSet<>();
    String line;
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    while ((line = reader.readLine()) != null) {
      messageBuilder.add(line.trim());
    }

    String message = String.join("\n", messageBuilder);
    int returnCode = process.waitFor();
    if (returnCode != 0) {
      buildClientLogger.logError(message);
    }
    else {
      buildClientLogger.logMessage(message);
    }
    processLock.release();
    return returnCode;
  }

  @Override
  public CompletableFuture<CleanCacheResult> buildTargetCleanCache(
      CleanCacheParams cleanCacheParams) {
    return executeCommand(
        () -> {
          CleanCacheResult result;
          try {
            result = new CleanCacheResult(String.join("\n", processResolver.runBazelLines("clean")), true);
          } catch (RuntimeException e) {
            result = new CleanCacheResult(e.getMessage(), false);
          }
          return Either.forRight(result);
        });
  }

  @Override
  public CompletableFuture<ScalacOptionsResult> buildTargetScalacOptions(
      ScalacOptionsParams scalacOptionsParams) {
    return executeCommand(() -> scalaBspServer.buildTargetScalacOptions(scalacOptionsParams));
  }

  @Override
  public CompletableFuture<JavacOptionsResult> buildTargetJavacOptions(
      JavacOptionsParams javacOptionsParams) {
    return executeCommand(() -> javaBspServer.buildTargetJavacOptions(javacOptionsParams));
  }

  @Override
  public CompletableFuture<ScalaTestClassesResult> buildTargetScalaTestClasses(
      ScalaTestClassesParams scalaTestClassesParams) {
    return scalaBspServer.buildTargetScalaTestClasses(scalaTestClassesParams);
  }

  @Override
  public CompletableFuture<ScalaMainClassesResult> buildTargetScalaMainClasses(
      ScalaMainClassesParams scalaMainClassesParams) {
    return scalaBspServer.buildTargetScalaMainClasses(scalaMainClassesParams);
  }

  private <T> CompletableFuture<T> handleBuildInitialize(
      Supplier<Either<ResponseError, T>> request) {
    if (isFinished()) {
      return completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
    }

    return getValue(request);
  }

  private <T> CompletableFuture<T> handleBuildShutdown(Supplier<Either<ResponseError, T>> request) {
    if (!isInitialized()) {
      return completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has not been initialized yet!", false));
    }

    return getValue(request);
  }

  private <T> CompletableFuture<T> executeCommand(Supplier<Either<ResponseError, T>> request) {
    if (!isInitialized()) {
      return completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverNotInitialized,
              "Server has not been initialized yet!",
              false));
    }
    if (isFinished()) {
      return completeExceptionally(
          new ResponseError(
              ResponseErrorCode.serverErrorEnd, "Server has already shutdown!", false));
    }

    return getValue(request);
  }

  private <T> CompletableFuture<T> completeExceptionally(ResponseError error) {
    CompletableFuture<T> future = new CompletableFuture<>();
    future.completeExceptionally(new ResponseErrorException(error));
    return future;
  }

  private boolean isInitialized() {
    try {
      isInitialized.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      return false;
    }
    return true;
  }

  private boolean isFinished() {
    return isFinished.isDone();
  }

  private <T> CompletableFuture<T> getValue(Supplier<Either<ResponseError, T>> request) {
    CompletableFuture<Either<ResponseError, T>> execution = CompletableFuture.supplyAsync(request);
    Either<ResponseError, T> either;
    try {
      either = execution.get();
    } catch (CompletionException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return completeExceptionally(
          new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
    }

    if (either.isLeft()) {
      return completeExceptionally(either.getLeft());
    }
    else {
      return CompletableFuture.completedFuture(either.getRight());
    }
  }

  public Iterable<SourceItem> getCachedBuildTargetSources(BuildTargetIdentifier target) {
    return targetsToSources.getOrDefault(target, new ArrayList<>());
  }
}
