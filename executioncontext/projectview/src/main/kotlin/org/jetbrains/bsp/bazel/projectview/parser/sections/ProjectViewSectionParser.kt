package org.jetbrains.bsp.bazel.projectview.parser.sections

import io.vavr.control.Try
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections

abstract class ProjectViewSectionParser<T : ProjectViewSection> {

    abstract val sectionName: String

    abstract fun parse(rawSections: ProjectViewRawSections): T?

    fun parse(rawSection: ProjectViewRawSection): Try<T?> =
        getSectionBodyOrFailureIfNameIsWrong(rawSection)
            .onFailure {
                log.error(
                    "Failed to parse section with '${rawSection.sectionName}'. Expected name: '$sectionName'. Parsing failed!",
                    it
                )
            }
            .map { parse(it) }

    private fun getSectionBodyOrFailureIfNameIsWrong(rawSection: ProjectViewRawSection): Try<String> {
        if (rawSection.sectionName != sectionName) {
            val exceptionMessage =
                "Project view parsing failed! Expected '$sectionName' section name, got '${rawSection.sectionName}'!"
            return Try.failure(IllegalArgumentException(exceptionMessage))
        }
        return Try.success(rawSection.sectionBody)
    }

    protected abstract fun parse(sectionBody: String): T?

    companion object {
        private val log = LogManager.getLogger(ProjectViewSectionParser::class.java)
    }
}
