package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection

data class ImportDepthSpec(
    override val value: Int
) : ExecutionContextSingletonEntity<Int>()

internal object ImportDepthSpecMapper : ProjectViewToExecutionContextEntityMapper<ImportDepthSpec> {

    override fun map(projectView: ProjectView): Result<ImportDepthSpec> =
        when (projectView.importDepth) {
            null -> default()
            else -> Result.success(map(projectView.importDepth!!))
        }

    override fun default(): Result<ImportDepthSpec> = Result.success(ImportDepthSpec(0))

    private fun map(importDepthSection: ProjectViewImportDepthSection): ImportDepthSpec =
        ImportDepthSpec(importDepthSection.value)

}
