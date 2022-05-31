package org.jetbrains.bsp.bazel.projectview.parser.sections

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.projectview.model.sections.*
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


object ProjectViewDebuggerAddressSectionParser :
    ProjectViewSingletonSectionParser<String, ProjectViewDebuggerAddressSection>(ProjectViewDebuggerAddressSection.SECTION_NAME) {

    override fun mapRawValue(rawValue: String): String = rawValue

    override fun createInstance(value: String): ProjectViewDebuggerAddressSection =
        ProjectViewDebuggerAddressSection(value)
}

object ProjectViewDeriveTargetsFlagSectionParser :
        ProjectViewSingletonSectionParser<Boolean, ProjectViewDeriveTargetsFlagSection>(ProjectViewDeriveTargetsFlagSection.SECTION_NAME) {

    override fun mapRawValue(rawValue: String): Boolean = rawValue.toBoolean()

    override fun createInstance(value: Boolean): ProjectViewDeriveTargetsFlagSection =
            ProjectViewDeriveTargetsFlagSection(value)
}

object ProjectViewJavaPathSectionParser :
    ProjectViewSingletonSectionParser<Path, ProjectViewJavaPathSection>(ProjectViewJavaPathSection.SECTION_NAME) {

    override fun mapRawValue(rawValue: String): Path = Path(rawValue)

    override fun createInstance(value: Path): ProjectViewJavaPathSection = ProjectViewJavaPathSection(value)
}
