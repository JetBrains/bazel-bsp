package org.jetbrains.bsp.bazel.projectview.generator.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection

abstract class ProjectViewExcludableListSectionGenerator<V, in T : ProjectViewExcludableListSection<V>> :
    ProjectViewListSectionGenerator<V, T>() {

    override fun generatePrettyStringForNonNull(section: T): String {
        val includedValuesPrettyStringRepresentation = generatePrettyStringForValues(
            section.values,
            ::generatePrettyStringForValueWithFourLeadingSpaces
        )
        val excludedValuesPrettyStringRepresentation = generatePrettyStringForValues(
            section.excludedValues,
            ::generatePrettyStringForExcludedValueWithFourLeadingSpacesAndExcludeSign
        )

        return "${section.sectionName}:\n$includedValuesPrettyStringRepresentation\n$excludedValuesPrettyStringRepresentation"
    }

    private fun generatePrettyStringForExcludedValueWithFourLeadingSpacesAndExcludeSign(value: V): String =
        "    -${generatePrettyStringForValue(value)}"
}

class ProjectViewTargetsSectionGenerator :
    ProjectViewExcludableListSectionGenerator<BuildTargetIdentifier, ProjectViewTargetsSection>() {

    override fun generatePrettyStringForValue(value: BuildTargetIdentifier): String =
        value.uri
}
