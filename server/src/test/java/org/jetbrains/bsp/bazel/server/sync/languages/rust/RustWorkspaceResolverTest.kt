package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.RustPackage
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

  private val execRoot = "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b/execroot/bazel_bsp"
  private lateinit var bazelPathsResolver: BazelPathsResolver
  private lateinit var resolver: RustWorkspaceResolver

  @BeforeEach
  fun beforeEach() {
    // given
    val bazelInfo = BasicBazelInfo(
      execRoot = execRoot,
      workspaceRoot = Paths.get("/Users/user/workspace/bazel-bsp"),
      release = BazelRelease.fromReleaseString("release 6.0.0")
    )

    bazelPathsResolver = BazelPathsResolver(bazelInfo)
    resolver = RustWorkspaceResolver(bazelPathsResolver)
  }

  private fun createModule(
      label: String,
      directDependencies: List<Label>,
      rustModule: RustModule
  ): Module =
      Module(
          label = Label(label),
          isSynthetic = false,
          directDependencies = directDependencies,
          languages = setOf(Language.RUST),
          tags = setOf(Tag.APPLICATION),
          baseDirectory = URI.create("/path/to/base/directory"),
          sourceSet = SourceSet(
              sources = setOf<URI>(),
              sourceRoots = setOf<URI>()
          ),
          resources = setOf<URI>(),
          outputs = setOf<URI>(),
          sourceDependencies = setOf<URI>(),
          languageData = rustModule,
          environmentVariables = mapOf<String, String>()
      )

  private fun createRustModule(
      crateId: String,
      crateRoot: String,
      location: RustCrateLocation = RustCrateLocation.EXEC_ROOT,
      crateFeatures: List<String> = emptyList(),
      dependencies: List<RustDependency>,
      procMacroArtifacts: List<String> = emptyList()
  ): RustModule =
      RustModule(
          crateId = crateId,
          location = location,
          fromWorkspace = true,
          name = "name", // TODO where is it used?
          kind = "kind",
          edition = "edition",
          crateFeatures = crateFeatures,
          dependencies = dependencies,
          crateRoot = crateRoot,
          version = "version",
          procMacroArtifacts = procMacroArtifacts,
          procMacroSrv = "/path/to/procMacroSrv",
          rustcSysroot = "/path/to/rustcSysroot",
          rustcSrcSysroot = "/path/to/rustcSrcSysroot",
          cargoBinPath = "/path/to/cargoBinPath",
          rustcVersion = "rustcVersion",
          rustcHost = "/path/to/rustcHost",
      )

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
        crateId = "SampleTarget",
        crateRoot = "/path/to/target",
        crateFeatures = features,
        dependencies = emptyList()
    )
    val module = createModule(
        label = "/path/to/target:SampleTarget",
        directDependencies = emptyList(),
        rustModule = rustModule
    )
    val modules = listOf(module)

    // when
    val packages = resolver.rustPackages(modules)
    val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

    // then
    packages.size shouldBe 1

    val pkg = packages[0]
    pkg.id shouldBe rustModule.crateRoot
    pkg.version shouldBe rustModule.version
    pkg.edition shouldBe rustModule.edition
    pkg.features.map { it.name } shouldBe features
    pkg.enabledFeatures shouldBe features

    pkg.targets shouldNotBe null
    pkg.targets shouldBe pkg.allTargets
    pkg.targets.size shouldBe 1

    val target = pkg.targets[0]
    target.name shouldBe rustModule.crateId
    target.crateRootUrl shouldBe rustModule.crateRoot
    target.kind shouldBe rustModule.kind
    target.edition shouldBe rustModule.edition
    target.requiredFeatures shouldBe features

    dependencies shouldNotBe null
    dependencies shouldBe emptyList()

    rawDependencies shouldNotBe null
    rawDependencies shouldBe emptyList()
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
    dependencies shouldBe emptyList()

    rawDependencies shouldNotBe null
    rawDependencies shouldBe emptyList()
  }

  @Test
  fun `should return proper dependency list for a target with one dependency`() {
    // given
    //   B
    //   |
    //   A
    val rustModuleA = createRustModule(
        crateId = "TargetA",
        crateRoot = "/path/to/target/a",
        dependencies = emptyList()
    )
    val moduleA = createModule(
        label = "/path/to/target/a:TargetA",
        directDependencies = emptyList(),
        rustModule = rustModuleA
    )

    val rustModuleB = createRustModule(
        crateId = "TargetB",
        crateRoot = "/path/to/target/b",
        dependencies = listOf(
            RustDependency(
                crateId = rustModuleA.crateId,
                rename = rustModuleA.crateId
            )
        )
    )
    val moduleB = createModule(
        label = "/path/to/target/b:TargetB",
        directDependencies = listOf(moduleA.label),
        rustModule = rustModuleB
    )

    val modules = listOf(moduleA, moduleB)
    val packages = resolver.rustPackages(modules)

    // when
    val (dependencies, rawDependencies) = resolver.rustDependencies(packages, modules)

    // then
    dependencies.size shouldBe 1

    val dependency = dependencies[0]
    dependency.source shouldBe rustModuleB.crateRoot
    dependency.target shouldBe rustModuleA.crateRoot
    dependency.name shouldBe rustModuleA.crateId

    rawDependencies.size shouldBe 1
    // TODO are rawDependencies needed?
  }
}
