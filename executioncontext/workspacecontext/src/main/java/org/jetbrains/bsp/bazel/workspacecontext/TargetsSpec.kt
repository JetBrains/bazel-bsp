package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextExcludableListEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import java.nio.file.Path
import kotlin.io.path.pathString

data class TargetsSpec(
    override val values: List<BuildTargetIdentifier>,
    override val excludedValues: List<BuildTargetIdentifier>,
) : ExecutionContextExcludableListEntity<BuildTargetIdentifier>()

private val defaultTargetsSpec = TargetsSpec(
    values = emptyList(),
    excludedValues = emptyList(),
)

internal object TargetsSpecMapper : ProjectViewToExecutionContextEntityMapper<TargetsSpec> {

    private const val NAME = "targets"

    override fun map(projectView: ProjectView): Try<TargetsSpec> =
        if (projectView.deriveTargetsFromDirectories?.value == true)
            deriveTargetsFromDirectoriesSectionTrue(projectView)
        else
            deriveTargetsFromDirectoriesSectionFalse(projectView)

    private fun deriveTargetsFromDirectoriesSectionTrue(projectView: ProjectView): Try<TargetsSpec> =
        when {
            projectView.directories == null -> deriveTargetsFromDirectoriesSectionFalse(projectView)
            hasEmptyIncludedValuesAndEmptyExcludedValuesDirectories(projectView.directories!!) -> deriveTargetsFromDirectoriesSectionFalse(projectView)
            hasEmptyIncludedValuesAndNonEmptyExcludedValuesDirectories(projectView.directories!!) -> Try.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    NAME, "'directories' section has no included targets."
                )
            )
            else -> Try.success(mapNotEmptyDerivedTargetSection(projectView.targets, projectView.directories!!))
        }

    private fun deriveTargetsFromDirectoriesSectionFalse(projectView: ProjectView): Try<TargetsSpec> =
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

    private fun mapNotEmptyNotDerivedTargetsSection(targetsSection: ProjectViewTargetsSection): TargetsSpec =
        TargetsSpec(targetsSection.values, targetsSection.excludedValues)

    private fun mapNotEmptyDerivedTargetSection(targetsSection: ProjectViewTargetsSection?, directoriesSection: ProjectViewDirectoriesSection): TargetsSpec {
        val directoriesValues = directoriesSection.values.map { mapDirectoryToTarget(it) }
        val directoriesExcludedValues = directoriesSection.excludedValues.map { mapDirectoryToTarget(it) }

        return TargetsSpec(
                targetsSection?.values.orEmpty() + directoriesValues,
                targetsSection?.excludedValues.orEmpty() + directoriesExcludedValues)
    }

    private fun mapDirectoryToTarget(buildDirectoryIdentifier: Path): BuildTargetIdentifier =
        if (buildDirectoryIdentifier.pathString == ".")
            BuildTargetIdentifier("//...")
        else
            BuildTargetIdentifier("//" + buildDirectoryIdentifier.pathString + "/...")


    override fun default(): Try<TargetsSpec> = Try.success(defaultTargetsSpec)
}
