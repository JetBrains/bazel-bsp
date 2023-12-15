package org.jetbrains.bsp.bazel.server.sync.languages.rust

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.bazelrunner.orLatestSupported
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class RustPackageResolverTest {

    private val outputBase = "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b"
    private val execRoot = "$outputBase/execroot/bazel_bsp"
    private lateinit var bazelPathsResolver: BazelPathsResolver
    private lateinit var resolver: RustPackageResolver

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

        bazelPathsResolver = BazelPathsResolver(bazelInfo)
        resolver = RustPackageResolver(bazelPathsResolver)
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
    fun `should return proper package for a single local target`() {
        // given
        val features = listOf(
            "a_feature",
            "another_feature"
        )
        val (rustModule, module) = getModuleWithoutDependencies(features)

        // when
        val packages = resolver.rustPackages(listOf(module))

        // then
        packages.size shouldBe 1

        val pkg = packages[0]
        pkg.id shouldBe module.label.value.split(":")[0]
        pkg.version shouldBe rustModule.version
        pkg.edition shouldBe rustModule.edition
        pkg.features.keys shouldContainExactlyInAnyOrder features
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
    }
}
