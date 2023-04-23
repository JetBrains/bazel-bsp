package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep

import java.time.Duration

object BazelBspPythonProjectTest : BazelBspTestBaseScenario() {

    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun repoName(): String = "python-project"

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(workspaceBuildTargets(),
        dependencySourcesResults(),
        pythonOptionsResults())

    private fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult {
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
                "file://\$BAZEL_CACHE/external/python3_9_x86_64-unknown-linux-gnu/bin/python3",
            )

        val examplePythonLibBuildTarget =
            PythonBuildTarget(null, null)

        val exampleExampleBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example:example"),
            listOf("application"),
            listOf("python"),
            listOf(
                BuildTargetIdentifier("$targetPrefix//lib:example_library"),
                BuildTargetIdentifier("@requests//:srcs")
            ),
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
            listOf(BuildTargetIdentifier("@pip_deps_numpy//:pkg")),
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
                exampleExampleTestBuildTarget,
            )
        )

        return workspaceBuildTargetsResult
    }

    private fun expectedTargetIdentifiers(): List<BuildTargetIdentifier> =
        expectedWorkspaceBuildTargetsResult()
            .targets
            .map { it.id }

    private fun workspaceBuildTargets(): BazelBspTestScenarioStep {
        val workspaceBuildTargetsResult = expectedWorkspaceBuildTargetsResult()

        return BazelBspTestScenarioStep("workspace build targets") {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(60),
                workspaceBuildTargetsResult
            )
        }
    }

    private fun dependencySourcesResults(): BazelBspTestScenarioStep {
        val examplePythonDependencySourcesItem = DependencySourcesItem(
            BuildTargetIdentifier("$targetPrefix//lib:example_library"),
            listOf(
                "file://\$BAZEL_CACHE/external/pip_deps_numpy/"
            )
        )

        val expectedTargetIdentifiers = expectedTargetIdentifiers().filter { it.uri == "@//lib:example_library" }
        val expectedDependencies = DependencySourcesResult(listOf(examplePythonDependencySourcesItem))
        val dependencySourcesParams = DependencySourcesParams(expectedTargetIdentifiers)

        return BazelBspTestScenarioStep(
            "dependency sources results"
        ) {
            testClient.testDependencySources(
                Duration.ofSeconds(30), dependencySourcesParams, expectedDependencies
            )
        }
    }

    private fun pythonOptionsResults(): BazelBspTestScenarioStep {
        val expectedTargetIdentifiers = expectedTargetIdentifiers().filter { it.uri != "bsp-workspace-root" }
        val expectedPythonOptionsItems = expectedTargetIdentifiers.map { PythonOptionsItem(it, emptyList()) }
        val expectedPythonOptionsResult = PythonOptionsResult(expectedPythonOptionsItems)
        val pythonOptionsParams = PythonOptionsParams(expectedTargetIdentifiers)

        return BazelBspTestScenarioStep(
            "pythonOptions results"
        ) {
            testClient.testPythonOptions(Duration.ofSeconds(30), pythonOptionsParams, expectedPythonOptionsResult)
        }
    }
}