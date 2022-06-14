package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.*
import com.google.common.collect.ImmutableList
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.time.Duration

class BazelBspLocalJdkTest : BazelBspTestBaseScenario(REPO_NAME) {

    override fun getScenarioSteps(): List<BazelBspTestScenarioStep> = ImmutableList.of(workspaceBuildTargets())

    private fun workspaceBuildTargets(): BazelBspTestScenarioStep {

        val exampleExampleJvmBuildTarget = JvmBuildTarget("file://\$BAZEL_CACHE/external/local_jdk/", "17")
        val rootBuildTarget = BuildTarget(
                BuildTargetIdentifier("bsp-workspace-root"),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                BuildTargetCapabilities(false, false, false, false))
        rootBuildTarget.displayName = "bsp-workspace-root"
        rootBuildTarget.baseDirectory = "file://\$WORKSPACE/"
        val exampleExampleBuildTarget = BuildTarget(
                BuildTargetIdentifier("//example:example"),
                ImmutableList.of("application"),
                ImmutableList.of("java"),
                ImmutableList.of(),
                BuildTargetCapabilities(true, false, true, false))
        exampleExampleBuildTarget.displayName = "//example:example"
        exampleExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        exampleExampleBuildTarget.data = exampleExampleJvmBuildTarget
        exampleExampleBuildTarget.dataKind = BuildTargetDataKind.JVM
        val workspaceBuildTargetsResult = WorkspaceBuildTargetsResult(
                ImmutableList.of(rootBuildTarget, exampleExampleBuildTarget))
        return BazelBspTestScenarioStep(
                "local-jdk-project workspace build targets"
        ) { testClient.testWorkspaceTargets(Duration.ofSeconds(30), workspaceBuildTargetsResult) }
    }


    companion object {
        private const val REPO_NAME = "local-jdk-project"

        // we cannot use `bazel test ...` because test runner blocks bazel daemon,
        // but testing server needs it for queries and etc
        @JvmStatic
        fun main(args: Array<String>) {
            val test = BazelBspLocalJdkTest()
            test.executeScenario()
        }
    }
}