package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection

data class BuildManualTargetsSpec(override val value: Boolean) : ExecutionContextSingletonEntity<Boolean>()

private val defaultBuildManualTargetsSpec = BuildManualTargetsSpec(
    value = false
)

internal object BuildManualTargetsSpecExtractor : ExecutionContextEntityExtractor<BuildManualTargetsSpec> {

    override fun fromProjectView(projectView: ProjectView): BuildManualTargetsSpec =
        if (projectView.buildManualTargets == null) default()
        else map(projectView.buildManualTargets!!)

    private fun map(buildManualTargetsSection: ProjectViewBuildManualTargetsSection): BuildManualTargetsSpec =
            BuildManualTargetsSpec(buildManualTargetsSection.value)

    override fun default(): BuildManualTargetsSpec = defaultBuildManualTargetsSpec
}
