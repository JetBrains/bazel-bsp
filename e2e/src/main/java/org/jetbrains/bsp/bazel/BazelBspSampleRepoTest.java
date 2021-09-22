package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.DependencySourcesItem;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.JvmBuildTarget;
import ch.epfl.scala.bsp4j.ResourcesItem;
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
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.TextDocumentIdentifier;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario;
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep;
import org.jetbrains.bsp.bazel.commons.Constants;

public class BazelBspSampleRepoTest extends BazelBspTestBaseScenario {

  private static final String REPO_NAME = "sample-repo";
  private static final Duration CLIENT_TIMEOUT = Duration.ofMinutes(8);

  public BazelBspSampleRepoTest() {
    super(REPO_NAME, CLIENT_TIMEOUT);
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    BazelBspSampleRepoTest test = new BazelBspSampleRepoTest();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return ImmutableList.of(
        resolveProject(),
        compareWorkspaceTargetsResults(),
        sourcesResults(),
        resourcesResults(),
        inverseSourcesResults(),
        dependencySourcesResults(),
        scalaMainClasses(),
        scalaTestClasses()
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
        "resolve project", () -> testClient.testResolveProject(false, false));
  }

  private BazelBspTestScenarioStep compareWorkspaceTargetsResults() {
    WorkspaceBuildTargetsResult expectedWorkspaceBuildTargetsResult =
        getExpectedWorkspaceBuildTargetsResult();

    return new BazelBspTestScenarioStep(
        "compare workspace targets results",
        () -> testClient.testCompareWorkspaceTargetsResults(expectedWorkspaceBuildTargetsResult));
  }

  private BazelBspTestScenarioStep sourcesResults() {
    WorkspaceBuildTargetsResult expectedWorkspaceBuildTargetsResult =
        getExpectedWorkspaceBuildTargetsResult();

    String exampleScalaSourceUri = String.format("%s/example/Example.scala", REPO_NAME);
    SourceItem exampleScalaSource =
        new SourceItem(exampleScalaSourceUri, SourceItemKind.FILE, false);

    SourcesItem exampleExampleSources =
        new SourcesItem(
            new BuildTargetIdentifier("//example:example"), ImmutableList.of(exampleScalaSource));

    String testScalaUri = String.format("%s/dep/Test.scala", REPO_NAME);
    SourceItem testScalaSource = new SourceItem(testScalaUri, SourceItemKind.FILE, false);

    String javaTestJavaUri = String.format("%s/dep/JavaTest.java", REPO_NAME);
    SourceItem javaTestJavaSource = new SourceItem(javaTestJavaUri, SourceItemKind.FILE, false);

    String depScalaUri = String.format("%s/dep/Dep.scala", REPO_NAME);
    SourceItem depScalaSource = new SourceItem(depScalaUri, SourceItemKind.FILE, false);

    SourcesItem depDepSources =
        new SourcesItem(
            new BuildTargetIdentifier("//dep:dep"),
            ImmutableList.of(testScalaSource, javaTestJavaSource, depScalaSource));

    SourcesResult expectedSourcesResult =
        new SourcesResult(ImmutableList.of(exampleExampleSources, depDepSources));

    return new BazelBspTestScenarioStep(
        "sources results",
        () ->
            testClient.testSourcesResults(
                expectedWorkspaceBuildTargetsResult, expectedSourcesResult));
  }

  private BazelBspTestScenarioStep resourcesResults() {
    WorkspaceBuildTargetsResult expectedWorkspaceBuildTargetsResult =
        getExpectedWorkspaceBuildTargetsResult();

    String fileTxtUri = String.format("%s/example/file.txt", REPO_NAME);
    String file2TxtUri = String.format("%s/example/file2.txt", REPO_NAME);

    ResourcesItem exampleExampleResource =
        new ResourcesItem(
            new BuildTargetIdentifier("//example:example"),
            ImmutableList.of(fileTxtUri, file2TxtUri));

    ResourcesResult expectedResourcesResult =
        new ResourcesResult(ImmutableList.of(exampleExampleResource));

    return new BazelBspTestScenarioStep(
        "resources results",
        () ->
            testClient.testResourcesResults(
                expectedWorkspaceBuildTargetsResult, expectedResourcesResult));
  }

