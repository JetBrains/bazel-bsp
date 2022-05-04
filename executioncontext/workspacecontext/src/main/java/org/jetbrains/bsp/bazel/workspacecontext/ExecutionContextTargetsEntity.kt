package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextListEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection

data class ExecutionContextTargetsEntity(
    override val includedValues: List<BuildTargetIdentifier>,
    override val excludedValues: List<BuildTargetIdentifier>,
) : ExecutionContextListEntity<BuildTargetIdentifier>()


internal object WorkspaceContextTargetsEntityMapper : ProjectViewToExecutionContextEntityMapper<ExecutionContextTargetsEntity> {

    private const val NAME = "targets"

    // TODO will be changed anyway soon
    override fun map(projectView: ProjectView): Try<ExecutionContextTargetsEntity> =
        toTry(projectView.targets)
            .flatMap(::validate)
            .map(::map)

    private fun toTry(targetsSection: ProjectViewTargetsSection?): Try<ProjectViewTargetsSection> =
        // TODO will be changed after ProjectView transition into vavr
        targetsSection?.let { Try.success(it) } ?: Try.failure(
            ProjectViewToExecutionContextEntityMapperException(
                NAME, "'targets' section in project view is empty."
            )
        )

    private fun validate(targetsSection: ProjectViewTargetsSection): Try<ProjectViewTargetsSection> {
        return if (targetsSection.values.isEmpty) {
            Try.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    NAME, "'targets' section has no included targets."
                )
            )
        } else Try.success(targetsSection)
    }

    private fun map(targetsSection: ProjectViewTargetsSection): ExecutionContextTargetsEntity =
        ExecutionContextTargetsEntity(
            targetsSection.values.asJava().toList(),
            targetsSection.excludedValues.asJava().toList()
        )
}
