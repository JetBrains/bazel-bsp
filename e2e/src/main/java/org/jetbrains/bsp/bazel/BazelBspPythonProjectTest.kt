package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.time.Duration

object BazelBspPythonProjectTest : BazelBspTestBaseScenario() {

    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun repoName(): String = "python-project"

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(workspaceBuildTargets())

    private fun workspaceBuildTargets(): BazelBspTestScenarioStep {

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

        val examplePythonBuildTarget =
            PythonBuildTarget(
                "PY3",
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/external/bazel_tools/tools/python/py3wrapper.sh"
            )

        val examplePythonLibBuildTarget =
            PythonBuildTarget(null, null)

        val exampleExampleBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example:example"),
            listOf("application"),
            listOf("python"),
            listOf(BuildTargetIdentifier("$targetPrefix//lib:example_library"),
                BuildTargetIdentifier("@requests//:srcs")),
            BuildTargetCapabilities(true, false, true, false)
        )
        exampleExampleBuildTarget.displayName = "$targetPrefix//example:example"
        exampleExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        exampleExampleBuildTarget.data = examplePythonBuildTarget
        exampleExampleBuildTarget.dataKind = BuildTargetDataKind.PYTHON

        val exampleExampleLibBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//lib:example_library"),
            listOf("library"),
            listOf("python"),
            listOf(),
            BuildTargetCapabilities(true, false, false, false)
        )
        exampleExampleLibBuildTarget.displayName = "$targetPrefix//lib:example_library"
        exampleExampleLibBuildTarget.baseDirectory = "file://\$WORKSPACE/lib/"
        exampleExampleLibBuildTarget.data = examplePythonLibBuildTarget
        exampleExampleLibBuildTarget.dataKind = BuildTargetDataKind.PYTHON

        val exampleExampleTestBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//test:test"),
            listOf("test"),
            listOf("python"),
            listOf(),
            BuildTargetCapabilities(true, true, false, false)
        )
        exampleExampleTestBuildTarget.displayName = "$targetPrefix//test:test"
        exampleExampleTestBuildTarget.baseDirectory = "file://\$WORKSPACE/test/"
        exampleExampleTestBuildTarget.data = examplePythonBuildTarget
        exampleExampleTestBuildTarget.dataKind = BuildTargetDataKind.PYTHON

        val workspaceBuildTargetsResult = WorkspaceBuildTargetsResult(
            listOf(
                bspWorkspaceRootExampleBuildTarget,
                exampleExampleBuildTarget,
                exampleExampleLibBuildTarget,
                exampleExampleTestBuildTarget
            )
        )
        return BazelBspTestScenarioStep("workspace build targets") {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(60),
                workspaceBuildTargetsResult
            )
        }
    }


}