  private BazelBspTestScenarioStep inverseSourcesResults() {
    String depScalaUri = String.format("file://%s/dep/Dep.scala", getTestingRepoPath(REPO_NAME));
    TextDocumentIdentifier inverseSourcesDocument = new TextDocumentIdentifier(depScalaUri);

    InverseSourcesResult expectedInverseSourcesResult =
        new InverseSourcesResult(ImmutableList.of(new BuildTargetIdentifier("//dep:dep")));

    return new BazelBspTestScenarioStep(
        "inverse sources results",
        () ->
            testClient.testInverseSourcesResults(
                inverseSourcesDocument, expectedInverseSourcesResult));
  }

  private BazelBspTestScenarioStep scalaMainClasses() {
    ScalaMainClassesParams scalaMainClassesParams =
        new ScalaMainClassesParams(
            ImmutableList.of(
                new BuildTargetIdentifier("//example:example"),
                new BuildTargetIdentifier("//target_without_main_class:library"),
                new BuildTargetIdentifier("//target_without_args:binary"),
                new BuildTargetIdentifier("//target_without_jvm_flags:binary")));

    ScalaMainClass exampleExampleMainClass =
        new ScalaMainClass(
            "example.Example", ImmutableList.of("arg1", "arg2"), ImmutableList.of("-Xms2G -Xmx5G"));
    ScalaMainClassesItem exampleExampleMainClasses =
        new ScalaMainClassesItem(
            new BuildTargetIdentifier("//example:example"),
            ImmutableList.of(exampleExampleMainClass));

    ScalaMainClass withoutArgsBinaryMainClass =
        new ScalaMainClass(
            "example.Example", ImmutableList.of(), ImmutableList.of("-Xms2G -Xmx5G"));
    ScalaMainClassesItem withoutArgsBinaryMainClasses =
        new ScalaMainClassesItem(
            new BuildTargetIdentifier("//target_without_args:binary"),
            ImmutableList.of(withoutArgsBinaryMainClass));

    ScalaMainClass withoutJvmFlagsBinaryMainClass =
        new ScalaMainClass("example.Example", ImmutableList.of("arg1", "arg2"), ImmutableList.of());
    ScalaMainClassesItem withoutJvmFlagsBinaryMainClasses =
        new ScalaMainClassesItem(
            new BuildTargetIdentifier("//target_without_jvm_flags:binary"),
            ImmutableList.of(withoutJvmFlagsBinaryMainClass));

    ScalaMainClassesResult expectedScalaMainClassesResult =
        new ScalaMainClassesResult(
            ImmutableList.of(
                exampleExampleMainClasses,
                withoutArgsBinaryMainClasses,
                withoutJvmFlagsBinaryMainClasses));

    return new BazelBspTestScenarioStep(
        "Scala main classes",
        () ->
            testClient.testScalaMainClasses(
                scalaMainClassesParams, expectedScalaMainClassesResult));
  }

  private BazelBspTestScenarioStep scalaTestClasses() {
    ScalaTestClassesParams scalaTestClassesParams =
        new ScalaTestClassesParams(
            ImmutableList.of(
                new BuildTargetIdentifier("//example:example"),
                new BuildTargetIdentifier("//example:example-test"),
                new BuildTargetIdentifier("//example:example-spec2-test")));

    ScalaTestClassesItem exampleExampleTestTestClasses =
        new ScalaTestClassesItem(
            new BuildTargetIdentifier("//example:example-test"),
            ImmutableList.of("example.ExampleTest"));

    // TODO (https://github.com/JetBrains/bazel-bsp/issues/96)
    ScalaTestClassesItem exampleExampleSpec2TestTestClasses =
        new ScalaTestClassesItem(
            new BuildTargetIdentifier("//example:example-spec2-test"), ImmutableList.of());

    ScalaTestClassesResult expectedScalaTestClassesResult =
        new ScalaTestClassesResult(
            ImmutableList.of(exampleExampleTestTestClasses, exampleExampleSpec2TestTestClasses));

    return new BazelBspTestScenarioStep(
        "Scala test classes",
        () ->
            testClient.testScalaTestClasses(
                scalaTestClassesParams, expectedScalaTestClassesResult));
  }

