package org.jetbrains.bsp.bazel.projectview.parser.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Implementation of excludable list section parser.
 *
 *
 * It takes a raw section and search for entries - they are split by whitespaces, if entry starts
 * with '-' -- this entry is excluded, otherwise included.
 *
 * @param <T> type of parsed list section
</T> */
abstract class ProjectViewExcludableListSectionParser<V, T : ProjectViewExcludableListSection<V>> protected constructor(
    override val sectionName: String,
) : ProjectViewListSectionParser<V, T>(sectionName) {

    override fun concatSectionsItems(section1: T, section2: T): T =
        createInstance(
            includedValues = section1.values + section2.values,
            excludedValues = section1.excludedValues + section2.excludedValues
        )

    override fun parse(allEntries: List<String>): T? {
        val rawIncludedEntries = filterIncludedEntries(allEntries)
        val rawExcludedEntries = filterExcludedEntries(allEntries)

        val includedEntries = rawIncludedEntries.map { mapRawValues(it) }
        val excludedEntries = rawExcludedEntries.map { mapRawValues(it) }

        return createInstanceOrEmpty(includedEntries, excludedEntries)
    }

    private fun filterIncludedEntries(entries: List<String>): List<String> =
        entries.filterNot { isExcluded(it) }

    private fun filterExcludedEntries(entries: List<String>): List<String> =
        entries
            .filter { isExcluded(it) }
            .map { removeExcludedEntryPrefix(it) }

    private fun removeExcludedEntryPrefix(excludedEntry: String): String = excludedEntry.drop(1)

    private fun isExcluded(entry: String): Boolean = entry.startsWith(EXCLUDED_ENTRY_PREFIX)

    private fun createInstanceOrEmpty(includedValues: List<V>, excludedValues: List<V>): T? =
        if (includedValues.isNotEmpty() or excludedValues.isNotEmpty()) createInstance(includedValues, excludedValues)
        else null

    override fun createInstance(values: List<V>): T = createInstance(values, emptyList())

    protected abstract fun createInstance(includedValues: List<V>, excludedValues: List<V>): T

    private companion object {
        private const val EXCLUDED_ENTRY_PREFIX = "-"
    }
}


object ProjectViewTargetsSectionParser :
    ProjectViewExcludableListSectionParser<BuildTargetIdentifier, ProjectViewTargetsSection>(ProjectViewTargetsSection.SECTION_NAME) {

    override fun mapRawValues(rawValue: String): BuildTargetIdentifier = BuildTargetIdentifier(rawValue)

    override fun createInstance(
        includedValues: List<BuildTargetIdentifier>, excludedValues: List<BuildTargetIdentifier>
    ): ProjectViewTargetsSection = ProjectViewTargetsSection(includedValues, excludedValues)
}

object ProjectViewDirectoriesSectionParser :
        ProjectViewExcludableListSectionParser<Path, ProjectViewDirectoriesSection>(ProjectViewDirectoriesSection.SECTION_NAME) {

    override fun mapRawValues(rawValue: String): Path = Path(rawValue)

    override fun createInstance(
            includedValues: List<Path>, excludedValues: List<Path>
    ): ProjectViewDirectoriesSection = ProjectViewDirectoriesSection(includedValues, excludedValues)
}
