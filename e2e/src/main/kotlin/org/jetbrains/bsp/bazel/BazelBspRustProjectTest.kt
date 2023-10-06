package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.RustCrateType
import ch.epfl.scala.bsp4j.RustDepKindInfo
import ch.epfl.scala.bsp4j.RustDependency
import ch.epfl.scala.bsp4j.RustPackage
import ch.epfl.scala.bsp4j.RustRawDependency
import ch.epfl.scala.bsp4j.RustTarget
import ch.epfl.scala.bsp4j.RustTargetKind
import ch.epfl.scala.bsp4j.RustWorkspaceParams
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import com.google.common.collect.ImmutableList
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import java.time.Duration

object BazelBspRustProjectTest : BazelBspTestBaseScenario() {

    @JvmStatic
    fun main(args: Array<String>) = executeScenario()

    override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
        workspaceBuildTargets(),
        rustWorkspaceResults(),
    )

    private fun workspaceBuildTargets(): BazelBspTestScenarioStep {
        val workspaceBuildTargetsResult = expectedWorkspaceBuildTargetsResult()

        return BazelBspTestScenarioStep("workspace build targets") {
            testClient.testWorkspaceTargets(
                Duration.ofSeconds(60),
                workspaceBuildTargetsResult
            )
        }
    }

    override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult =
        WorkspaceBuildTargetsResult(
            listOf(
                makeRoot(),
                makeExampleLib(),
                makeExampleFeature(),
                makeExample()
            )
        )

    private fun makeRoot(): BuildTarget {
        val bspWorkspaceRootExampleBuildTarget =
            BuildTarget(
                BuildTargetIdentifier("bsp-workspace-root"),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                BuildTargetCapabilities().also { it.canCompile = false; it.canTest = false; it.canRun = false; it.canDebug = false }
            )
        bspWorkspaceRootExampleBuildTarget.baseDirectory = "file://\$WORKSPACE/"
        bspWorkspaceRootExampleBuildTarget.displayName = "bsp-workspace-root"
        return bspWorkspaceRootExampleBuildTarget
    }

    private fun makeExampleLib(): BuildTarget {
        val exampleLibRustBuildTargetDependencies = listOf(
            BuildTargetIdentifier("@crate_index__serde-1.0.160//:serde"),
            BuildTargetIdentifier("@crate_index__serde_json-1.0.96//:serde_json")
        )
        val exampleLibRustBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
            listOf("library"),
            listOf("rust"),
            exampleLibRustBuildTargetDependencies,
            BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false }
        )
        exampleLibRustBuildTarget.displayName = "$targetPrefix//example-lib:example_lib"
        exampleLibRustBuildTarget.baseDirectory = "file://\$WORKSPACE/example-lib/"
        return exampleLibRustBuildTarget
    }

    private fun makeExample(): BuildTarget {
        val exampleRustBuildTargetDependencies = listOf(
            BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
            BuildTargetIdentifier("@crate_index__itertools-0.10.5//:itertools"),
        )
        val exampleRustBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example:example"),
            listOf("application"),
            listOf("rust"),
            exampleRustBuildTargetDependencies,
            BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = false }
        )
        exampleRustBuildTarget.displayName = "$targetPrefix//example:example"
        exampleRustBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        return exampleRustBuildTarget
    }

    private fun makeExampleFeature(): BuildTarget {
        val exampleFeatureRustBuildTargetDependencies = listOf(
            BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
            BuildTargetIdentifier("@crate_index__itertools-0.10.5//:itertools"),
            BuildTargetIdentifier("@crate_index__itoa-1.0.6//:itoa")
        )
        val exampleFeatureRustBuildTarget = BuildTarget(
            BuildTargetIdentifier("$targetPrefix//example:example_foo"),
            listOf("application"),
            listOf("rust"),
            exampleFeatureRustBuildTargetDependencies,
            BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = false }
        )
        exampleFeatureRustBuildTarget.displayName = "$targetPrefix//example:example_foo"
        exampleFeatureRustBuildTarget.baseDirectory = "file://\$WORKSPACE/example/"
        return exampleFeatureRustBuildTarget
    }

    private fun rustWorkspaceResults(): BazelBspTestScenarioStep {
        val expectedTargetIdentifiers = expectedTargetIdentifiers().filter {
            it.uri != "bsp-workspace-root"
        }
        val expectedResolvedTargets = expectedTargetIdentifiers.filter {
            it.uri != "@//example:example"
        }
        val expectedRustWorkspaceResult = RustWorkspaceResult(
            expectedPackages(),
            expectedRawDependencies(),
            expectedDependencies(),
            expectedResolvedTargets
        )
        val rustWorkspaceParams = RustWorkspaceParams(expectedTargetIdentifiers)

        return BazelBspTestScenarioStep(
            "rustWorkspace results"
        ) {
            testClient.testRustWorkspace(
                Duration.ofSeconds(30),
                rustWorkspaceParams,
                expectedRustWorkspaceResult
            )
        }
    }

    private fun expectedPackages(): List<RustPackage> {
        val exampleLibTargets = listOf(
            RustTarget(
                "example_lib",
                "file://\$WORKSPACE/example-lib/lib.rs",
                RustTargetKind.LIB,
                "2018",
                false
            ).also { it.crateTypes = listOf(RustCrateType.RLIB); it.requiredFeatures = setOf() }
        )
        val exampleLibPackage = RustPackage(
            "@//example-lib",
            "file://\$WORKSPACE/example-lib/",
            "@//example-lib",
            "0.0.0",
            "WORKSPACE",
            "2018",
            exampleLibTargets,
            exampleLibTargets,
            mapOf(),
            setOf()
        ).also { it.env = expectedEnv("@//example-lib", "example-lib") }

        val exampleTargets = listOf(
            RustTarget(
                "example",
                "file://\$WORKSPACE/example/main.rs",
                RustTargetKind.BIN,
                "2018",
                false
            ).also { it.crateTypes = listOf(); it.requiredFeatures = setOf() },
            RustTarget(
                "example_foo",
                "file://\$WORKSPACE/example/main.rs",
                RustTargetKind.BIN,
                "2018",
                false
            ).also { it.crateTypes = listOf(); it.requiredFeatures = setOf("foo") }
        )
        val examplePackage = RustPackage(
            "@//example",
            "file://\$WORKSPACE/example/",
            "@//example",
            "0.0.0",
            "WORKSPACE",
            "2018",
            listOf(exampleTargets[1]),
            exampleTargets,
            mapOf("foo" to setOf()),
            setOf("foo")
        ).also { it.env = expectedEnv("@//example", "example") }

        return listOf(exampleLibPackage, examplePackage)
    }

    private fun expectedEnv(packageName: String, name: String): Map<String, String> =
        mapOf(
            "CARGO" to "cargo",
            "CARGO_CRATE_NAME" to "$packageName",
            "CARGO_MANIFEST_DIR" to "file://\$WORKSPACE/$name///$name",
            "CARGO_PKG_AUTHORS" to "",
            "CARGO_PKG_DESCRIPTION" to "",
            "CARGO_PKG_LICENSE" to "",
            "CARGO_PKG_LICENSE_FILE" to "",
            "CARGO_PKG_NAME" to "$packageName",
            "CARGO_PKG_REPOSITORY" to "",
            "CARGO_PKG_VERSION" to "0.0.0",
            "CARGO_PKG_VERSION_MAJOR" to "0",
            "CARGO_PKG_VERSION_MINOR" to "0",
            "CARGO_PKG_VERSION_PATCH" to "0",
            "CARGO_PKG_VERSION_PRE" to ""
        )

    private fun expectedDependencies(): Map<String, List<RustDependency>> {
        val exampleDependencies = listOf(
            RustDependency("@//example-lib").also { it.name = "example_lib" },
            RustDependency("@crate_index__itertools-0.10.5//").also { it.name = "itertools" },
            RustDependency("@crate_index__itoa-1.0.6//").also { it.name = "itoa" }
        ).map { dep -> dep.also { it.depKinds = listOf(RustDepKindInfo("normal")) } }

        val exampleLibDependencies = listOf(
            RustDependency("@crate_index__serde-1.0.160//").also { it.name = "serde" },
            RustDependency("@crate_index__serde_json-1.0.96//").also { it.name = "serde_json" }
        ).map { dep -> dep.also { it.depKinds = listOf(RustDepKindInfo("normal")) } }

        return mapOf("@//example" to exampleDependencies, "@//example-lib" to exampleLibDependencies)
    }

    private fun expectedRawDependencies(): Map<String, List<RustRawDependency>> {
        val exampleRawDependencies = listOf(
            RustRawDependency("@//example-lib:example_lib", false, true, setOf()),
            RustRawDependency("@crate_index__itertools-0.10.5//:itertools", false, true, setOf()),
        )

        val exampleLibRawDependencies = listOf(
            RustRawDependency("@crate_index__serde-1.0.160//:serde", false, true, setOf()),
            RustRawDependency("@crate_index__serde_json-1.0.96//:serde_json", false, true, setOf())
        )

        return mapOf("@//example" to exampleRawDependencies, "@//example-lib" to exampleLibRawDependencies)
    }
}
