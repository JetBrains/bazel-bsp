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

        val examplePythonBuildTarget =
            PythonBuildTarget(
                "PY3",
                "file://\$BAZEL_CACHE/bazel-out/k8-fastbuild/bin/external/bazel_tools/tools/python/py3wrapper.sh"
            )

        val exampleExampleBuildTarget = BuildTarget(
            BuildTargetIdentifier("//example:example"),
            listOf("application"),
            listOf("python"),
            listOf(BuildTargetIdentifier("//lib:example_library")),
            BuildTargetCapabilities(true, false, true, false)
        )
        exampleExampleBuildTarget.displayName = "//example:example"
        exampleExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        exampleExampleBuildTarget.data = examplePythonBuildTarget
        exampleExampleBuildTarget.dataKind = BuildTargetDataKind.PYTHON

        val examplePythonLibBuildTarget =
            PythonBuildTarget(null, null)

        val exampleExampleLibBuildTarget = BuildTarget(
            BuildTargetIdentifier("//lib:example_library"),
            listOf("library"),
            listOf("python"),
            listOf(),
            BuildTargetCapabilities(true, false, false, false)
        )
        exampleExampleLibBuildTarget.displayName = "//lib:example_library"
        exampleExampleLibBuildTarget.baseDirectory = "file://\$WORKSPACE/lib/"
        exampleExampleLibBuildTarget.data = examplePythonLibBuildTarget
        exampleExampleLibBuildTarget.dataKind = BuildTargetDataKind.PYTHON


        val examplePythonTestBuildTarget =
            PythonBuildTarget("PY3", "bazel-out/k8-fastbuild/bin/external/bazel_tools/tools/python/py3wrapper.sh")
        val exampleExampleTestBuildTarget = BuildTarget(
            BuildTargetIdentifier("//test:test"),
            listOf("test"),
            listOf("python"),
            listOf(),
            BuildTargetCapabilities(true, true, false, false)
        )
        exampleExampleTestBuildTarget.displayName = "//test:test"
        exampleExampleTestBuildTarget.baseDirectory = "file://\$WORKSPACE/test/"
        exampleExampleTestBuildTarget.data = examplePythonTestBuildTarget
        exampleExampleTestBuildTarget.dataKind = BuildTargetDataKind.PYTHON


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
            listOf(bspWorkspaceRootExampleBuildTarget, exampleExampleBuildTarget, exampleExampleLibBuildTarget, exampleExampleTestBuildTarget)
        )
        return BazelBspTestScenarioStep("workspace build targets") {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(60),
                workspaceBuildTargetsResult
            )
        }
    }


}