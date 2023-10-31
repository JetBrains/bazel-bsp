package org.jetbrains.bsp.bazel.server.sync.languages.java

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.bazelrunner.orLatestSupported
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths

class IdeClasspathResolverTest {

  private val outputBase = "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b"
  private val execRoot = "$outputBase/execroot/bazel_bsp"
  private lateinit var bazelPathsResolver: BazelPathsResolver

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
  }

  @Test
  fun `should return runtime classpath for if exists and compile classpath if runtime doesnt exist and filter current target jar out`() {
    // given
    val compileClasspath = listOf(
      URI.create("file:///path/to/normal/maven/library-ijar.jar"),
      URI.create("file://$execRoot/path/to/target/targetName.abi.jar"),
      URI.create("file://$execRoot/path/to/another/target/anotherTargetName.abi.jar"),
      URI.create("file:///path/to/another/normal/maven/library-ijar.jar"),
    )

    val runtimeClasspath = listOf(
      URI.create("file:///path/to/normal/maven/library.jar"),
      URI.create("file://$execRoot/path/to/target/targetName.jar"),
      URI.create("file://$execRoot/path/to/another/target/anotherTargetName.jar"),
    )

    val resolver = IdeClasspathResolver(
      label = Label("@//path/to/target:targetName"),
      bazelPathsResolver = bazelPathsResolver,
      compileClasspath = compileClasspath.asSequence(),
      runtimeClasspath = runtimeClasspath.asSequence(),
    )

    // when
    val resolvedClasspath = resolver.resolve().toList()

    // then
    resolvedClasspath shouldContainExactlyInAnyOrder  listOf(
      URI.create("file:///path/to/normal/maven/library.jar"),
      URI.create("file://$execRoot/path/to/another/target/anotherTargetName.jar"),
      URI.create("file:///path/to/another/normal/maven/library-ijar.jar"),
    )
  }
}
