package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.DependencySourcesItem;
import ch.epfl.scala.bsp4j.DependencySourcesParams;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InverseSourcesParams;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.JvmBuildTarget;
import ch.epfl.scala.bsp4j.JvmEnvironmentItem;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult;
import ch.epfl.scala.bsp4j.ResourcesItem;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.ScalaBuildTarget;
import ch.epfl.scala.bsp4j.ScalaMainClass;
import ch.epfl.scala.bsp4j.ScalaMainClassesItem;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaPlatform;
import ch.epfl.scala.bsp4j.ScalaTestClassesItem;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourceItemKind;
import ch.epfl.scala.bsp4j.SourcesItem;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario;
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep;

public class BazelBspSampleRepoTest extends BazelBspTestBaseScenario {

  private static final String REPO_NAME = "sample-repo";

  public BazelBspSampleRepoTest() {
    super(REPO_NAME);
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    var test = new BazelBspSampleRepoTest();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return List.of(
        resolveProject(),
        compareWorkspaceTargetsResults(),
        sourcesResults(),
        resourcesResults(),
        inverseSourcesResults(),
        dependencySourcesResults(),
        scalaMainClasses(),
        scalaTestClasses(),
        jvmRunEnvironment(),
        jvmTestEnvironment()
        // TODO
        //  new BazelBspServerSingleTest(
        //      "targets run unsuccessfully",
        //      client::testTargetsRunUnsuccessfully),
        //  new BazelBspServerSingleTest(
        //      "targets test unsuccessfully",
        //      client::testTargetsTestUnsuccessfully),
        //      new BazelBspServerSingleTest(
        //          "target capabilities",
        //          client::testTargetCapabilities)
        );
  }

  private BazelBspTestScenarioStep resolveProject() {
    return new BazelBspTestScenarioStep(
        "resolve project", () -> testClient.testResolveProject(Duration.ofMinutes(2)));
  }

