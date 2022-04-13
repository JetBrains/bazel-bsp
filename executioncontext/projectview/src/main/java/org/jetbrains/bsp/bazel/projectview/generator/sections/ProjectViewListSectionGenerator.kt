package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.vavr.collection.List
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection

abstract class ProjectViewListSectionGenerator<V, in T : ProjectViewListSection<V>> :
    ProjectViewSectionGenerator<T>() {

    override fun generatePrettyStringRepresentationForNonNull(section: T): String {
        val valuesPrettyStringRepresentation = generatePrettyStringRepresentationForValues(section.values)

        return "${section.sectionName}:\n${valuesPrettyStringRepresentation}"
    }

    private fun generatePrettyStringRepresentationForValues(values: List<V>): String =
        values.asJava().toList()
            .joinToString(
                separator = "\n",
                transform = ::generatePrettyStringRepresentationForValueWithLeadingFourSpaces
            )

    private fun generatePrettyStringRepresentationForValueWithLeadingFourSpaces(value: V): String =
        "    ${generatePrettyStringRepresentationForValue(value)}"

    protected open fun generatePrettyStringRepresentationForValue(value: V): String = value.toString()
}

class ProjectViewBuildFlagsSectionGenerator : ProjectViewListSectionGenerator<String, ProjectViewBuildFlagsSection>()
