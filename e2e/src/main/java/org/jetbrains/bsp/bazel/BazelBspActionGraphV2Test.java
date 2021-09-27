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

public class BazelBspActionGraphV2Test extends BazelBspTestBaseScenario {

  private static final String REPO_NAME = "action-graph-v2";
  private static final Duration CLIENT_TIMEOUT = Duration.ofMinutes(5);

  public BazelBspActionGraphV2Test() {
    super(REPO_NAME, CLIENT_TIMEOUT);
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    BazelBspActionGraphV2Test test = new BazelBspActionGraphV2Test();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return ImmutableList.of(actionGraphV2JavacOptions(), actionGraphV2ScalacOptions());
  }

  private BazelBspTestScenarioStep actionGraphV2JavacOptions() {
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

    JavacOptionsResult expectedJavacOptionsResult =
        new JavacOptionsResult(ImmutableList.of(exampleExampleJavacOptions));

    return new BazelBspTestScenarioStep(
        "action-graph-v2 javac options",
        () -> testClient.testJavacOptions(javacOptionsParams, expectedJavacOptionsResult));
  }

  private BazelBspTestScenarioStep actionGraphV2ScalacOptions() {
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
                "/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac.jar"),
            "bin/example/");

    ScalacOptionsResult expectedScalacOptionsResult =
        new ScalacOptionsResult(ImmutableList.of(exampleExampleScalacOptions));

    return new BazelBspTestScenarioStep(
        "action-graph-v2 scalac options",
        () -> testClient.testScalacOptions(scalacOptionsParams, expectedScalacOptionsResult));
  }
}
