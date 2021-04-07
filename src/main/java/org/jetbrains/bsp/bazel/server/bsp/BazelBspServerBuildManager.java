package org.jetbrains.bsp.bazel.server.bsp;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.JvmBuildTarget;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourceItemKind;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelQueryKindParameters;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.QueryResolver;
import org.jetbrains.bsp.bazel.server.bsp.utils.BuildManagerParsingUtils;

public class BazelBspServerBuildManager {

  private static final Logger LOGGER = LogManager.getLogger(BazelBspServerBuildManager.class);

  public static final String DEBUG_MESSAGE = "DEBUG:";
  public static final String ASPECT_LOCATION = ".bazelbsp/aspects.bzl";
  public static final String FETCH_JAVA_VERSION_ASPECT =
      "@//.bazelbsp:aspects.bzl%fetch_java_target_version";
  public static final String BAZEL_JDK_CURRENT_JAVA_TOOLCHAIN =
      "@bazel_tools//tools/jdk:current_java_toolchain";
  private final BazelBspServerConfig serverConfig;
  private final BazelBspServerRequestHelpers serverRequestHelpers;
  private final BazelData bazelData;
  private final BazelRunner bazelRunner;

  private BepServer bepServer;
  private ScalaBuildTarget scalacClasspath;
  private String javaVersion;

  public BazelBspServerBuildManager(
      BazelBspServerConfig serverConfig,
      BazelBspServerRequestHelpers serverRequestHelpers,
      BazelData bazelData,
      BazelRunner bazelRunner) {
    this.serverConfig = serverConfig;
    this.serverRequestHelpers = serverRequestHelpers;
    this.bazelData = bazelData;
    this.bazelRunner = bazelRunner;
  }