  private BazelBspTestScenarioStep dependencySourcesResults() {
    WorkspaceBuildTargetsResult expectedWorkspaceBuildTargetsResult =
        getExpectedWorkspaceBuildTargetsResult();

    List<String> dependencies =
        ImmutableList.of(
            "https/repo1.maven.org/maven2/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3-sources.jar",
            "https/repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2-sources.jar",
            "https/repo1.maven.org/maven2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1-sources.jar",
            "https/repo1.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.3.2/error_prone_annotations-2.3.2-sources.jar",
            "https/repo1.maven.org/maven2/org/codehaus/mojo/animal-sniffer-annotations/1.17/animal-sniffer-annotations-1.17-sources.jar",
            "https/repo1.maven.org/maven2/org/checkerframework/checker-qual/2.8.1/checker-qual-2.8.1-sources.jar",
            "https/repo1.maven.org/maven2/com/google/guava/guava/28.0-jre/guava-28.0-jre-sources.jar");

    DependencySourcesItem exampleExampleDependencies =
        new DependencySourcesItem(new BuildTargetIdentifier("//example:example"), dependencies);

    DependencySourcesItem depDepDependencies =
        new DependencySourcesItem(new BuildTargetIdentifier("//dep:dep"), dependencies);

    DependencySourcesResult expectedDependencies =
        new DependencySourcesResult(
            ImmutableList.of(exampleExampleDependencies, depDepDependencies));

    return new BazelBspTestScenarioStep(
        "dependency sources results",
        () ->
            testClient.testDependencySourcesResults(
                expectedWorkspaceBuildTargetsResult, expectedDependencies));
  }

  private WorkspaceBuildTargetsResult getExpectedWorkspaceBuildTargetsResult() {
    List<String> scalaTargetsJars =
        ImmutableList.of(
            "__main__/external/io_bazel_rules_scala_scala_compiler/scala-compiler-2.12.8.jar",
            "__main__/external/io_bazel_rules_scala_scala_library/scala-library-2.12.8.jar",
            "__main__/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.8.jar");

    ScalaBuildTarget scalaTarget =
        new ScalaBuildTarget(
            "org.scala-lang", "2.12.8", "2.12", ScalaPlatform.JVM, scalaTargetsJars);
    scalaTarget.setJvmBuildTarget(new JvmBuildTarget(null, "8"));

    BuildTarget exampleExampleTarget =
        new BuildTarget(
            new BuildTargetIdentifier("//example:example"),
            ImmutableList.of(),
            ImmutableList.of(Constants.SCALA),
            ImmutableList.of(new BuildTargetIdentifier("//dep:dep")),
            new BuildTargetCapabilities(true, false, true));
    exampleExampleTarget.setData(scalaTarget);
    exampleExampleTarget.setDataKind(BuildTargetDataKind.SCALA);

    BuildTarget depDepTarget =
        new BuildTarget(
            new BuildTargetIdentifier("//dep:dep"),
            ImmutableList.of(),
            ImmutableList.of(Constants.JAVA, Constants.SCALA),
            ImmutableList.of(new BuildTargetIdentifier("//dep/deeper:deeper")),
            new BuildTargetCapabilities(true, false, false));
    depDepTarget.setData(scalaTarget);
    depDepTarget.setDataKind(BuildTargetDataKind.SCALA);

    BuildTarget depDeeperExportTarget =
        new BuildTarget(
            new BuildTargetIdentifier("//dep:deeper-export"),
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(new BuildTargetIdentifier("//dep/deeper:deeper")),
            new BuildTargetCapabilities(true, false, false));

    return new WorkspaceBuildTargetsResult(
        ImmutableList.of(exampleExampleTarget, depDepTarget, depDeeperExportTarget));
  }
}
