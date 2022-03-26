package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import ch.epfl.scala.bsp4j.ScalacOptionsItem;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario;
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep;

public class BazelBspActionGraphV1Test extends BazelBspTestBaseScenario {

  private static final String REPO_NAME = "sample-repo";

  public BazelBspActionGraphV1Test() {
    super(REPO_NAME);
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    BazelBspActionGraphV1Test test = new BazelBspActionGraphV1Test();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return ImmutableList.of(actionGraphV1JavacOptions(), actionGraphV1ScalacOptions());
  }

  private BazelBspTestScenarioStep actionGraphV1JavacOptions() {
    JavacOptionsParams javacOptionsParams =
        new JavacOptionsParams(
            ImmutableList.of(
                new BuildTargetIdentifier("//example:example"),
                new BuildTargetIdentifier("//dep:java-dep")));

    JavacOptionsItem exampleExampleJavacOptions =
        new JavacOptionsItem(
            new BuildTargetIdentifier("//example:example"),
            ImmutableList.of(),
            ImmutableList.of(),
            "");

    JavacOptionsItem depJavaDepJavacOptions =
        new JavacOptionsItem(
            new BuildTargetIdentifier("//dep:java-dep"),
            ImmutableList.of("-Werror", "-Xlint:all"),
            ImmutableList.of(),
            "");

    JavacOptionsResult expectedJavacOptionsResult =
        new JavacOptionsResult(
            ImmutableList.of(exampleExampleJavacOptions, depJavaDepJavacOptions));

    return new BazelBspTestScenarioStep(
        "action-graph-v1 javac options",
        () ->
            testClient.testJavacOptions(
                Duration.ofSeconds(20), javacOptionsParams, expectedJavacOptionsResult));
  }

  private BazelBspTestScenarioStep actionGraphV1ScalacOptions() {
    ScalacOptionsParams scalacOptionsParams =
        new ScalacOptionsParams(
            ImmutableList.of(
                new BuildTargetIdentifier("//example:example"),
                new BuildTargetIdentifier("//dep:dep")));

    ScalacOptionsItem exampleExampleScalacOptions =
        new ScalacOptionsItem(
            new BuildTargetIdentifier("//example:example"),
            ImmutableList.of("-target:jvm-1.8"),
            ImmutableList.of(
                "__main__/external/io_bazel_rules_scala_scala_library/scala-library-2.12.8.jar",
                "__main__/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.8.jar",
                "/bin/dep/deeper/deeper.jar",
                "/bin/dep/dep.jar"),
            "bin/example/");

    ScalacOptionsItem depDepScalacOptions =
        new ScalacOptionsItem(
            new BuildTargetIdentifier("//dep:dep"),
            ImmutableList.of(),
            ImmutableList.of(
                "__main__/external/io_bazel_rules_scala_scala_library/scala-library-2.12.8.jar",
                "__main__/external/io_bazel_rules_scala_scala_reflect/scala-reflect-2.12.8.jar",
                "bin/dep/deeper/deeper.jar"),
            "bin/dep/");

    ScalacOptionsResult expectedScalacOptionsResult =
        new ScalacOptionsResult(ImmutableList.of(exampleExampleScalacOptions, depDepScalacOptions));

    return new BazelBspTestScenarioStep(
        "action-graph-v1 scalac options",
        () ->
            testClient.testScalacOptions(
                Duration.ofSeconds(20), scalacOptionsParams, expectedScalacOptionsResult));
  }
}
