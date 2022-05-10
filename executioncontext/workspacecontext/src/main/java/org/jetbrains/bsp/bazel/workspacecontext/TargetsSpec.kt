package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextExcludableListEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection

data class TargetsSpec(
    override val values: List<BuildTargetIdentifier>,
    override val excludedValues: List<BuildTargetIdentifier>,
) : ExecutionContextExcludableListEntity<BuildTargetIdentifier>()

private val defaultTargetsSpec = TargetsSpec(
    values = listOf(BuildTargetIdentifier("//...")),
    excludedValues = emptyList(),
)

internal object TargetsSpecMapper : ProjectViewToExecutionContextEntityMapper<TargetsSpec> {

    private const val NAME = "targets"

    override fun map(projectView: ProjectView): Try<TargetsSpec> =
        when {
            projectView.targets == null -> Try.success(defaultTargetsSpec)
            hasEmptyIncludedValuesAndEmptyExcludedValues(projectView.targets!!) -> Try.success(defaultTargetsSpec)
            hasEmptyIncludedValuesAndNonEmptyExcludedValues(projectView.targets!!) -> Try.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    NAME, "'targets' section has no included targets."
                )
            )
            else -> Try.success(mapNotEmptySection(projectView.targets!!))
        }

    private fun hasEmptyIncludedValuesAndEmptyExcludedValues(targetsSection: ProjectViewTargetsSection): Boolean =
        targetsSection.values.isEmpty() and targetsSection.excludedValues.isEmpty()
    private fun hasEmptyIncludedValuesAndNonEmptyExcludedValues(targetsSection: ProjectViewTargetsSection): Boolean =
        targetsSection.values.isEmpty() and targetsSection.excludedValues.isNotEmpty()

    private fun mapNotEmptySection(targetsSection: ProjectViewTargetsSection): TargetsSpec =
        TargetsSpec(targetsSection.values, targetsSection.excludedValues)

    override fun default(): Try<TargetsSpec> = Try.success(defaultTargetsSpec)
}
