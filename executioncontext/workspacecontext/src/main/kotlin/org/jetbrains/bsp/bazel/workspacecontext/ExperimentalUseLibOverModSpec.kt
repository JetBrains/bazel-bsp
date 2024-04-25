package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.projectview.model.ProjectView

data class ExperimentalUseLibOverModSpec(
  override val value: Boolean,
) : ExecutionContextSingletonEntity<Boolean>()

internal object ExperimentalUseLibOverModSpecExtractor : ExecutionContextEntityExtractor<ExperimentalUseLibOverModSpec> {
  override fun fromProjectView(projectView: ProjectView): ExperimentalUseLibOverModSpec =
    ExperimentalUseLibOverModSpec(projectView.useLibOverModSection?.value ?: false)
}