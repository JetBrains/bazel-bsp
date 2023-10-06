package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextExcludableListEntity
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import java.nio.file.Path

data class DirectoriesSpec(
  override val values: List<Path>,
  override val excludedValues: List<Path>,
) : ExecutionContextExcludableListEntity<Path>()

internal class DirectoriesSpecExtractor(private val workspaceRoot: Path)
  : ExecutionContextEntityExtractor<DirectoriesSpec> {

  override fun fromProjectView(projectView: ProjectView): DirectoriesSpec =
    projectView.directories?.toDirectoriesSpec()
      ?: DirectoriesSpec(
        values = listOf(workspaceRoot),
        excludedValues = emptyList(),
      )

  private fun ProjectViewDirectoriesSection.toDirectoriesSpec(): DirectoriesSpec =
    DirectoriesSpec(
      values = values.map { it.resolveAndNormalize() },
      excludedValues = excludedValues.map { it.resolveAndNormalize() },
    )

  private fun Path.resolveAndNormalize() =
    workspaceRoot.resolve(this).normalize()
}
