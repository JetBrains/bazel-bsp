package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection

data class BuildManualTargetsSpec(override val value: Boolean) : ExecutionContextSingletonEntity<Boolean>()

private val defaultBuildManualTargetsSpec = BuildManualTargetsSpec(
    value = false
)

internal object BuildManualTargetsSpecMapper : ProjectViewToExecutionContextEntityMapper<BuildManualTargetsSpec> {

    override fun map(projectView: ProjectView): Try<BuildManualTargetsSpec> =
        if (projectView.buildManualTargets == null) default()
        else Try.success(map(projectView.buildManualTargets!!))

    private fun map(buildManualTargetsSection: ProjectViewBuildManualTargetsSection): BuildManualTargetsSpec =
            BuildManualTargetsSpec(buildManualTargetsSection.value)

    override fun default(): Try<BuildManualTargetsSpec> = Try.success(defaultBuildManualTargetsSpec)
}
