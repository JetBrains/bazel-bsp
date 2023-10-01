package org.jetbrains.bsp.bazel.projectview.parser.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections

abstract class ProjectViewSectionParser<T : ProjectViewSection> {

    abstract val sectionName: String

    abstract fun parse(rawSections: ProjectViewRawSections): T?

    /**
     * Parses a raw section of a project view and returns the parsed result.
     *
     * @param rawSection the raw section to be parsed
     * @return the parsed result of the raw section, or null if parsing fails
     * @throws IllegalArgumentException if the section name of the raw section does not match the expected section name
     */
    fun parse(rawSection: ProjectViewRawSection): T? {
        if (rawSection.sectionName != sectionName) {
            val exceptionMessage =
                "Project view parsing failed. Expected '$sectionName' section name, got '${rawSection.sectionName}'."
            throw IllegalArgumentException(exceptionMessage)
        }
        return parse(rawSection.sectionBody)

    }

    protected abstract fun parse(sectionBody: String): T?
}
