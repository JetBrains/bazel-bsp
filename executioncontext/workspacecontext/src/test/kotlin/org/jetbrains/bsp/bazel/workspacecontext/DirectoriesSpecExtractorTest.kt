package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class DirectoriesSpecExtractorTest {

  @Test
  fun `should return workspace root if directories section is null`() {
    // given
    val workspaceRoot = Path("path/to/workspace")
    val projectView = ProjectView.Builder(directories = null).build()

    // when
    val directoriesSpec = DirectoriesSpecExtractor(workspaceRoot).fromProjectView(projectView)

    // then
    val expectedDirectoriesSpec = DirectoriesSpec(
      values = listOf(workspaceRoot),
      excludedValues = emptyList()
    )
    directoriesSpec shouldBe expectedDirectoriesSpec
  }

  @Test
  fun `should return workspace root if directories section contains only dot wildcard`() {
    // given
    val workspaceRoot = Path("path/to/workspace")
    val projectView = ProjectView.Builder(
      directories = ProjectViewDirectoriesSection(
        values = listOf(Path(".")),
        excludedValues = emptyList()
      )
    ).build()

    // when
    val directoriesSpec = DirectoriesSpecExtractor(workspaceRoot).fromProjectView(projectView)

    // then
    val expectedDirectoriesSpec = DirectoriesSpec(
      values = listOf(workspaceRoot),
      excludedValues = emptyList()
    )
    directoriesSpec shouldBe expectedDirectoriesSpec
  }

  @Test
  fun `should return resolved paths if directories section is not null`() {
    // given
    val workspaceRoot = Path("path/to/workspace")
    val projectView = ProjectView.Builder(
      directories = ProjectViewDirectoriesSection(
        values = listOf(Path("path/to/included1"), Path("path/to/included2")),
        excludedValues = listOf(Path("path/to/excluded")),
      )
    ).build()

    // when
    val directoriesSpec = DirectoriesSpecExtractor(workspaceRoot).fromProjectView(projectView)

    // then
    val expectedDirectoriesSpec = DirectoriesSpec(
      values = listOf(
        workspaceRoot.resolve("path/to/included1"),
        workspaceRoot.resolve("path/to/included2")
      ),
      excludedValues = listOf(workspaceRoot.resolve("path/to/excluded"))
    )
    directoriesSpec shouldBe expectedDirectoriesSpec
  }
}
