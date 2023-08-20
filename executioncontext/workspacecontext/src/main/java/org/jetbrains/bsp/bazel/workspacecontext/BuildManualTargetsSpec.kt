package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection

data class BuildManualTargetsSpec(override val value: Boolean) : ExecutionContextSingletonEntity<Boolean>()

private val defaultBuildManualTargetsSpec = BuildManualTargetsSpec(
    value = false
)

internal object BuildManualTargetsSpecMapper : ProjectViewToExecutionContextEntityMapper<BuildManualTargetsSpec> {

    override fun map(projectView: ProjectView): Result<BuildManualTargetsSpec> =
        if (projectView.buildManualTargets == null) default()
        else Result.success(map(projectView.buildManualTargets!!))

    private fun map(buildManualTargetsSection: ProjectViewBuildManualTargetsSection): BuildManualTargetsSpec =
            BuildManualTargetsSpec(buildManualTargetsSection.value)

    override fun default(): Result<BuildManualTargetsSpec> = Result.success(defaultBuildManualTargetsSpec)
}
