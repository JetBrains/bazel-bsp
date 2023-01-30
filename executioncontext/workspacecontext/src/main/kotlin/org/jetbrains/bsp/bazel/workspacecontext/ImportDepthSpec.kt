package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection

data class ImportDepthSpec(
    override val value: Int
) : ExecutionContextSingletonEntity<Int>()

internal object ImportDepthSpecMapper : ProjectViewToExecutionContextEntityMapper<ImportDepthSpec> {

    override fun map(projectView: ProjectView): Try<ImportDepthSpec> =
        when (projectView.importDepth) {
            null -> default()
            else -> Try.success(map(projectView.importDepth!!))
        }

    override fun default(): Try<ImportDepthSpec> = Try.success(ImportDepthSpec(0))

    private fun map(importDepthSection: ProjectViewImportDepthSection): ImportDepthSpec =
        ImportDepthSpec(importDepthSection.value)

}
