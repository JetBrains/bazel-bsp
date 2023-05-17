package org.jetbrains.bsp.bazel.server.sync.languages.rust

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