  private BazelBspTestScenarioStep compareWorkspaceTargetsResults() {
    WorkspaceBuildTargetsResult expectedWorkspaceBuildTargetsResult =
        getExpectedWorkspaceBuildTargetsResult();

    return new BazelBspTestScenarioStep(
        "compare workspace targets results",
        () ->
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(30), expectedWorkspaceBuildTargetsResult));
  }

  private BazelBspTestScenarioStep sourcesResults() {
    SourceItem targetWithoutJvmFlagsExampleScala =
        new SourceItem(
            "file://$WORKSPACE/target_without_jvm_flags/Example.scala", SourceItemKind.FILE, false);

    SourcesItem targetWithoutJvmFlagsSources =
        new SourcesItem(
            new BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            List.of(targetWithoutJvmFlagsExampleScala));
    targetWithoutJvmFlagsSources.setRoots(List.of("file://$WORKSPACE/target_without_jvm_flags/"));

    SourceItem targetWithoutArgsExampleScala =
        new SourceItem(
            "file://$WORKSPACE/target_without_args/Example.scala", SourceItemKind.FILE, false);

    SourcesItem targetWithoutArgsSources =
        new SourcesItem(
            new BuildTargetIdentifier("//target_without_args:binary"),
            List.of(targetWithoutArgsExampleScala));
    targetWithoutArgsSources.setRoots(List.of("file://$WORKSPACE/target_without_args/"));

    SourceItem targetWithoutMainClassExampleScala =
        new SourceItem(
            "file://$WORKSPACE/target_without_main_class/Example.scala",
            SourceItemKind.FILE,
            false);

    SourcesItem targetWithoutMainClassSources =
        new SourcesItem(
            new BuildTargetIdentifier("//target_without_main_class:library"),
            List.of(targetWithoutMainClassExampleScala));
    targetWithoutMainClassSources.setRoots(List.of("file://$WORKSPACE/target_without_main_class/"));

    SourceItem targetWithResourcesJavaBinaryJava =
        new SourceItem(
            "file://$WORKSPACE/target_with_resources/JavaBinary.java", SourceItemKind.FILE, false);

    SourcesItem targetWithResourcesSources =
        new SourcesItem(
            new BuildTargetIdentifier("//target_with_resources:java_binary"),
            List.of(targetWithResourcesJavaBinaryJava));
    targetWithResourcesSources.setRoots(List.of("file://$WORKSPACE/target_with_resources/"));

    SourceItem targetWithDependencyJavaBinaryJava =
        new SourceItem(
            "file://$WORKSPACE/target_with_dependency/JavaBinary.java", SourceItemKind.FILE, false);

    SourcesItem targetWithDependencySources =
        new SourcesItem(
            new BuildTargetIdentifier("//target_with_dependency:java_binary"),
            List.of(targetWithDependencyJavaBinaryJava));
    targetWithDependencySources.setRoots(List.of("file://$WORKSPACE/target_with_dependency/"));

    SourceItem scalaTargetsScalaBinaryScala =
        new SourceItem(
            "file://$WORKSPACE/scala_targets/ScalaBinary.scala", SourceItemKind.FILE, false);

    SourcesItem scalaTargetsScalaBinarySources =
        new SourcesItem(
            new BuildTargetIdentifier("//scala_targets:scala_binary"),
            List.of(scalaTargetsScalaBinaryScala));
    scalaTargetsScalaBinarySources.setRoots(List.of("file://$WORKSPACE/scala_targets/"));

    SourceItem scalaTargetsScalaTestScala =
        new SourceItem(
            "file://$WORKSPACE/scala_targets/ScalaTest.scala", SourceItemKind.FILE, false);

    SourcesItem scalaTargetsScalaTestSources =
        new SourcesItem(
            new BuildTargetIdentifier("//scala_targets:scala_test"),
            List.of(scalaTargetsScalaTestScala));
    scalaTargetsScalaTestSources.setRoots(List.of("file://$WORKSPACE/scala_targets/"));

    SourceItem javaTargetsJavaBinaryJava =
        new SourceItem(
            "file://$WORKSPACE/java_targets/JavaBinary.java", SourceItemKind.FILE, false);

    SourcesItem javaTargetsJavaBinarySources =
        new SourcesItem(
            new BuildTargetIdentifier("//java_targets:java_binary"),
            List.of(javaTargetsJavaBinaryJava));
    javaTargetsJavaBinarySources.setRoots(List.of("file://$WORKSPACE/java_targets/"));

    SourceItem javaTargetsJavaLibraryJava =
        new SourceItem(
            "file://$WORKSPACE/java_targets/JavaLibrary.java", SourceItemKind.FILE, false);

    SourcesItem javaTargetsJavaLibrarySources =
        new SourcesItem(
            new BuildTargetIdentifier("//java_targets:java_library"),
            List.of(javaTargetsJavaLibraryJava));
    javaTargetsJavaLibrarySources.setRoots(List.of("file://$WORKSPACE/java_targets/"));

    SourceItem javaTargetsSubpackageJavaLibraryJava =
        new SourceItem(
            "file://$WORKSPACE/java_targets/subpackage/JavaLibrary2.java",
            SourceItemKind.FILE,
            false);

    SourcesItem javaTargetsSubpackageJavaLibrarySources =
        new SourcesItem(
            new BuildTargetIdentifier("//java_targets/subpackage:java_library"),
            List.of(javaTargetsSubpackageJavaLibraryJava));
    javaTargetsSubpackageJavaLibrarySources.setRoots(
        List.of("file://$WORKSPACE/java_targets/subpackage/"));

    SourcesItem bspWorkspaceRoot =
        new SourcesItem(new BuildTargetIdentifier("bsp-workspace-root"), List.of());
    bspWorkspaceRoot.setRoots(List.of());

    SourcesItem javaTargetsJavaLibraryExportedSources =
        new SourcesItem(
            new BuildTargetIdentifier("//java_targets:java_library_exported"), List.of());
    javaTargetsJavaLibraryExportedSources.setRoots(List.of());

    SourcesParams sourcesParams = new SourcesParams(getExpectedTargetIdentifiers());

    SourcesResult expectedSourcesResult =
        new SourcesResult(
            List.of(
                targetWithoutArgsSources,
                targetWithoutJvmFlagsSources,
                targetWithoutMainClassSources,
                targetWithResourcesSources,
                targetWithDependencySources,
                scalaTargetsScalaBinarySources,
                scalaTargetsScalaTestSources,
                javaTargetsJavaBinarySources,
                javaTargetsJavaLibrarySources,
                javaTargetsSubpackageJavaLibrarySources,
                bspWorkspaceRoot,
                javaTargetsJavaLibraryExportedSources));

    return new BazelBspTestScenarioStep(
        "sources results",
        () -> testClient.testSources(Duration.ofSeconds(30), sourcesParams, expectedSourcesResult));
  }

  private BazelBspTestScenarioStep resourcesResults() {

    ResourcesItem bspWorkspaceRoot =
        new ResourcesItem(
            new BuildTargetIdentifier("bsp-workspace-root"), List.of("file://$WORKSPACE/"));

    ResourcesItem targetWithResources =
        new ResourcesItem(
            new BuildTargetIdentifier("//target_with_resources:java_binary"),
            List.of(
                "file://$WORKSPACE/target_with_resources/file1.txt",
                "file://$WORKSPACE/target_with_resources/file2.txt"));

    ResourcesItem javaTargetsSubpackageJavaLibrary =
        new ResourcesItem(
            new BuildTargetIdentifier("//java_targets/subpackage:java_library"), List.of());

    ResourcesItem javaTargetsJavaBinary =
        new ResourcesItem(new BuildTargetIdentifier("//java_targets:java_binary"), List.of());

    ResourcesItem javaTargetsJavaLibrary =
        new ResourcesItem(new BuildTargetIdentifier("//java_targets:java_library"), List.of());

    ResourcesItem javaTargetsJavaLibraryExported =
        new ResourcesItem(
            new BuildTargetIdentifier("//java_targets:java_library_exported"), List.of());

    ResourcesItem scalaTargetsScalaBinary =
        new ResourcesItem(new BuildTargetIdentifier("//scala_targets:scala_binary"), List.of());

    ResourcesItem scalaTargetsScalaTest =
        new ResourcesItem(new BuildTargetIdentifier("//scala_targets:scala_test"), List.of());

    ResourcesItem targetWithDependencyJavaBinary =
        new ResourcesItem(
            new BuildTargetIdentifier("//target_with_dependency:java_binary"), List.of());

    ResourcesItem targetWithoutArgsBinary =
        new ResourcesItem(new BuildTargetIdentifier("//target_without_args:binary"), List.of());

    ResourcesItem targetWithoutJvmFlagsBinary =
        new ResourcesItem(
            new BuildTargetIdentifier("//target_without_jvm_flags:binary"), List.of());

    ResourcesItem targetWithoutMainClassLibrary =
        new ResourcesItem(
            new BuildTargetIdentifier("//target_without_main_class:library"), List.of());

    ResourcesResult expectedResourcesResult =
        new ResourcesResult(
            List.of(
                bspWorkspaceRoot,
                targetWithResources,
                javaTargetsSubpackageJavaLibrary,
                javaTargetsJavaBinary,
                javaTargetsJavaLibrary,
                javaTargetsJavaLibraryExported,
                scalaTargetsScalaBinary,
                scalaTargetsScalaTest,
                targetWithDependencyJavaBinary,
                targetWithoutArgsBinary,
                targetWithoutJvmFlagsBinary,
                targetWithoutMainClassLibrary));

    ResourcesParams resourcesParams = new ResourcesParams(getExpectedTargetIdentifiers());

    return new BazelBspTestScenarioStep(
        "resources results",
        () ->
            testClient.testResources(
                Duration.ofSeconds(30), resourcesParams, expectedResourcesResult));
  }

  private BazelBspTestScenarioStep inverseSourcesResults() {
    TextDocumentIdentifier inverseSourcesDocument =
        new TextDocumentIdentifier("file://$WORKSPACE/java_targets/JavaBinary.java");

    InverseSourcesResult expectedInverseSourcesResult =
        new InverseSourcesResult(List.of(new BuildTargetIdentifier("//java_targets:java_binary")));

    InverseSourcesParams inverseSourcesParams = new InverseSourcesParams(inverseSourcesDocument);

    return new BazelBspTestScenarioStep(
        "inverse sources results",
        () ->
            testClient.testInverseSources(
                Duration.ofSeconds(30), inverseSourcesParams, expectedInverseSourcesResult));
  }

  // FIXME: Is it even correct? Here when queried for *all* targets we return only the ones that
  //   are actually Scala ones. It kinda makes sense, but it seems to be inconsistent.
  private BazelBspTestScenarioStep scalaMainClasses() {

    ScalaMainClassesParams scalaMainClassesParams =
        new ScalaMainClassesParams(getExpectedTargetIdentifiers());

    ScalaMainClassesItem scalaTargetsScalaBinary =
        new ScalaMainClassesItem(
            new BuildTargetIdentifier("//scala_targets:scala_binary"),
            List.of(
                new ScalaMainClass(
                    "example.Example", List.of("arg1", "arg2"), List.of("-Xms2G -Xmx5G"))));

    ScalaMainClassesItem targetWithoutJvmFlagsBinary =
        new ScalaMainClassesItem(
            new BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            List.of(new ScalaMainClass("example.Example", List.of("arg1", "arg2"), List.of())));

    ScalaMainClassesItem targetWithoutArgsBinary =
        new ScalaMainClassesItem(
            new BuildTargetIdentifier("//target_without_args:binary"),
            List.of(new ScalaMainClass("example.Example", List.of(), List.of("-Xms2G -Xmx5G"))));

    // FIXME: I'd like to add a test case where target's environment variables field is non-null.
    //  But how can I even populate it?
    //  Apparently it is populated in JVM run environment?

    ScalaMainClassesResult expectedScalaMainClassesResult =
        new ScalaMainClassesResult(
            List.of(scalaTargetsScalaBinary, targetWithoutJvmFlagsBinary, targetWithoutArgsBinary));

    return new BazelBspTestScenarioStep(
        "Scala main classes",
        () ->
            testClient.testScalaMainClasses(
                Duration.ofSeconds(30), scalaMainClassesParams, expectedScalaMainClassesResult));
  }

  // FIXME: Re-add a spec2 test target. But that requires messing with the bazel toolchain
  //  See:
  // https://github.com/bazelbuild/rules_scala/tree/9b85affa2e08a350a4315882b602eda55b262356/examples/testing/multi_frameworks_toolchain
  //  See: https://github.com/JetBrains/bazel-bsp/issues/96
  private BazelBspTestScenarioStep scalaTestClasses() {
    ScalaTestClassesParams scalaTestClassesParams =
        new ScalaTestClassesParams(getExpectedTargetIdentifiers());

    ScalaTestClassesItem scalaTagetsScalaTest =
        new ScalaTestClassesItem(
            new BuildTargetIdentifier("//scala_targets:scala_test"),
            List.of("io.bazel.rulesscala.scala_test.Runner"));

    ScalaTestClassesResult expectedScalaTestClassesResult =
        new ScalaTestClassesResult(List.of(scalaTagetsScalaTest));

    return new BazelBspTestScenarioStep(
        "Scala test classes",
        () ->
            testClient.testScalaTestClasses(
                Duration.ofSeconds(30), scalaTestClassesParams, expectedScalaTestClassesResult));
  }

  private BazelBspTestScenarioStep dependencySourcesResults() {
    DependencySourcesItem javaTargetsJavaBinary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//java_targets:java_binary"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/java_binary-src.jar"));

    DependencySourcesItem javaTargetsJavaLibrary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//java_targets:java_library"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library-src.jar"));

    DependencySourcesItem targetWithDependencyJavaBinary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//target_with_dependency:java_binary"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library_exported-src.jar",
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library-src.jar",
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_dependency/java_binary-src.jar",
                "file://$BAZEL_CACHE/external/guava/guava-28.0-jre-src.jar"));

    DependencySourcesItem javaTargetsSubpackageJavaLibrary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//java_targets/subpackage:java_library"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library-src.jar"));

    DependencySourcesItem scalaTargetsScalaBinary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//scala_targets:scala_binary"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_binary-src.jar"));

    DependencySourcesItem scalaTargetsScalaTest =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//scala_targets:scala_test"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_test-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9-src.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9-src.jar"));

    DependencySourcesItem targetWithResourcesJavaBinary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//target_with_resources:java_binary"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_resources/java_binary-src.jar"));

    DependencySourcesItem targetWithoutArgsBinary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//target_without_args:binary"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_args/binary-src.jar"));

    DependencySourcesItem targetWithoutJvmFlagsBinary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_jvm_flags/binary-src.jar"));

    DependencySourcesItem targetWithoutMainClassLibrary =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//target_without_main_class:library"),
            List.of(
                "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_main_class/library-src.jar"));

    DependencySourcesItem javaTargetsJavaLibraryExported =
        new DependencySourcesItem(
            new BuildTargetIdentifier("//java_targets:java_library_exported"), List.of());

    DependencySourcesItem bspWorkspaceRoot =
        new DependencySourcesItem(new BuildTargetIdentifier("bsp-workspace-root"), List.of());

    DependencySourcesResult expectedDependencies =
        new DependencySourcesResult(
            List.of(
                javaTargetsJavaBinary,
                javaTargetsJavaLibrary,
                targetWithDependencyJavaBinary,
                javaTargetsSubpackageJavaLibrary,
                scalaTargetsScalaBinary,
                scalaTargetsScalaTest,
                targetWithResourcesJavaBinary,
                targetWithoutArgsBinary,
                targetWithoutJvmFlagsBinary,
                targetWithoutMainClassLibrary,
                javaTargetsJavaLibraryExported,
                bspWorkspaceRoot));

    DependencySourcesParams dependencySourcesParams =
        new DependencySourcesParams(getExpectedTargetIdentifiers());

    return new BazelBspTestScenarioStep(
        "dependency sources results",
        () ->
            testClient.testDependencySources(
                Duration.ofSeconds(30), dependencySourcesParams, expectedDependencies));
  }

  // FIXME: Environment is always empty for now until we figure out how to handle it.
  // FIXME: Working directory is not an URI???
  // FIXME: Should this return only targets which are runnable?
  private BazelBspTestScenarioStep jvmRunEnvironment() {
    JvmRunEnvironmentParams params = new JvmRunEnvironmentParams(getExpectedTargetIdentifiers());

    JvmRunEnvironmentResult expectedResult =
        new JvmRunEnvironmentResult(
            List.of(
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//java_targets:java_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/java_binary.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//java_targets:java_library"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_with_dependency:java_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_dependency/java_binary.jar",
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar",
                        "file://$BAZEL_CACHE/external/guava/guava-28.0-jre.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//java_targets/subpackage:java_library"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//scala_targets:scala_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_binary.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of("-Xms2G -Xmx5G"),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//scala_targets:scala_test"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_test.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_with_resources:java_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_resources/java_binary.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_without_args:binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_args/binary.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of("-Xms2G -Xmx5G"),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_without_jvm_flags:binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_jvm_flags/binary.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_without_main_class:library"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_main_class/library.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of())));

    return new BazelBspTestScenarioStep(
        "jvm run environment results",
        () -> testClient.testJvmRunEnvironment(Duration.ofSeconds(30), params, expectedResult));
  }

  // FIXME: Should this return only targets that are actually testable?
  private BazelBspTestScenarioStep jvmTestEnvironment() {
    JvmTestEnvironmentParams params = new JvmTestEnvironmentParams(getExpectedTargetIdentifiers());

    JvmTestEnvironmentResult expectedResult =
        new JvmTestEnvironmentResult(
            List.of(
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//java_targets:java_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/java_binary.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//java_targets:java_library"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/libjava_library.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_with_dependency:java_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_dependency/java_binary.jar",
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar",
                        "file://$BAZEL_CACHE/external/guava/guava-28.0-jre.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//java_targets/subpackage:java_library"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/java_targets/subpackage/libjava_library.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//scala_targets:scala_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_binary.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of("-Xms2G -Xmx5G"),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//scala_targets:scala_test"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/scala_targets/scala_test.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalactic/scalactic_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest/scalatest_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_compatible/scalatest-compatible-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_core/scalatest-core_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_featurespec/scalatest-featurespec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_flatspec/scalatest-flatspec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_freespec/scalatest-freespec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funspec/scalatest-funspec_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_funsuite/scalatest-funsuite_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_matchers_core/scalatest-matchers-core_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_mustmatchers/scalatest-mustmatchers_2.12-3.2.9.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scalatest_shouldmatchers/scalatest-shouldmatchers_2.12-3.2.9.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_with_resources:java_binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_with_resources/java_binary.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_without_args:binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_args/binary.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of("-Xms2G -Xmx5G"),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_without_jvm_flags:binary"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_jvm_flags/binary.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of()),
                new JvmEnvironmentItem(
                    new BuildTargetIdentifier("//target_without_main_class:library"),
                    List.of(
                        "file://$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/target_without_main_class/library.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                        "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"),
                    List.of(),
                    "$WORKSPACE",
                    Map.of())));

    return new BazelBspTestScenarioStep(
        "jvm test environment results",
        () -> testClient.testJvmTestEnvironment(Duration.ofSeconds(30), params, expectedResult));
  }

  private List<BuildTargetIdentifier> getExpectedTargetIdentifiers() {
    return getExpectedWorkspaceBuildTargetsResult().getTargets().stream()
        .map(BuildTarget::getId)
        .collect(Collectors.toList());
  }

  private WorkspaceBuildTargetsResult getExpectedWorkspaceBuildTargetsResult() {

    JvmBuildTarget jvmBuildTarget =
        new JvmBuildTarget("file://$BAZEL_CACHE/external/remotejdk11_$OS/", "11");

    BuildTarget javaTargetsJavaBinary =
        new BuildTarget(
            new BuildTargetIdentifier("//java_targets:java_binary"),
            List.of("application"),
            List.of("java"),
            List.of(),
            new BuildTargetCapabilities(true, false, true, false));

    javaTargetsJavaBinary.setDisplayName("//java_targets:java_binary");
    javaTargetsJavaBinary.setBaseDirectory("file://$WORKSPACE/java_targets/");
    javaTargetsJavaBinary.setDataKind("jvm");
    javaTargetsJavaBinary.setData(jvmBuildTarget);

    ScalaBuildTarget scalaBuildTarget =
        new ScalaBuildTarget(
            "org.scala-lang",
            "2.12.14",
            "2.12",
            ScalaPlatform.JVM,
            List.of(
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_compiler/scala-compiler-2.12.14.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_library/scala-library-2.12.14.jar",
                "file://$BAZEL_CACHE/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.14.jar"));

    scalaBuildTarget.setJvmBuildTarget(jvmBuildTarget);

    BuildTarget scalaTargetsScalaBinary =
        new BuildTarget(
            new BuildTargetIdentifier("//scala_targets:scala_binary"),
            List.of("application"),
            List.of("scala"),
            List.of(),
            new BuildTargetCapabilities(true, false, true, false));

    scalaTargetsScalaBinary.setDisplayName("//scala_targets:scala_binary");
    scalaTargetsScalaBinary.setBaseDirectory("file://$WORKSPACE/scala_targets/");
    scalaTargetsScalaBinary.setDataKind("scala");
    scalaTargetsScalaBinary.setData(scalaBuildTarget);

    BuildTarget javaTargetsSubpackageSubpackage =
        new BuildTarget(
            new BuildTargetIdentifier("//java_targets/subpackage:java_library"),
            List.of("library"),
            List.of("java"),
            List.of(),
            new BuildTargetCapabilities(true, false, false, false));
    javaTargetsSubpackageSubpackage.setDisplayName("//java_targets/subpackage:java_library");
    javaTargetsSubpackageSubpackage.setBaseDirectory("file://$WORKSPACE/java_targets/subpackage/");
    javaTargetsSubpackageSubpackage.setDataKind("jvm");
    javaTargetsSubpackageSubpackage.setData(jvmBuildTarget);

    BuildTarget javaTargetsJavaLibrary =
        new BuildTarget(
            new BuildTargetIdentifier("//java_targets:java_library"),
            List.of("library"),
            List.of("java"),
            List.of(),
            new BuildTargetCapabilities(true, false, false, false));
    javaTargetsJavaLibrary.setDisplayName("//java_targets:java_library");
    javaTargetsJavaLibrary.setBaseDirectory("file://$WORKSPACE/java_targets/");
    javaTargetsJavaLibrary.setDataKind("jvm");
    javaTargetsJavaLibrary.setData(jvmBuildTarget);

    BuildTarget targetWithoutJvmFlagsBinary =
        new BuildTarget(
            new BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            List.of("application"),
            List.of("scala"),
            List.of(),
            new BuildTargetCapabilities(true, false, true, false));
    targetWithoutJvmFlagsBinary.setDisplayName("//target_without_jvm_flags:binary");
    targetWithoutJvmFlagsBinary.setBaseDirectory("file://$WORKSPACE/target_without_jvm_flags/");
    targetWithoutJvmFlagsBinary.setDataKind("scala");
    targetWithoutJvmFlagsBinary.setData(scalaBuildTarget);

    BuildTarget targetWithoutMainClassLibrary =
        new BuildTarget(
            new BuildTargetIdentifier("//target_without_main_class:library"),
            List.of("library"),
            List.of("scala"),
            List.of(),
            new BuildTargetCapabilities(true, false, false, false));
    targetWithoutMainClassLibrary.setDisplayName("//target_without_main_class:library");
    targetWithoutMainClassLibrary.setBaseDirectory("file://$WORKSPACE/target_without_main_class/");
    targetWithoutMainClassLibrary.setDataKind("scala");
    targetWithoutMainClassLibrary.setData(scalaBuildTarget);

    BuildTarget targetWithoutArgsBinary =
        new BuildTarget(
            new BuildTargetIdentifier("//target_without_args:binary"),
            List.of("application"),
            List.of("scala"),
            List.of(),
            new BuildTargetCapabilities(true, false, true, false));
    targetWithoutArgsBinary.setDisplayName("//target_without_args:binary");
    targetWithoutArgsBinary.setBaseDirectory("file://$WORKSPACE/target_without_args/");
    targetWithoutArgsBinary.setDataKind("scala");
    targetWithoutArgsBinary.setData(scalaBuildTarget);

    BuildTarget targetWithDependencyJavaBinary =
        new BuildTarget(
            new BuildTargetIdentifier("//target_with_dependency:java_binary"),
            List.of("application"),
            List.of("java"),
            List.of(
                new BuildTargetIdentifier("//java_targets:java_library_exported"),
                new BuildTargetIdentifier("@guava//:guava"),
                new BuildTargetIdentifier("//java_targets/subpackage:java_library")),
            new BuildTargetCapabilities(true, false, true, false));
    targetWithDependencyJavaBinary.setDisplayName("//target_with_dependency:java_binary");
    targetWithDependencyJavaBinary.setBaseDirectory("file://$WORKSPACE/target_with_dependency/");
    targetWithDependencyJavaBinary.setDataKind("jvm");
    targetWithDependencyJavaBinary.setData(jvmBuildTarget);

    BuildTarget scalaTargetsScalaTest =
        new BuildTarget(
            new BuildTargetIdentifier("//scala_targets:scala_test"),
            List.of("test"),
            List.of("scala"),
            List.of(),
            new BuildTargetCapabilities(true, true, false, false));
    scalaTargetsScalaTest.setDisplayName("//scala_targets:scala_test");
    scalaTargetsScalaTest.setBaseDirectory("file://$WORKSPACE/scala_targets/");
    scalaTargetsScalaTest.setDataKind("scala");
    scalaTargetsScalaTest.setData(scalaBuildTarget);

    BuildTarget targetWithResourcesJavaBinary =
        new BuildTarget(
            new BuildTargetIdentifier("//target_with_resources:java_binary"),
            List.of("application"),
            List.of("java"),
            List.of(),
            new BuildTargetCapabilities(true, false, true, false));
    targetWithResourcesJavaBinary.setDisplayName("//target_with_resources:java_binary");
    targetWithResourcesJavaBinary.setBaseDirectory("file://$WORKSPACE/target_with_resources/");
    targetWithResourcesJavaBinary.setDataKind("jvm");
    targetWithResourcesJavaBinary.setData(jvmBuildTarget);

    BuildTarget javaTargetsJavaLibraryExported =
        new BuildTarget(
            new BuildTargetIdentifier("//java_targets:java_library_exported"),
            List.of("library"),
            List.of(),
            List.of(new BuildTargetIdentifier("//java_targets/subpackage:java_library")),
            new BuildTargetCapabilities(true, false, false, false));
    javaTargetsJavaLibraryExported.setDisplayName("//java_targets:java_library_exported");
    javaTargetsJavaLibraryExported.setBaseDirectory("file://$WORKSPACE/java_targets/");

    BuildTarget bspWorkspaceRoot =
        new BuildTarget(
            new BuildTargetIdentifier("bsp-workspace-root"),
            List.of(),
            List.of(),
            List.of(),
            new BuildTargetCapabilities(false, false, false, false));

    bspWorkspaceRoot.setDisplayName("bsp-workspace-root");
    bspWorkspaceRoot.setBaseDirectory("file://$WORKSPACE/");

    return new WorkspaceBuildTargetsResult(
        List.of(
            javaTargetsJavaBinary,
            scalaTargetsScalaBinary,
            javaTargetsSubpackageSubpackage,
            javaTargetsJavaLibrary,
            targetWithoutJvmFlagsBinary,
            targetWithoutMainClassLibrary,
            targetWithoutArgsBinary,
            targetWithDependencyJavaBinary,
            scalaTargetsScalaTest,
            targetWithResourcesJavaBinary,
            javaTargetsJavaLibraryExported,
            bspWorkspaceRoot));
  }
}
