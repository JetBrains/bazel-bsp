package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewProduceTraceLogSection

data class ProduceTraceLogSpec(override val value: Boolean) : ExecutionContextSingletonEntity<Boolean>()

private val defaultBuildManualTargetsSpec = ProduceTraceLogSpec(
    value = false
)

internal object ProduceTraceLogSpecMapper : ProjectViewToExecutionContextEntityMapper<ProduceTraceLogSpec> {

    override fun map(projectView: ProjectView): Try<ProduceTraceLogSpec> =
        when (val produceTraceLog = projectView.produceTraceLog) {
            null -> default()
            else -> Try.success(map(produceTraceLog))
        }

    private fun map(projectViewProduceTraceLogSection: ProjectViewProduceTraceLogSection): ProduceTraceLogSpec =
        ProduceTraceLogSpec(projectViewProduceTraceLogSection.value)

    override fun default(): Try<ProduceTraceLogSpec> = Try.success(defaultBuildManualTargetsSpec)
}
