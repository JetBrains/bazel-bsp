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
      baseDirectory: URI,
      sources: Set<URI>,
      rustModule: RustModule
  ): Module =
      Module(
          label = Label(label),
          isSynthetic = false,
          directDependencies = directDependencies,
          languages = setOf(Language.RUST),
          tags = setOf(Tag.APPLICATION),
          baseDirectory = baseDirectory,
          sourceSet = SourceSet(
              sources = sources,
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
          name = crateId.split("/")[0],
          kind = "bin",
          edition = "2018",
          crateFeatures = crateFeatures,
          dependencies = dependencies,
          crateRoot = crateRoot,
          version = "1.2.3",
          procMacroArtifacts = procMacroArtifacts,
          procMacroSrv = "/path/to/procMacroSrv",
          rustcSysroot = "/path/to/rustcSysroot",
          rustcSrcSysroot = "/path/to/rustcSrcSysroot",
          cargoBinPath = "/path/to/cargoBinPath",
          rustcVersion = "rustcVersion",
          rustcHost = "x86_64-unknown-linux-gnu",
      )

  private fun createTarget(
      packageName: String,
      targetName: String,
      directDependencies: List<String>,
      sources: Set<String>,
      crateRoot: String,
      baseDirectory: String
  ): Module =
      createModule(
          label = "$packageName:$targetName",
          directDependencies = directDependencies.map { Label(it) },
          rustModule = createRustModule(
              crateId = targetName,
              crateRoot = crateRoot,
              dependencies = directDependencies.map { RustDependency(it, it) },
          ),
          sources = sources.map { URI.create(it) }.toSet(),
          baseDirectory = URI.create(baseDirectory)
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
//    packages shouldBe emptyList()
    packages.size shouldBe 1

    val pkg = packages[0]
    pkg.id shouldBe module.label.value.split(":")[0]
    pkg.version shouldBe rustModule.version
    pkg.edition shouldBe rustModule.edition
    pkg.features.map { it.name } shouldContainExactlyInAnyOrder features
    pkg.enabledFeatures shouldContainExactlyInAnyOrder features

    pkg.targets shouldNotBe null
    pkg.targets shouldBe pkg.allTargets
    pkg.targets.size shouldBe 1

    val target = pkg.targets[0]
    target.name shouldBe module.label.value.split(":")[1]
    target.crateRootUrl shouldBe rustModule.crateRoot
    target.kind shouldBe rustModule.kind
    target.edition shouldBe rustModule.edition
    target.requiredFeatures shouldContainExactlyInAnyOrder features

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

    val dependency = dependencies[0]
    dependency.source shouldBe moduleB.label.value.split(":")[0]
    dependency.target shouldBe moduleA.label.value.split(":")[0]
    dependency.name shouldBe moduleA.label.value.split(":")[1]

    rawDependencies.size shouldBe 1
    // TODO are rawDependencies needed?
  }

  @Test
  fun `should return proper dependency graph for multiple targets with multiple dependencies`() {
    // given
    // B    A
    // | \ / \
    // C  D   E
    // \ /  \ |
    //  F     G

    val pathPrefix = "file:///path/to/targets"

    val moduleA = createTarget(
        packageName = "pkgA",
        targetName = "A",
        directDependencies = listOf("D", "E"),
        sources = setOf("$pathPrefix/dirA/src/lib.rs"),
        crateRoot = "$pathPrefix/dirA/src/lib.rs",
        baseDirectory = "$pathPrefix/dirA/"
    )
    val moduleB = createTarget(
        packageName = "pkgB",
        targetName = "B",
        directDependencies = listOf("C", "D"),
        sources = setOf("$pathPrefix/dirB/src/lib.rs"),
        crateRoot = "$pathPrefix/dirB/src/lib.rs",
        baseDirectory = "$pathPrefix/dirB/"
    )
    val moduleC = createTarget(
        packageName = "pkgC",
        targetName = "C",
        directDependencies = listOf("F"),
        sources = setOf("$pathPrefix/dirC/src/lib.rs"),
        crateRoot = "$pathPrefix/dirC/src/lib.rs",
        baseDirectory = "$pathPrefix/dirC/"
    )
    val moduleD = createTarget(
        packageName = "pkgD",
        targetName = "D",
        directDependencies = listOf("F", "G"),
        sources = setOf("$pathPrefix/dirD/src/lib.rs"),
        crateRoot = "$pathPrefix/dirD/src/lib.rs",
        baseDirectory = "$pathPrefix/dirD/"
    )
    val moduleE = createTarget(
        packageName = "pkgE",
        targetName = "E",
        directDependencies = listOf("G"),
        sources = setOf("$pathPrefix/dirE/src/lib.rs"),
        crateRoot = "$pathPrefix/dirE/src/lib.rs",
        baseDirectory = "$pathPrefix/dirE/"
    )
    val moduleF = createTarget(
        packageName = "pkgF",
        targetName = "F",
        directDependencies = emptyList(),
        sources = setOf("$pathPrefix/dirF/src/lib.rs"),
        crateRoot = "$pathPrefix/dirF/src/lib.rs",
        baseDirectory = "$pathPrefix/dirF/"
    )
    val moduleG = createTarget(
        packageName = "pkgG",
        targetName = "G",
        directDependencies = emptyList(),
        sources = setOf("$pathPrefix/dirG/src/lib.rs"),
        crateRoot = "$pathPrefix/dirG/src/lib.rs",
        baseDirectory = "$pathPrefix/dirG/"
    )

    val modules = listOf(moduleA, moduleB, moduleC, moduleD, moduleE, moduleF, moduleG)
    val packages = resolver.rustPackages(modules)

    // when
    val (dependencies, _) = resolver.rustDependencies(packages, modules)

    // then
    dependencies.map { it.source }.toSet() shouldContainExactlyInAnyOrder listOf(
        moduleA, moduleB, moduleC, moduleD, moduleE
    ).map { it.label.value.split(":")[0] }

    val dependenciesNames = dependencies
        .groupBy { it.source }
        .mapValues { (_, deps) -> deps.map { it.target } }
    val trueDependenciesNames = mapOf(
        moduleA to listOf("D", "E"),
        moduleB to listOf("C", "D"),
        moduleC to listOf("F"),
        moduleD to listOf("F", "G"),
        moduleE to listOf("G")
    ).mapKeys { (module, _) -> module.label.value.split(":")[0] }

    dependenciesNames shouldContainExactly trueDependenciesNames
  }
}
