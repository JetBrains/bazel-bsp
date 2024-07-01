package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.projectview.model.ProjectView

data class ExperimentalAddTransitiveCompileTimeJars(
  override val value: Boolean,
) : ExecutionContextSingletonEntity<Boolean>()

internal object ExperimentalAddTransitiveCompileTimeJarsExtractor :
  ExecutionContextEntityExtractor<ExperimentalAddTransitiveCompileTimeJars> {
  override fun fromProjectView(projectView: ProjectView): ExperimentalAddTransitiveCompileTimeJars =
    ExperimentalAddTransitiveCompileTimeJars(projectView.addTransitiveCompileTimeJars?.value ?: false)
}