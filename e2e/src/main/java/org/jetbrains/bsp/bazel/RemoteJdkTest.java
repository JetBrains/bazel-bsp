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

public class RemoteJdkTest extends BazelBspTestBaseScenario {

  private static final String REPO_NAME = "remote-jdk-project";

  public RemoteJdkTest() {
    super(REPO_NAME);
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    RemoteJdkTest test = new RemoteJdkTest();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return ImmutableList.of(workspaceBuildTargets());
  }

  private BazelBspTestScenarioStep workspaceBuildTargets() {
    JvmBuildTarget exampleExampleJvmBuildTarget =
        new JvmBuildTarget("file://$BAZEL_CACHE/external/remotejdk11_macos/", "11");

    BuildTarget rootBuildTarget =
        new BuildTarget(
            new BuildTargetIdentifier("bsp-workspace-root"),
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(),
            new BuildTargetCapabilities(false, false, false, false));
    rootBuildTarget.setDisplayName("bsp-workspace-root");
    rootBuildTarget.setBaseDirectory("file://$WORKSPACE/");

    BuildTarget exampleExampleBuildTarget =
        new BuildTarget(
            new BuildTargetIdentifier("//example:example"),
            ImmutableList.of("application"),
            ImmutableList.of("java"),
            ImmutableList.of(),
            new BuildTargetCapabilities(true, false, true, false));
    exampleExampleBuildTarget.setDisplayName("//example:example");
    exampleExampleBuildTarget.setBaseDirectory("file://$WORKSPACE/example/");
    exampleExampleBuildTarget.setData(exampleExampleJvmBuildTarget);
    exampleExampleBuildTarget.setDataKind(BuildTargetDataKind.JVM);

    WorkspaceBuildTargetsResult workspaceBuildTargetsResult =
        new WorkspaceBuildTargetsResult(
            ImmutableList.of(rootBuildTarget, exampleExampleBuildTarget));

    return new BazelBspTestScenarioStep(
        "remote-jdk-project workspace build targets",
        () -> testClient.testWorkspaceTargets(Duration.ofSeconds(30), workspaceBuildTargetsResult));
  }
}
