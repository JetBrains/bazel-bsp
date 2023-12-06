package org.jetbrains.bsp.bazel.server.sync.languages.rust

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.bazelrunner.orLatestSupported
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class RustLanguagePluginTest {

  private val outputBase = "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b"
  private val execRoot = "$outputBase/execroot/bazel_bsp"
  private lateinit var bazelPathsResolver: BazelPathsResolver
  private lateinit var languagePlugin: RustLanguagePlugin

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
    languagePlugin = RustLanguagePlugin(bazelPathsResolver)
  }


  @Test
  fun `should return empty workspace for empty requested targets list`() {
    // given
    val (modules, _) = getSampleModules()

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
    val (modules, modulesMap) = getSampleModules()
    val requestedTargets = listOf(modulesMap["F"]!!)

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.resolvedTargets[0].name } shouldContainExactlyInAnyOrder
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
    val (modules, modulesMap) = getSampleModules()
    val requestedTargets = listOf(modulesMap["E"]!!)

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.resolvedTargets[0].name } shouldContainExactlyInAnyOrder
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
    val (modules, modulesMap) = getSampleModules()
    val requestedTargets = listOf(modulesMap["A"]!!)

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.resolvedTargets[0].name } shouldContainExactlyInAnyOrder
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
    val (modules, modulesMap) = getSampleModules()
    val requestedTargets = listOf(modulesMap["B"]!!, modulesMap["E"]!!)

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.resolvedTargets[0].name } shouldContainExactlyInAnyOrder
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
    val (modules, modulesMap) = getSampleModules()
    val requestedTargets = listOf(modulesMap["A"]!!, modulesMap["B"]!!)

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.resolvedTargets[0].name } shouldContainExactlyInAnyOrder
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
    val (modules, modulesMap) = getSampleModules()
    val requestedTargets = listOf(
        modulesMap["A"]!!, modulesMap["E"]!!, modulesMap["G"]!!, modulesMap["H"]!!
    )

    // when
    val workspace = languagePlugin.toRustWorkspaceResult(requestedTargets, modules)

    // then
    workspace.packages.map { it.resolvedTargets[0].name } shouldContainExactlyInAnyOrder
        listOf("A", "D", "E", "F", "G", "H")
  }
}
