package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.RustPackage
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.bazelrunner.orLatestSupported
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class RustDependencyResolverTest {

    private val outputBase = "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b"
    private val execRoot = "$outputBase/execroot/bazel_bsp"
    private lateinit var rustPackageResolver: RustPackageResolver
    private lateinit var resolver: RustDependencyResolver

    @BeforeEach
    fun beforeEach() {
        // given
        val bazelInfo = BasicBazelInfo(
            execRoot = execRoot,
            outputBase = Paths.get(outputBase),
            workspaceRoot = Paths.get("/Users/user/workspace/bazel-bsp"),
            release = BazelRelease.fromReleaseString("release 6.0.0").orLatestSupported(),
            false
        )

        rustPackageResolver = RustPackageResolver(BazelPathsResolver(bazelInfo))
        resolver = RustDependencyResolver(rustPackageResolver)
    }

    @Test
    fun `should return empty dependency list for empty package list`() {
        // given
        val modules = emptyList<Module>()
        val packages = emptyList<RustPackage>()

        // when
        val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

        // then
        dependencies shouldBe emptyMap()

        rawDependencies shouldBe emptyMap()
    }

    @Test
    fun `should return empty dependencies for a single local target`() {
        // given
        val (_, module) = getModuleWithoutDependencies()
        val modules = listOf(module)
        val packages = rustPackageResolver.rustPackages(modules)

        // when
        val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

        // then
        dependencies shouldBe emptyMap()

        rawDependencies shouldBe emptyMap()
    }

    @Test
    fun `should return proper dependency list for a target with one dependency`() {
        // given
        //   B
        //   |
        //   A

        val modules = getModulesWithDependency()
        val packages = rustPackageResolver.rustPackages(modules)

        // when
        val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

        // then
        dependencies.size shouldBe 1

        val dependency = dependencies.entries.first()
        dependency.key shouldBe modules[1].label.value.split(":")[0]
        dependency.value[0].pkg shouldBe modules[0].label.value.split(":")[0]
        dependency.value[0].name shouldBe modules[0].label.value.split(":")[1]

        rawDependencies.size shouldBe 1

        val rawDependency = rawDependencies.entries.first()
        rawDependency.value[0].name shouldBe modules[0].label.value
        rawDependency.key shouldBe modules[1].label.value.split(":")[0]
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
        val packages = rustPackageResolver.rustPackages(modules)

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