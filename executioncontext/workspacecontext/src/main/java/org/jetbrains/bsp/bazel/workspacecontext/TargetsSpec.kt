package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextExcludableListEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDeriveTargetsFlagSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
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
            if (deriveTargetsFlags(projectView.deriveTargetsFlag!!))
                deriveTargetsFlagTrue(projectView)
            else
                deriveTargetsFlagFalse(projectView)

    private fun deriveTargetsFlagTrue(projectView: ProjectView): Try<TargetsSpec> =
        when {
            projectView.directories == null -> deriveTargetsFlagFalse(projectView)
            hasEmptyIncludedValuesAndEmptyExcludedValuesDirectories(projectView.directories!!) -> deriveTargetsFlagFalse(projectView)
            hasEmptyIncludedValuesAndNonEmptyExcludedValuesDirectories(projectView.directories!!) -> Try.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    NAME, "'directories' sections have no included targets."
                )
            )
            else -> Try.success(mapNotEmptyDerivedTargetSection(projectView.targets, projectView.directories!!))
        }

    private fun deriveTargetsFlagFalse(projectView: ProjectView): Try<TargetsSpec> =
        when {
            projectView.targets == null -> Try.success(defaultTargetsSpec)
            hasEmptyIncludedValuesAndEmptyExcludedValues(projectView.targets!!) -> Try.success(defaultTargetsSpec)
            hasEmptyIncludedValuesAndNonEmptyExcludedValues(projectView.targets!!) -> Try.failure(
                    ProjectViewToExecutionContextEntityMapperException(
                            NAME, "'targets' section has no included targets."
                    )
            )
            else -> Try.success(mapNotEmptyNotDerivedTargetsSection(projectView.targets!!))
        }

    private fun hasEmptyIncludedValuesAndEmptyExcludedValues(targetsSection: ProjectViewTargetsSection): Boolean =
        targetsSection.values.isEmpty() and targetsSection.excludedValues.isEmpty()
    private fun hasEmptyIncludedValuesAndNonEmptyExcludedValues(targetsSection: ProjectViewTargetsSection): Boolean =
        targetsSection.values.isEmpty() and targetsSection.excludedValues.isNotEmpty()

    private fun hasEmptyIncludedValuesAndEmptyExcludedValuesDirectories(directoriesSection: ProjectViewDirectoriesSection): Boolean =
        directoriesSection.values.isEmpty() and directoriesSection.excludedValues.isEmpty()
    private fun hasEmptyIncludedValuesAndNonEmptyExcludedValuesDirectories(directoriesSection: ProjectViewDirectoriesSection): Boolean =
        directoriesSection.values.isEmpty() and directoriesSection.excludedValues.isNotEmpty()

    private fun deriveTargetsFlags(deriveTargetsFlagSection: ProjectViewDeriveTargetsFlagSection): Boolean =
        deriveTargetsFlagSection.value

    private fun mapNotEmptyNotDerivedTargetsSection(targetsSection: ProjectViewTargetsSection): TargetsSpec =
        TargetsSpec(targetsSection.values, targetsSection.excludedValues)

    private fun mapNotEmptyDerivedTargetSection(targetsSection: ProjectViewTargetsSection?, directoriesSection: ProjectViewDirectoriesSection): TargetsSpec {
        val directoriesValues = directoriesSection.values.map { mapDirectoryToTarget(it) }
        val directoriesExcludedValues = directoriesSection.excludedValues.map { mapDirectoryToTarget(it) }

        return TargetsSpec(
                (targetsSection?.values ?: emptyList()) + directoriesValues,
                (targetsSection?.excludedValues ?: emptyList()) + directoriesExcludedValues)
    }

    private fun mapDirectoryToTarget(buildDirectoryIdentifier: BuildTargetIdentifier): BuildTargetIdentifier =
            BuildTargetIdentifier("//" + buildDirectoryIdentifier.uri + "/...")

    override fun default(): Try<TargetsSpec> = Try.success(defaultTargetsSpec)
}
