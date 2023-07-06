package org.jetbrains.bsp.bazel.projectview.parser.sections

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildManualTargetsSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDeriveTargetsFromDirectoriesSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Implementation of single value section parser.
 *
 *
 * It takes a raw section and trims the content.
 *
 * @param <T> type of parsed single value section
</T> */
abstract class ProjectViewSingletonSectionParser<V, T : ProjectViewSingletonSection<V>> protected constructor(
    override val sectionName: String
) : ProjectViewSectionParser<T>() {

    override fun parse(rawSections: ProjectViewRawSections): T? =
        rawSections.getLastSectionWithName(sectionName)
            ?.let { parse(it) }
            ?.get()
            .also { log.debug("Parsed '$sectionName' section. Result:\n$it") }

    override fun parse(sectionBody: String): T? =
        sectionBody.trim()
            .ifBlank { null }
            ?.let { mapRawValue(it) }
            ?.let { createInstance(it) }

    protected abstract fun mapRawValue(rawValue: String): V

    protected abstract fun createInstance(value: V): T

    companion object {
        private val log = LogManager.getLogger(ProjectViewSingletonSectionParser::class.java)
    }
}


object ProjectViewBazelPathSectionParser :
    ProjectViewSingletonSectionParser<Path, ProjectViewBazelPathSection>(ProjectViewBazelPathSection.SECTION_NAME) {

    override fun mapRawValue(rawValue: String): Path = Path(rawValue)

    override fun createInstance(value: Path): ProjectViewBazelPathSection = ProjectViewBazelPathSection(value)
}

object ProjectViewDeriveTargetsFromDirectoriesSectionParser :
        ProjectViewSingletonSectionParser<Boolean, ProjectViewDeriveTargetsFromDirectoriesSection>(ProjectViewDeriveTargetsFromDirectoriesSection.SECTION_NAME) {

    override fun mapRawValue(rawValue: String): Boolean = rawValue.toBoolean()

    override fun createInstance(value: Boolean): ProjectViewDeriveTargetsFromDirectoriesSection =
            ProjectViewDeriveTargetsFromDirectoriesSection(value)
}

object ProjectViewBuildManualTargetsSectionParser :
    ProjectViewSingletonSectionParser<Boolean, ProjectViewBuildManualTargetsSection>(ProjectViewBuildManualTargetsSection.SECTION_NAME)
{

    override fun mapRawValue(rawValue: String): Boolean = rawValue.toBoolean()

    override fun createInstance(value: Boolean): ProjectViewBuildManualTargetsSection = ProjectViewBuildManualTargetsSection(value)
}


object ProjectViewImportDepthSectionParser :
    ProjectViewSingletonSectionParser<Int, ProjectViewImportDepthSection>(ProjectViewImportDepthSection.SECTION_NAME) {

    override fun mapRawValue(rawValue: String): Int = rawValue.toInt()

    override fun createInstance(value: Int): ProjectViewImportDepthSection =
        ProjectViewImportDepthSection(value)
}
