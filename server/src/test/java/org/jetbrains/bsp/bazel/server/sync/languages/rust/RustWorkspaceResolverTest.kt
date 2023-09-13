package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.RustPackage
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths


class RustWorkspaceResolverTest {

  private val outputBase = "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b"
  private val execRoot = "$outputBase/execroot/bazel_bsp"
  private lateinit var bazelPathsResolver: BazelPathsResolver
  private lateinit var resolver: RustWorkspaceResolver

  @BeforeEach
  fun beforeEach() {
    // given
    val bazelInfo = BasicBazelInfo(
      execRoot = execRoot,
      outputBase = Paths.get(outputBase),
      workspaceRoot = Paths.get("/Users/user/workspace/bazel-bsp"),
      release = BazelRelease.fromReleaseString("release 6.0.0")
    )

    bazelPathsResolver = BazelPathsResolver(bazelInfo)
    resolver = RustWorkspaceResolver(bazelPathsResolver)
  }

  @Test
  fun `should return empty package list for empty module list`() {
    // given
    val modules = emptyList<Module>()

    // when
    val packages = resolver.rustPackages(modules)

    // then
    packages shouldNotBe null
    packages shouldBe emptyList()
  }

  @Test
  fun `should return proper package and empty dependencies for a single local target`() {
    // given
    val features = listOf(
        "a_feature",
        "another_feature"
    )
    val rustModule = createRustModule(
        crateId = "sample_target/src/lib.rs",
        crateRoot = "file:///path/to/sample_target/src/lib.rs",
        crateFeatures = features,
        dependencies = emptyList()
    )
    val module = createModule(
        label = "@//sample_target:sample_target",
        directDependencies = emptyList(),
        sources = setOf<URI>(
            URI.create("file:///path/to/sample_target/src/lib.rs")
        ),
        baseDirectory = URI.create("file:///path/to/sample_target/"),
        rustModule = rustModule
    )
    val modules = listOf(module)

    // when
    val packages = resolver.rustPackages(modules)
    val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

    // then
    packages.size shouldBe 1

    val pkg = packages[0]
    pkg.id shouldBe module.label.value.split(":")[0]
    pkg.version shouldBe rustModule.version
    pkg.edition shouldBe rustModule.edition
    pkg.features.map { it.name } shouldContainExactlyInAnyOrder features
    pkg.enabledFeatures shouldContainExactlyInAnyOrder features

    pkg.resolvedTargets shouldNotBe null
    pkg.resolvedTargets shouldBe pkg.allTargets
    pkg.resolvedTargets.size shouldBe 1

    val target = pkg.resolvedTargets[0]
    target.name shouldBe module.label.value.split(":")[1]
    target.crateRootUrl shouldBe rustModule.crateRoot
    target.kind.toString().lowercase() shouldBe rustModule.kind
    target.edition shouldBe rustModule.edition
    target.requiredFeatures shouldContainExactlyInAnyOrder features

    dependencies shouldNotBe null
    dependencies shouldBe emptyMap()

    rawDependencies shouldNotBe null
    rawDependencies shouldBe emptyMap()
  }

  @Test
  fun `should return empty dependency list for empty package list`() {
    // given
    val modules = emptyList<Module>()
    val packages = emptyList<RustPackage>()

    // when
    val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

    // then
    dependencies shouldNotBe null
    dependencies shouldBe emptyMap()

    rawDependencies shouldNotBe null
    rawDependencies shouldBe emptyMap()
  }

  @Test
  fun `should return proper dependency list for a target with one dependency`() {
    // given
    //   B
    //   |
    //   A

    val rustModuleA = createRustModule(
        crateId = "dirA/src/lib.rs",
        crateRoot = "file:///path/to/targetA/src/lib.rs",
        dependencies = emptyList()
    )
    val moduleA = createModule(
        label = "@//dirA:targetA",
        directDependencies = emptyList(),
        sources = setOf(URI.create("file:///path/to/dirA/src/lib.rs")),
        baseDirectory = URI.create("file:///path/to/dirA/"),
        rustModule = rustModuleA
    )

    val rustModuleB = createRustModule(
        crateId = "dirB/src/lib.rs",
        crateRoot = "file:///path/to/dirB/src/lib.rs",
        dependencies = listOf(
            RustDependency(
                crateId = rustModuleA.crateId,
                rename = rustModuleA.crateId
            )
        )
    )
    val moduleB = createModule(
        label = "@//dirB:targetB",
        directDependencies = listOf(moduleA.label),
        sources = setOf(URI.create("file:///path/to/dirB/src/lib.rs")),
        baseDirectory = URI.create("file:///path/to/dirB/"),
        rustModule = rustModuleB
    )

    val modules = listOf(moduleA, moduleB)
    val packages = resolver.rustPackages(modules)

    // when
    val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

    // then
    dependencies.size shouldBe 1

    val dependency = dependencies.entries.first()
    dependency.key shouldBe moduleB.label.value.split(":")[0]
    dependency.value[0].pkg shouldBe moduleA.label.value.split(":")[0]
    dependency.value[0].name shouldBe moduleA.label.value.split(":")[1]

    rawDependencies.size shouldBe 1

    val rawDependency = rawDependencies.entries.first()
    rawDependency.value[0].name shouldBe moduleA.label.value
    rawDependency.key shouldBe moduleB.label.value.split(":")[0]
  }

  @Test
  fun `should return proper dependency graph for multiple targets with multiple dependencies`() {
    // given
    // B    A
    // | \ / \
    // C  D   E
    // \ /  \ | \
    //  F     G  H

    val (modules, _) = getSampleModules()
    val packages = resolver.rustPackages(modules)

    // when
    val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

    // then
    dependencies.keys.toSet() shouldContainExactlyInAnyOrder
        listOf("A", "B", "C", "D", "E").map { "@//pkg$it" }

    val dependenciesNames = dependencies
        .mapValues { (_, deps) -> deps.map { it.name } }
    val trueDependenciesNames = mapOf(
        "A" to listOf("D", "E"),
        "B" to listOf("C", "D"),
        "C" to listOf("F"),
        "D" to listOf("F", "G"),
        "E" to listOf("G", "H")
    ).mapKeys { (name, _) -> "@//pkg$name" }
    dependenciesNames shouldContainExactly trueDependenciesNames

    rawDependencies.keys.toSet() shouldContainExactlyInAnyOrder
        listOf("A", "B", "C", "D", "E").map { "@//pkg$it" }

    val rawDependenciesNames = rawDependencies
        .mapValues { (_, deps) -> deps.map { it.name } }
    val trueRawDependenciesNames = trueDependenciesNames
        .mapValues { (_, names) -> names.map { "@//pkg$it:$it" } }
    rawDependenciesNames shouldContainExactly trueRawDependenciesNames
  }
}
