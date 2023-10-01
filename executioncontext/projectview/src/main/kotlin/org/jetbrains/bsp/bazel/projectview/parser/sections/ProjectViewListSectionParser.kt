package org.jetbrains.bsp.bazel.projectview.parser.sections

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import java.util.regex.Pattern

/**
 * Implementation of list section parser.
 *
 *
 * It takes a raw section and search for entries - they are split by whitespaces, no entry is
 * excluded even if it starts with '-'.
 *
 * @param <T> type of parsed list section
</T> */
abstract class ProjectViewListSectionParser<V, T : ProjectViewListSection<V>> protected constructor(
    override val sectionName: String,
) : ProjectViewSectionParser<T>() {

    override fun parse(rawSections: ProjectViewRawSections): T? = try {
        parseAllSectionsAndMerge(rawSections)?.also { log.debug("Parsed '$sectionName' section. Result:\n$it") }
    } catch (e: Exception) {
        log.error("Failed to parse '$sectionName' section.", e)
        null
    }

    private fun parseAllSectionsAndMerge(rawSections: ProjectViewRawSections): T? =
        rawSections.getAllWithName(sectionName)
            .mapNotNull { parse(it) }
            .reduceOrNull(::concatSectionsItems)

    protected open fun concatSectionsItems(section1: T, section2: T): T =
        createInstance(section1.values + section2.values)

    override fun parse(sectionBody: String): T? {
        val allEntries = splitSectionEntries(sectionBody)

        return parse(allEntries)
    }

    private fun splitSectionEntries(sectionBody: String): List<String> =
        WHITESPACE_CHAR_REGEX.split(sectionBody).filter { it.isNotBlank() }

    protected open fun parse(allEntries: List<String>): T? {
        val values = allEntries.map { mapRawValues(it) }
        return createInstanceOrNull(values)
    }

    protected abstract fun mapRawValues(rawValue: String): V

    private fun createInstanceOrNull(values: List<V>): T? =
        values.ifEmpty { null }?.let { createInstance(values) }

    protected abstract fun createInstance(values: List<V>): T

    companion object {
        private val log = LogManager.getLogger(ProjectViewListSectionParser::class.java)
        private val WHITESPACE_CHAR_REGEX = Pattern.compile("[ \n\t]+")
    }
}


object ProjectViewBuildFlagsSectionParser :
    ProjectViewListSectionParser<String, ProjectViewBuildFlagsSection>(ProjectViewBuildFlagsSection.SECTION_NAME) {

    override fun mapRawValues(rawValue: String): String =rawValue

    override fun createInstance(values: List<String>): ProjectViewBuildFlagsSection = ProjectViewBuildFlagsSection(values)
}
