package org.jetbrains.bsp.bazel.projectview.parser

import io.vavr.control.Try
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewDebuggerAddressSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewJavaPathSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewTargetsSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewBazelPathSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewBuildFlagsSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewBuildManualTargetsSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.*
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter
import org.jetbrains.bsp.bazel.utils.dope.DopeFiles
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Default implementation of ProjectViewParser.
 *
 * @see ProjectViewParser
 * @see ProjectViewSectionSplitter
 */
open class DefaultProjectViewParser : ProjectViewParser {

    private val log = LogManager.getLogger(DefaultProjectViewParser::class.java)

    override fun parse(projectViewFilePath: Path): Try<ProjectView> {
        log.info("Parsing project view from {}.", projectViewFilePath)

        return DopeFiles.readText(projectViewFilePath)
            .onFailure { log.error("Failed to read file {}. Parsing failed!", projectViewFilePath, it) }
            .flatMap(this::parse)
            .onSuccess { log.info("Project view from {} parsed!\n{}", projectViewFilePath, it) }
            .onFailure { log.error("Failed to parse file {}. Parsing failed!", projectViewFilePath, it) }
    }

    override fun parse(projectViewFileContent: String): Try<ProjectView> {
        log.debug("Parsing project view for the content:\n'{}'.", projectViewFileContent)

        val rawSections = ProjectViewSectionSplitter.getRawSectionsForFileContent(projectViewFileContent)

        return ProjectView.Builder(
            imports = findImportedProjectViews(rawSections),
            targets = ProjectViewTargetsSectionParser.parse(rawSections),
            bazelPath = ProjectViewBazelPathSectionParser.parse(rawSections),
            debuggerAddress = ProjectViewDebuggerAddressSectionParser.parse(rawSections),
            javaPath = ProjectViewJavaPathSectionParser.parse(rawSections),
            buildFlags = ProjectViewBuildFlagsSectionParser.parse(rawSections),
            buildManualTargets = ProjectViewBuildManualTargetsSectionParser.parse(rawSections),
            directories = ProjectViewDirectoriesSectionParser.parse(rawSections),
            deriveTargetsFromDirectories = ProjectViewDeriveTargetsFromDirectoriesSectionParser.parse(rawSections),
            importDepth = ProjectViewImportDepthSectionParser.parse(rawSections),
            produceTraceLog = ProjectViewProduceTraceLogSectionParser.parse(rawSections),
        ).build()
    }

    private fun findImportedProjectViews(rawSections: ProjectViewRawSections): List<Try<ProjectView>> =
        rawSections
            .getAllWithName(IMPORT_STATEMENT)
            .map { it.sectionBody }
            .map(String::trim)
            .map(::Path)
            .onEach { log.debug("Parsing imported file {}.", it) }
            .map(this::parse)

    companion object {
        private const val IMPORT_STATEMENT = "import"
    }
}
