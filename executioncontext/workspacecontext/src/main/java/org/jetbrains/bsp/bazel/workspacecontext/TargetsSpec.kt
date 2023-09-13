package org.jetbrains.bsp.bazel.workspacecontext

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextExcludableListEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractorException
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

internal object TargetsSpecExtractor : ExecutionContextEntityExtractor<TargetsSpec> {

    private const val NAME = "targets"

    override fun fromProjectView(projectView: ProjectView): TargetsSpec =
        if (projectView.deriveTargetsFromDirectories?.value == true)
            deriveTargetsFromDirectoriesSectionTrue(projectView)
        else
            deriveTargetsFromDirectoriesSectionFalse(projectView)

    private fun deriveTargetsFromDirectoriesSectionTrue(projectView: ProjectView): TargetsSpec =
        when {
            projectView.directories == null -> deriveTargetsFromDirectoriesSectionFalse(projectView)
            hasEmptyIncludedValuesAndEmptyExcludedValuesDirectories(projectView.directories!!) -> deriveTargetsFromDirectoriesSectionFalse(
                projectView
            )

            hasEmptyIncludedValuesAndNonEmptyExcludedValuesDirectories(projectView.directories!!) -> throw ExecutionContextEntityExtractorException(
                NAME, "'directories' section has no included targets."
            )

            else -> mapNotEmptyDerivedTargetSection(projectView.targets, projectView.directories!!)
        }

    private fun deriveTargetsFromDirectoriesSectionFalse(projectView: ProjectView): TargetsSpec =
        when {
            projectView.targets == null -> defaultTargetsSpec
            hasEmptyIncludedValuesAndEmptyExcludedValues(projectView.targets!!) -> defaultTargetsSpec
            hasEmptyIncludedValuesAndNonEmptyExcludedValues(projectView.targets!!) -> throw ExecutionContextEntityExtractorException(
                NAME, "'targets' section has no included targets."
            )

            else -> mapNotEmptyNotDerivedTargetsSection(projectView.targets!!)
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

    private fun mapNotEmptyDerivedTargetSection(
        targetsSection: ProjectViewTargetsSection?,
        directoriesSection: ProjectViewDirectoriesSection,
    ): TargetsSpec {
        val directoriesValues = directoriesSection.values.map { mapDirectoryToTarget(it) }
        val directoriesExcludedValues = directoriesSection.excludedValues.map { mapDirectoryToTarget(it) }

        return TargetsSpec(
            targetsSection?.values.orEmpty() + directoriesValues,
            targetsSection?.excludedValues.orEmpty() + directoriesExcludedValues
        )
    }

    private fun mapDirectoryToTarget(buildDirectoryIdentifier: Path): BuildTargetIdentifier =
        if (buildDirectoryIdentifier.pathString == ".")
            BuildTargetIdentifier("//...")
        else
            BuildTargetIdentifier("//" + buildDirectoryIdentifier.pathString + "/...")
}
