package org.jetbrains.bsp.bazel.server.sync.languages.rust

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths


class RustLanguagePluginTest {

  private val execRoot = "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b/execroot/bazel_bsp"
  private lateinit var bazelPathsResolver: BazelPathsResolver
  private lateinit var languagePlugin: RustLanguagePlugin

  @BeforeEach
  fun beforeEach() {
    // given
    val bazelInfo = BasicBazelInfo(
      execRoot = execRoot,
      workspaceRoot = Paths.get("/Users/user/workspace/bazel-bsp"),
      release = BazelRelease.fromReleaseString("release 6.0.0")
    )

    bazelPathsResolver = BazelPathsResolver(bazelInfo)
    languagePlugin = RustLanguagePlugin(bazelPathsResolver)
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

  private fun getSampleModules(): List<Module> {
    // B    A
    // | \ / \
    // C  D   E
    // \ /  \ | \
    //  F     G  H

    val pathPrefix = "file:///path/to/targets"

    val moduleA = createTarget(
        packageName = "pkgA",
        targetName = "A",
        directDependencies = listOf("pkgD:D", "pkgE:E"),
        sources = setOf("$pathPrefix/dirA/src/lib.rs"),
        crateRoot = "$pathPrefix/dirA/src/lib.rs",
        baseDirectory = "$pathPrefix/dirA/"
    )
    val moduleB = createTarget(
        packageName = "pkgB",
        targetName = "B",
        directDependencies = listOf("pkgC:C", "pkgD:D"),
        sources = setOf("$pathPrefix/dirB/src/lib.rs"),
        crateRoot = "$pathPrefix/dirB/src/lib.rs",
        baseDirectory = "$pathPrefix/dirB/"
    )
    val moduleC = createTarget(
        packageName = "pkgC",
        targetName = "C",
        directDependencies = listOf("pkgF:F"),
        sources = setOf("$pathPrefix/dirC/src/lib.rs"),
        crateRoot = "$pathPrefix/dirC/src/lib.rs",
        baseDirectory = "$pathPrefix/dirC/"
    )
    val moduleD = createTarget(
        packageName = "pkgD",
        targetName = "D",
        directDependencies = listOf("pkgF:F", "pkgG:G"),
        sources = setOf("$pathPrefix/dirD/src/lib.rs"),
        crateRoot = "$pathPrefix/dirD/src/lib.rs",
        baseDirectory = "$pathPrefix/dirD/"
    )
    val moduleE = createTarget(
        packageName = "pkgE",
        targetName = "E",
        directDependencies = listOf("pkgG:G", "pkgH:H"),
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
    val moduleH = createTarget(
        packageName = "pkgH",
        targetName = "H",
        directDependencies = emptyList(),
        sources = setOf("$pathPrefix/dirH/src/lib.rs"),
        crateRoot = "$pathPrefix/dirH/src/lib.rs",
        baseDirectory = "$pathPrefix/dirH/"
    )

    return listOf(moduleA, moduleB, moduleC, moduleD, moduleE, moduleF, moduleG, moduleH)
  }

  @Test
  fun `should return empty workspace for empty requested targets list`() {
    // given
    val modules = getSampleModules()

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(emptyList(), modules)

    // then
    workspace.packages shouldBe emptyList()
  }

  @Test
  fun `should return workspace with a single target for a single requested target without dependencies`() {
    // given
    //    B    A
    //    | \ / \
    //    C  D   E
    //    \ /  \ | \
    // --> F     G  H
    val modules = getSampleModules()
    val requestedTargets = modules.filter { it.label.value.split(":")[1] == "F" }

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.targets[0].name } shouldContainExactlyInAnyOrder
        listOf("F")
  }

  @Test
  fun `should return proper workspace for a single requested target with single dependency layer`() {
    // given
    //    B    A
    //    | \ / \
    //    C  D   E <--
    //    \ /  \ | \
    //     F     G  H
    val modules = getSampleModules()
    val requestedTargets = modules.filter { it.label.value.split(":")[1] == "E" }

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.targets[0].name } shouldContainExactlyInAnyOrder
        listOf("E", "G", "H")
  }

  @Test
  fun `should return proper workspace for a single requested target with multiple dependency layers`() {
    // given
    //    B    A  <--
    //    | \ / \
    //    C  D   E
    //    \ /  \ | \
    //     F     G  H
    val modules = getSampleModules()
    val requestedTargets = modules.filter { it.label.value.split(":")[1] == "A" }

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.targets[0].name } shouldContainExactlyInAnyOrder
        listOf("A", "D", "E", "F", "G", "H")
  }

  @Test
  fun `should return proper workspace for multiple requested targets`() {
    // given
    //--> B    A
    //    | \ / \
    //    C  D   E <--
    //    \ /  \ | \
    //     F     G  H
    val modules = getSampleModules()
    val requestedTargets = modules.filter { it.label.value.split(":")[1] in listOf("B", "E") }

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.targets[0].name } shouldContainExactlyInAnyOrder
        listOf("B", "C", "D", "F", "E", "G", "H")
  }

  @Test
  fun `should return workspace with all modules for requested targets that depend on every module`() {
    // given
    //--> B    A <--
    //    | \ / \
    //    C  D   E
    //    \ /  \ | \
    //     F     G  H
    val modules = getSampleModules()
    val requestedTargets = modules.filter { it.label.value.split(":")[1] in listOf("A", "B") }

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.targets[0].name } shouldContainExactlyInAnyOrder
        listOf("A", "B", "C", "D", "E", "F", "G", "H")
  }

  @Test
  fun `should return workspace with non repeating modules`() {
    // given
    //    B    A <--
    //    | \ / \
    //    C  D   E <--
    //    \ /  \ | \
    //     F --> G  H <--
    val modules = getSampleModules()
    val requestedTargets = modules.filter { it.label.value.split(":")[1] in listOf("A", "E", "G", "H") }

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.targets[0].name } shouldContainExactlyInAnyOrder
        listOf("A", "D", "E", "F", "G", "H")
  }
}
