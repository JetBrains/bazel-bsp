package org.jetbrains.bsp.bazel.projectview.generator.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import java.nio.file.Path
import kotlin.io.path.pathString

abstract class ProjectViewExcludableListSectionGenerator<V, in T : ProjectViewExcludableListSection<V>> :
    ProjectViewListSectionGenerator<V, T>() {

    /**
     * Returns pretty representation of an excludable list section, it means that the format looks like that:
     *
     * <section name>:
     *     <included value 1> (4 leading spaces)
     *     <included value 2>
     *     <included value 3>
     *     ...
     *     <excluded value 1>
     *     <excluded value 2>
     *     ...
     */
    override fun generatePrettyStringForNonNull(section: T): String {
        val includedValuesPrettyStringRepresentation = generatePrettyStringForValues(
            section.values, ::generatePrettyStringForValueWithFourLeadingSpaces
        )
        val excludedValuesPrettyStringRepresentation = generatePrettyStringForValues(
            section.excludedValues, ::generatePrettyStringForExcludedValueWithFourLeadingSpacesAndExcludeSign
        )

        return listOfNotNull(
            "${section.sectionName}:",
            includedValuesPrettyStringRepresentation,
            excludedValuesPrettyStringRepresentation,
        ).joinToString(separator = "\n")
    }

    private fun generatePrettyStringForExcludedValueWithFourLeadingSpacesAndExcludeSign(value: V): String =
        "    -${generatePrettyStringForValue(value)}"
}

object ProjectViewTargetsSectionGenerator :
    ProjectViewExcludableListSectionGenerator<BuildTargetIdentifier, ProjectViewTargetsSection>() {

    override fun generatePrettyStringForValue(value: BuildTargetIdentifier): String = value.uri
}

object ProjectViewDirectoriesSectionGenerator :
        ProjectViewExcludableListSectionGenerator<Path, ProjectViewDirectoriesSection>() {

    override fun generatePrettyStringForValue(value: Path): String = value.pathString
}
