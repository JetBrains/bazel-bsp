package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView

data class ImportDepthSpec(
    override val value: Int
) : ExecutionContextSingletonEntity<Int>()

internal object ImportDepthSpecExtractor : ExecutionContextEntityExtractor<ImportDepthSpec> {

    override fun fromProjectView(projectView: ProjectView): ImportDepthSpec =
        when (projectView.importDepth) {
            null -> default()
            else -> ImportDepthSpec(projectView.importDepth!!.value)
        }

    override fun default(): ImportDepthSpec = ImportDepthSpec(0)

}
