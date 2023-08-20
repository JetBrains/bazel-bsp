package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextListEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection

data class BuildFlagsSpec(
    override val values: List<String>,
) : ExecutionContextListEntity<String>()

private val defaultBuildFlagsSpec = BuildFlagsSpec(
    values = emptyList()
)

internal object BuildFlagsSpecMapper : ProjectViewToExecutionContextEntityMapper<BuildFlagsSpec> {

    override fun map(projectView: ProjectView): Result<BuildFlagsSpec> =
        when (projectView.buildFlags) {
            null -> Result.success(defaultBuildFlagsSpec)
            else -> Result.success(mapNotEmptySection(projectView.buildFlags!!))
        }

    private fun mapNotEmptySection(targetsSection: ProjectViewBuildFlagsSection): BuildFlagsSpec =
        BuildFlagsSpec(
            values = targetsSection.values
        )

    override fun default(): Result<BuildFlagsSpec> = Result.success(defaultBuildFlagsSpec)
}
