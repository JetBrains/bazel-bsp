package org.jetbrains.bsp.bazel.projectview.generator.sections

import io.vavr.collection.List
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection

abstract class ProjectViewListSectionGenerator<V, in T : ProjectViewListSection<V>> :
    ProjectViewSectionGenerator<T>() {

    /**
     * Returns pretty representation of a list section, it means that the format looks like that:
     *
     * <section name>:
     *     <value 1> (4 leading spaces)
     *     <value 2>
     *     <value 3>
     *     ...
     */
    override fun generatePrettyStringForNonNull(section: T): String {
        val valuesPrettyStringRepresentation = generatePrettyStringForValues(
            section.values,
            ::generatePrettyStringForValueWithFourLeadingSpaces
        )

        return "${section.sectionName}:\n${valuesPrettyStringRepresentation}"
    }

    protected fun generatePrettyStringForValues(values: List<V>, transformer: (V) -> String): String =
        values.asJava().toList().joinToString(separator = "\n", transform = transformer)

    protected fun generatePrettyStringForValueWithFourLeadingSpaces(value: V): String =
        "    ${generatePrettyStringForValue(value)}"

    protected open fun generatePrettyStringForValue(value: V): String = value.toString()
}

class ProjectViewBuildFlagsSectionGenerator : ProjectViewListSectionGenerator<String, ProjectViewBuildFlagsSection>()