  public BuildTarget getBuildTargetForRule(Build.Rule rule) {
    String name = rule.getName();
    LOGGER.info("Getting targets for rule: " + name);

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
      if (source.getUri().endsWith(Constants.SCALA_EXTENSION)) {
        extensions.add(Constants.SCALA);
      } else if (source.getUri().endsWith(Constants.JAVA_EXTENSION)) {
        extensions.add(Constants.JAVA);
      } else if (source.getUri().endsWith(Constants.KOTLIN_EXTENSION)) {
        extensions.add(Constants.KOTLIN);
        extensions.add(
            Constants.JAVA); // TODO(andrefmrocha): Remove this when kotlin is natively supported
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
                true,
                ruleClass.endsWith("_" + Constants.TEST_RULE_TYPE),
                ruleClass.endsWith("_" + Constants.BINARY_RULE_TYPE)));
    target.setBaseDirectory(
        Uri.packageDirFromLabel(label.getUri(), bazelData.getWorkspaceRoot()).toString());
    target.setDisplayName(label.getUri());
    if (extensions.contains(Constants.SCALA)) {
      getScalaBuildTarget(rule)
          .ifPresent(
              (buildTarget) -> {
                target.setDataKind(BuildTargetDataKind.SCALA);
                target.setTags(
                    Lists.newArrayList(BuildManagerParsingUtils.getRuleType(rule.getRuleClass())));
                target.setData(buildTarget);
              });
    } else if (extensions.contains(Constants.JAVA) || extensions.contains(Constants.KOTLIN)) {
      target.setDataKind(BuildTargetDataKind.JVM);
      target.setTags(Lists.newArrayList(BuildManagerParsingUtils.getRuleType(rule.getRuleClass())));
      target.setData(getJVMBuildTarget(rule));
    }
    return target;
  }

  private List<BuildTarget> getBuildTargetForProjectPath(String projectPath) {
    List<BazelQueryKindParameters> kindParameters =
        ImmutableList.of(
            BazelQueryKindParameters.fromPatternAndInput("binary", projectPath),
            BazelQueryKindParameters.fromPatternAndInput("library", projectPath),
            BazelQueryKindParameters.fromPatternAndInput("test", projectPath));

    BazelProcess bazelProcess =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withFlag(BazelRunnerFlag.NOHOST_DEPS)
            .withFlag(BazelRunnerFlag.NOIMPLICIT_DEPS)
            .withKinds(kindParameters)
            .executeBazelBesCommand();

    Build.QueryResult queryResult = QueryResolver.getQueryResultForProcess(bazelProcess);

    return queryResult.getTargetList().stream()
        .map(Build.Target::getRule)
        .filter(rule -> !rule.getRuleClass().equals("filegroup"))
        .map(this::getBuildTargetForRule)
        .collect(Collectors.toList());
  }

  private Optional<ScalaBuildTarget> getScalaBuildTarget(Build.Rule rule) {
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
          bepServer.getCompilerClasspath().stream().map(Uri::toString).collect(Collectors.toList());
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
      scalacClasspath.setJvmBuildTarget(getJVMBuildTarget(rule));
    }

    return Optional.of(scalacClasspath);
  }

  private JvmBuildTarget getJVMBuildTarget(Build.Rule rule) {
    Optional<String> javaHomePath = getJavaPath(rule);
    Optional<String> javaVersion = getJavaVersion();
    return new JvmBuildTarget(javaHomePath.orElse(null), javaVersion.orElse(null));
  }

  private Optional<String> getJavaPath(Build.Rule rule) {
    List<String> traversingPath = Lists.newArrayList("$jvm", "$java_runtime", ":alias", "actual");

    return traverseDependency(rule, traversingPath)
        .map(Build.Rule::getLocation)
        .map(location -> location.substring(0, location.indexOf("/BUILD")))
        .map(path -> Uri.fromAbsolutePath(path).toString());
  }

  private Optional<Build.Rule> traverseDependency(
      Build.Rule startingRule, List<String> attributesToTraverse) {
    Build.Rule currentRule = startingRule;

    for (String attributeToTraverse : attributesToTraverse) {
      Optional<Build.Rule> rule =
          currentRule.getAttributeList().stream()
              .filter(
                  attribute ->
                      attribute.getName().equals(attributeToTraverse) && attribute.hasStringValue())
              .findFirst()
              .flatMap(this::getTargetFromAttribute)
              .map(Build.Target::getRule);

      if (!rule.isPresent()) {
        return Optional.empty();
      }

      currentRule = rule.get();
    }

    return Optional.of(currentRule);
  }

  private Optional<Build.Target> getTargetFromAttribute(Build.Attribute attribute) {
    BazelProcess processResult =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withArgument(attribute.getStringValue())
            .executeBazelBesCommand();

    return QueryResolver.getQueryResultForProcess(processResult).getTargetList().stream()
        .findFirst();
  }

  private Optional<String> getJavaVersion() {
    if (javaVersion == null) {
      List<String> lines =
          bazelRunner
              .commandBuilder()
              .build()
              .withFlag(BazelRunnerFlag.ASPECTS, FETCH_JAVA_VERSION_ASPECT)
              .withArgument(BAZEL_JDK_CURRENT_JAVA_TOOLCHAIN)
              .executeBazelCommand()
              .getStderr();

      Optional<String> javaVersion =
          lines.stream()
              .map(line -> Splitter.on(" ").splitToList(line))
              .filter(
                  parts ->
                      parts.size() == 3
                          && parts.get(0).equals(DEBUG_MESSAGE)
                          && parts.get(1).contains(ASPECT_LOCATION)
                          && parts.get(2).chars().allMatch(Character::isDigit))
              .map(parts -> parts.get(2))
              .findFirst();

      javaVersion.ifPresent(version -> this.javaVersion = version);
    }

    return Optional.ofNullable(javaVersion);
  }

  public Either<ResponseError, CompileResult> buildTargetsWithBep(
      List<BuildTargetIdentifier> targets, List<String> extraFlags) {
    List<String> bazelTargets =
        targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList());

    StatusCode exitCode = StatusCode.ERROR;

    final Map<String, String> diagnosticsProtosLocations =
        bepServer.getDiagnosticsProtosLocations();
    BazelProcess bazelProcess =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withTargets(bazelTargets)
            .executeBazelBesCommand();

    Build.QueryResult queryResult = QueryResolver.getQueryResultForProcess(bazelProcess);

    for (Build.Target target : queryResult.getTargetList()) {
      target.getRule().getRuleOutputList().stream()
          .filter(output -> output.contains(Constants.DIAGNOSTICS))
          .forEach(
              output ->
                  diagnosticsProtosLocations.put(
                      target.getRule().getName(),
                      BuildManagerParsingUtils.convertOutputToPath(
                          output, bazelData.getBinRoot())));
    }

    try {
      if (bepServer.getBuildTargetsSources().isEmpty()) {
        getWorkspaceBuildTargets().wait();
      }

      exitCode =
          bazelRunner
              .commandBuilder()
              .build()
              .withFlags(extraFlags)
              .withArguments(
                  bazelTargets) // TODO: Parameterize command builders for different cases
              .executeBazelBesCommand()
              .waitAndGetResult()
              .getStatusCode();
    } catch (InterruptedException e) {
      LOGGER.error("Failed to run bazel: {}", e.toString());
    }

    for (Map.Entry<String, String> diagnostics : diagnosticsProtosLocations.entrySet()) {
      String target = diagnostics.getKey();
      String diagnosticsPath = diagnostics.getValue();
      BuildTargetIdentifier targetIdentifier = new BuildTargetIdentifier(target);
      // TODO (abrams27) is it ok?
      bepServer.emitDiagnostics(
          bepServer.collectDiagnostics(targetIdentifier, diagnosticsPath), targetIdentifier);
    }

    return Either.forRight(new CompileResult(exitCode));
  }

  public CompletableFuture<WorkspaceBuildTargetsResult> getWorkspaceBuildTargets() {
    return serverRequestHelpers.executeCommand(
        "workspaceBuildTargets",
        () -> {
          List<String> projectPaths = serverConfig.getTargetProjectPaths();
          // TODO (abrams27) simplify
          List<BuildTarget> targets = new ArrayList<>();

          for (String projectPath : projectPaths) {
            targets.addAll(getBuildTargetForProjectPath(projectPath));
          }
          return Either.forRight(new WorkspaceBuildTargetsResult(targets));
        });
  }

  public List<SourceItem> getSourceItems(Build.Rule rule, BuildTargetIdentifier label) {
    List<SourceItem> srcs = getSrcs(rule, false);
    srcs.addAll(getSrcs(rule, true));
    // TODO (abrams27) fix updating
    bepServer.getBuildTargetsSources().put(label, srcs);
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
                return Lists.newArrayList(Uri.fromFileLabel(dep, bazelData.getWorkspaceRoot()))
                    .stream();
              }
              BazelProcess bazelProcess =
                  bazelRunner
                      .commandBuilder()
                      .query()
                      .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
                      .withArgument(dep)
                      .executeBazelBesCommand();

              Build.QueryResult queryResult = QueryResolver.getQueryResultForProcess(bazelProcess);

              return queryResult.getTargetList().stream()
                  .map(Build.Target::getRule)
                  .flatMap(queryRule -> getSrcsPaths(queryRule, srcType).stream())
                  .collect(Collectors.toList())
                  .stream();
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private boolean isSourceFile(String dep) {
    return Constants.FILE_EXTENSIONS.stream().anyMatch(dep::endsWith) && !dep.startsWith("@");
  }

  public String getSourcesRoot(String uri) {
    List<String> root =
        Constants.KNOWN_SOURCE_ROOTS.stream().filter(uri::contains).collect(Collectors.toList());
    return bazelData.getWorkspaceRoot()
        + (root.size() == 0
            ? ""
            : uri.substring(1, uri.indexOf(root.get(0)) + root.get(0).length()));
  }

  public List<String> lookUpTransitiveSourceJars(String target) {
    // TODO(illicitonion): Use an aspect output group, rather than parsing stderr
    // logging
    List<String> lines =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlag(BazelRunnerFlag.ASPECTS, "@//.bazelbsp:aspects.bzl%print_aspect")
            .withArgument(target)
            .executeBazelBesCommand()
            .getStderr();

    return lines.stream()
        .map(line -> Splitter.on(" ").splitToList(line))
        .filter(
            parts ->
                parts.size() == 3
                    && parts.get(0).equals(DEBUG_MESSAGE)
                    && parts.get(1).contains(ASPECT_LOCATION)
                    && parts.get(2).endsWith(".jar"))
        .map(parts -> Constants.EXEC_ROOT_PREFIX + parts.get(2))
        .collect(Collectors.toList());
  }

  public List<String> getResources(Build.Rule rule, Build.QueryResult queryResult) {
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
                      .map(
                          label ->
                              Uri.fromFileLabel(label, bazelData.getWorkspaceRoot()).toString())
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
        .map(src -> Uri.fromFileLabel(src, bazelData.getWorkspaceRoot()).toString())
        .collect(Collectors.toList());
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
  }
}
