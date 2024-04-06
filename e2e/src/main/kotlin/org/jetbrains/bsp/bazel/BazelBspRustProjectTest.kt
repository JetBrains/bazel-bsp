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
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object BazelBspRustProjectTest : BazelBspTestBaseScenario() {
  private val testClient = createTestkitClient()

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
        1.minutes,
        workspaceBuildTargetsResult
      )
    }
  }

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult =
    WorkspaceBuildTargetsResult(
      listOf(
        makeExampleLib(),
        makeExampleFeature(),
        makeExample()
      )
    )

  private fun makeExampleLib(): BuildTarget {
    val exampleLibDependencies = listOf(
      BuildTargetIdentifier("@crate_index__instant-0.1.12//:instant")
    )
    return makeBuildTarget("example-lib", "example_lib", "library", exampleLibDependencies, false)
  }

  private fun makeExample(): BuildTarget {
    val exampleDependencies = listOf(
      BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
      BuildTargetIdentifier("@crate_index__itertools-0.10.5//:itertools"),
    )
    return makeBuildTarget("example", "example", "application", exampleDependencies, true)
  }

  private fun makeExampleFeature(): BuildTarget {
    val exampleFeatureDependencies = listOf(
      BuildTargetIdentifier("$targetPrefix//example-lib:example_lib"),
      BuildTargetIdentifier("@crate_index__itertools-0.10.5//:itertools"),
      BuildTargetIdentifier("@crate_index__itoa-1.0.6//:itoa")
    )
    return makeBuildTarget("example", "example_foo", "application", exampleFeatureDependencies, true)
  }

  private fun makeBuildTarget(
    packageName: String,
    name: String,
    type: String,
    dependencies: List<BuildTargetIdentifier>,
    canRun: Boolean
  ): BuildTarget {
    val buildtarget = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//$packageName:$name"),
      listOf(type),
      listOf("rust"),
      dependencies,
      BuildTargetCapabilities().also {
        it.canCompile = true; it.canTest = false; it.canRun = canRun; it.canDebug = false
      }
    )
    buildtarget.displayName = "$targetPrefix//$packageName:$name"
    buildtarget.baseDirectory = "file://\$WORKSPACE/$packageName/"
    return buildtarget
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
        30.seconds,
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

    val exampleTargets = listOf(
      expectedExampleTarget("example"),
      expectedExampleTarget("example_foo", setOf("foo"))
    )

    return listOf(
      expectedPackageFromDependency("cfg-if", "1.0.0"),
      expectedPackageFromDependency("either", "1.8.1", setOf("use_std")),
      expectedPackageFromDependency("instant", "0.1.12"),
      expectedPackageFromDependency("itertools", "0.10.5", setOf("default", "use_alloc", "use_std")),
      expectedPackageFromDependency("itoa", "1.0.6"),
      expectedPackageFromWorkspace("example-lib", exampleLibTargets, exampleLibTargets),
      expectedPackageFromWorkspace("example", listOf(exampleTargets[1]), exampleTargets, setOf("foo"))
    )
  }

  private fun expectedExampleTarget(name: String, features: Set<String> = setOf()): RustTarget =
    RustTarget(
      name,
      "file://\$WORKSPACE/example/main.rs",
      RustTargetKind.BIN,
      "2018",
      false
    ).also { it.crateTypes = listOf(); it.requiredFeatures = features }

  private fun expectedPackageFromDependency(
    name: String,
    version: String,
    features: Set<String> = setOf()
  ): RustPackage {
    val packageId = "crate_index__$name-$version"
    val packageName = "@$packageId//"
    val packageRootUrl = "file://\$BAZEL_OUTPUT_BASE_PATH/execroot/rust_test/external/$packageId/"

    val targets = expectedTargetFromDependency(name, packageRootUrl, features)

    return RustPackage(
      packageName,
      packageRootUrl,
      packageName,
      version,
      "DEPENDENCY",
      "2018",
      targets,
      targets,
      features.associateWith { setOf() },
      features
    ).also {
      it.env = expectedEnv(packageName, "$packageRootUrl$packageId//", version);
      it.source = "bazel+https://github.com/rust-lang/crates.io-index"
    }

  }

  private fun expectedTargetFromDependency(
    name: String,
    packageRootUrl: String,
    features: Set<String> = setOf()
  ): List<RustTarget> =
    listOf(
      RustTarget(
        name.replace("-", "_"),
        packageRootUrl + "src/lib.rs",
        RustTargetKind.LIB,
        "2018",
        false
      ).also { it.crateTypes = listOf(RustCrateType.RLIB); it.requiredFeatures = features }
    )

  private fun expectedEnv(packageName: String, manifestDir: String, version: String): Map<String, String> {
    val (major, minor, patch) = version.split(".")

    return mapOf(
      "CARGO" to "cargo",
      "CARGO_CRATE_NAME" to packageName,
      "CARGO_MANIFEST_DIR" to manifestDir,
      "CARGO_PKG_AUTHORS" to "",
      "CARGO_PKG_DESCRIPTION" to "",
      "CARGO_PKG_LICENSE" to "",
      "CARGO_PKG_LICENSE_FILE" to "",
      "CARGO_PKG_NAME" to packageName,
      "CARGO_PKG_REPOSITORY" to "",
      "CARGO_PKG_VERSION" to version,
      "CARGO_PKG_VERSION_MAJOR" to major,
      "CARGO_PKG_VERSION_MINOR" to minor,
      "CARGO_PKG_VERSION_PATCH" to patch,
      "CARGO_PKG_VERSION_PRE" to ""
    )
  }

  private fun expectedPackageFromWorkspace(
    name: String,
    resolvedTargets: List<RustTarget>,
    allTargets: List<RustTarget>,
    features: Set<String> = setOf()
  ): RustPackage {
    val packageName = "@//$name"

    return RustPackage(
      packageName,
      "file://\$WORKSPACE/$name/",
      packageName,
      "0.0.0",
      "WORKSPACE",
      "2018",
      resolvedTargets,
      allTargets,
      features.associateWith { setOf() },
      features
    ).also { it.env = expectedEnv(packageName, "file://\$WORKSPACE/$name///$name", "0.0.0") }
  }

  private fun expectedDependencies(): Map<String, List<RustDependency>> {
    val exampleDependencies = createDependency(
      listOf(
        Pair("@//example-lib", "example_lib"),
        Pair("@crate_index__itertools-0.10.5//", "itertools"),
        Pair("@crate_index__itoa-1.0.6//", "itoa")
      )
    )
    val exampleLibDependencies = createDependency(listOf(Pair("@crate_index__instant-0.1.12//", "instant")))
    val instantDependencies = createDependency(listOf(Pair("@crate_index__cfg-if-1.0.0//", "cfg_if")))
    val itertoolsDependencies = createDependency(listOf(Pair("@crate_index__either-1.8.1//", "either")))

    return mapOf(
      "@//example" to exampleDependencies,
      "@//example-lib" to exampleLibDependencies,
      "@crate_index__instant-0.1.12//" to instantDependencies,
      "@crate_index__itertools-0.10.5//" to itertoolsDependencies
    )
  }

  private fun createDependency(dependenciesNames: List<Pair<String, String>>): List<RustDependency> =
    dependenciesNames.map { dep ->
      RustDependency(dep.first).also {
        it.name = dep.second;
        it.depKinds = listOf(RustDepKindInfo("normal"))
      }
    }

  private fun expectedRawDependencies(): Map<String, List<RustRawDependency>> {
    val exampleDependencies =
      createRawDependency(listOf("@//example-lib:example_lib", "@crate_index__itertools-0.10.5//:itertools"))
    val exampleLibDependencies = createRawDependency(listOf("@crate_index__instant-0.1.12//:instant"))
    val instantDependencies = createRawDependency(listOf("@crate_index__cfg-if-1.0.0//:cfg_if"))
    val itertoolsDependencies = createRawDependency(listOf("@crate_index__either-1.8.1//:either"))

    return mapOf(
      "@//example" to exampleDependencies,
      "@//example-lib" to exampleLibDependencies,
      "@crate_index__instant-0.1.12//" to instantDependencies,
      "@crate_index__itertools-0.10.5//" to itertoolsDependencies
    )
  }

  private fun createRawDependency(rawDependenciesNames: List<String>): List<RustRawDependency> =
    rawDependenciesNames.map { dep -> RustRawDependency(dep, false, true, setOf()) }
}
