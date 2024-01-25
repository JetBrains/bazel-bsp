package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class DefaultWorkspaceContextProviderTest {
  private lateinit var workspaceRoot: Path
  private lateinit var projectViewFile: Path

  @BeforeEach
  fun beforeEach() {
    workspaceRoot = createTempDirectory("workspaceRoot")
    projectViewFile = workspaceRoot.resolve("projectview.bazelproject")
  }

  @Test
  fun `should parse project view file and return workspace context`() {
    // given
    projectViewFile.createFile()
    projectViewFile.writeText("""
      |targets:
      |  //...
    """.trimMargin())

    val provider = DefaultWorkspaceContextProvider(workspaceRoot, projectViewFile)

    // when
    val workspaceContext = provider.currentWorkspaceContext()

    // then
    workspaceContext.targets shouldBe TargetsSpec(listOf(BuildTargetIdentifier("//...")), emptyList())
  }

  @Test
  fun `should generate an empty project view file if the file doesn't exist`() {
    // given
    projectViewFile.deleteIfExists()
    val provider = DefaultWorkspaceContextProvider(workspaceRoot, projectViewFile)

    // when
    val workspaceContext = provider.currentWorkspaceContext()

    // then
    workspaceContext.targets shouldBe TargetsSpec(emptyList(), emptyList())
    projectViewFile.exists() shouldBe true
    projectViewFile.readText().trim() shouldBe ""
  }
}
