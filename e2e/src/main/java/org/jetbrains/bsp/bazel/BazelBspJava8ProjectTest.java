package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.JvmBuildTarget;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario;
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep;
import org.jetbrains.bsp.bazel.commons.Constants;

public class BazelBspJava8ProjectTest extends BazelBspTestBaseScenario {

  private static final String REPO_NAME = "java-8-project";

  public BazelBspJava8ProjectTest() {
    super(REPO_NAME);
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    BazelBspJava8ProjectTest test = new BazelBspJava8ProjectTest();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return ImmutableList.of(workspaceBuildTargets());
  }

  private BazelBspTestScenarioStep workspaceBuildTargets() {
    JvmBuildTarget exampleExampleJvmBuildTarget = new JvmBuildTarget("external/local_jdk/", "8");

    BuildTarget exampleExampleBuildTarget =
        new BuildTarget(
            new BuildTargetIdentifier("//example:example"),
            ImmutableList.of(),
            ImmutableList.of(Constants.JAVA),
            ImmutableList.of(),
            new BuildTargetCapabilities(true, false, true));
    exampleExampleBuildTarget.setData(exampleExampleJvmBuildTarget);
    exampleExampleBuildTarget.setDataKind(BuildTargetDataKind.JVM);

    WorkspaceBuildTargetsResult workspaceBuildTargetsResult =
        new WorkspaceBuildTargetsResult(ImmutableList.of(exampleExampleBuildTarget));

    return new BazelBspTestScenarioStep(
        "java-8-project workspace build targets",
        () -> testClient.testWorkspaceTargets(Duration.ofSeconds(30), workspaceBuildTargetsResult));
  }
}
