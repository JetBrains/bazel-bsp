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

public class LocalJdkTest extends BazelBspTestBaseScenario {

  private static final String REPO_NAME = "local-jdk-project";

  public LocalJdkTest() {
    super(REPO_NAME);
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    LocalJdkTest test = new LocalJdkTest();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return ImmutableList.of(workspaceBuildTargets());
  }

  private BazelBspTestScenarioStep workspaceBuildTargets() {
    // TODO this was always broken. We resolve local_jdk which is actual jdk installed
    // on the running machine. In my case as well as on CI it is Java 11. We were
    // returning this path annotated as Java 8, but actually it was java 11 anyway.
    // now it is detected because the version is actually checked rather than inferred
    // from heuristics.
    // We should figure out a way to enforce bazel to download runtime jdk that matches
    // expected number.
    var exampleExampleJvmBuildTarget =
        new JvmBuildTarget("file://$BAZEL_CACHE/external/local_jdk/", "17");

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
        "local-jdk-project workspace build targets",
        () -> testClient.testWorkspaceTargets(Duration.ofSeconds(30), workspaceBuildTargetsResult));
  }
}
