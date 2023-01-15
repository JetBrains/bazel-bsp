package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.time.Duration

object BazelBspPythonLibraryProjectTest : BazelBspTestBaseScenario() {

    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun repoName(): String = "python-library-project"

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(workspaceBuildTargets())

    private fun workspaceBuildTargets(): BazelBspTestScenarioStep {

        val examplePythonBuildTarget = PythonBuildTarget("PY3", "bazel-out/k8-fastbuild/bin/external/bazel_tools/tools/python/py3wrapper.sh")

        val exampleExampleBuildTarget = BuildTarget(
            BuildTargetIdentifier("//example:example"),
            listOf("application"),
            listOf("python"),
            listOf(BuildTargetIdentifier("//lib:calculators")),
            BuildTargetCapabilities(true, false, true, false)
        )
        exampleExampleBuildTarget.displayName = "//example:example"
        exampleExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        exampleExampleBuildTarget.data = examplePythonBuildTarget
        exampleExampleBuildTarget.dataKind = BuildTargetDataKind.PYTHON

        val bspWorkspaceRootExampleBuildTarget =
            BuildTarget(
                BuildTargetIdentifier("bsp-workspace-root"),
                listOf(),
                listOf(),
                listOf(),
                BuildTargetCapabilities(false, false, false, false)
            )
        bspWorkspaceRootExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/"
        bspWorkspaceRootExampleBuildTarget.displayName = "bsp-workspace-root"

        val workspaceBuildTargetsResult = WorkspaceBuildTargetsResult(
            listOf(exampleExampleBuildTarget, bspWorkspaceRootExampleBuildTarget)
        )
        return BazelBspTestScenarioStep("workspace build targets") {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(60),
                workspaceBuildTargetsResult
            )
        }
    }


}