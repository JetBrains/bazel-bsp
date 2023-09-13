package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView

data class ImportDepthSpec(
    override val value: Int
) : ExecutionContextSingletonEntity<Int>()

private val default = ImportDepthSpec(0)

internal object ImportDepthSpecExtractor : ExecutionContextEntityExtractor<ImportDepthSpec> {

    override fun fromProjectView(projectView: ProjectView): ImportDepthSpec =
        when (projectView.importDepth) {
            null -> default
            else -> ImportDepthSpec(projectView.importDepth!!.value)
        }
}
