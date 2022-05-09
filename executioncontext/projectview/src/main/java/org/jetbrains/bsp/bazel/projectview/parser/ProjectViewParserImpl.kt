package org.jetbrains.bsp.bazel.projectview.parser

import io.vavr.control.Option
import io.vavr.control.Try
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewBazelPathSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewBuildFlagsSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewDebuggerAddressSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewJavaPathSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.sections.ProjectViewTargetsSectionParser
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSection
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewRawSections
import org.jetbrains.bsp.bazel.projectview.parser.splitter.ProjectViewSectionSplitter
import org.jetbrains.bsp.bazel.utils.dope.DopeFiles
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Default implementation of ProjectViewParser.
 *
 * @see ProjectViewParser
 * @see ProjectViewSectionSplitter
 */
open class ProjectViewParserImpl : ProjectViewParser {

    override fun parse(projectViewFilePath: Path, defaultProjectViewFilePath: Path): Try<ProjectView> {
        log.info("Parsing project view from {} with default from {}.", projectViewFilePath, defaultProjectViewFilePath)

        return DopeFiles.readText(projectViewFilePath)
            .onFailure { log.error("Failed to read file {}. Parsing failed!", projectViewFilePath, it) }
            .flatMap { parse(it, defaultProjectViewFilePath) }
            .onSuccess {
                log.info(
                    "Project view from {} with default from {} parsed!\n{}",
                    projectViewFilePath,
                    defaultProjectViewFilePath,
                    it
                )
            }
            .onFailure { log.error("Failed to parse project view!", it) }
    }

    private fun parse(projectViewFileContent: String, defaultProjectViewFilePath: Path): Try<ProjectView> =
        DopeFiles.readText(defaultProjectViewFilePath)
            .flatMap { parse(projectViewFileContent, it) }


    override fun parse(projectViewFileContent: String, defaultProjectViewFileContent: String): Try<ProjectView> {
        log.debug(
            "Parsing project view for the content:\n'{}'\n\nand the default content:\n'{}'.",
            projectViewFileContent,
            defaultProjectViewFileContent
        )

        return parse(defaultProjectViewFileContent)
            .onFailure { log.error("Failed to parse default content. Parsing failed!", it) }
            .flatMap { parseWithDefault(projectViewFileContent, it) }
            .onFailure { log.error("Failed to parse content. Parsing failed!", it) }
    }

    private fun parseWithDefault(projectViewFileContent: String, defaultProjectView: ProjectView): Try<ProjectView> {
        val rawSections = ProjectViewSectionSplitter.getRawSectionsForFileContent(projectViewFileContent)

        log.debug("Parsing project view with default project view {}.", defaultProjectView)

        return ProjectView.Builder(
            imports = findImportedProjectViews(rawSections),
            targets = targetsParser.parseOrDefault(rawSections, Option.of(defaultProjectView.targets)).orNull,
            bazelPath = bazelPathParser.parseOrDefault(rawSections, Option.of(defaultProjectView.bazelPath)).orNull,
            debuggerAddress = debuggerAddressParser.parseOrDefault(
                rawSections,
                Option.of(defaultProjectView.debuggerAddress)
            ).orNull,
            javaPath = javaPathParser.parseOrDefault(rawSections, Option.of(defaultProjectView.javaPath)).orNull,
            buildFlags = buildFlagsParser.parseOrDefault(rawSections, Option.of(defaultProjectView.buildFlags)).orNull,
        ).build()
    }

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
            targets = targetsParser.parse(rawSections).orNull,
            bazelPath = bazelPathParser.parse(rawSections).orNull,
            debuggerAddress = debuggerAddressParser.parse(rawSections).orNull,
            javaPath = javaPathParser.parse(rawSections).orNull,
            buildFlags = buildFlagsParser.parse(rawSections).orNull,
        ).build()
    }

    private fun findImportedProjectViews(rawSections: ProjectViewRawSections): List<Try<ProjectView>> =
        rawSections
            .getAllWithName(IMPORT_STATEMENT)
            .toJavaList()
            .toList()
            .map(ProjectViewRawSection::getSectionBody)
            .map(String::trim)
            .map(Paths::get)
            .onEach { log.debug("Parsing imported file {}.", it) }
            .map(this::parse)

    private companion object {
        private val log = LogManager.getLogger(ProjectViewParserImpl::class.java)

        private const val IMPORT_STATEMENT = "import"

        private val targetsParser = ProjectViewTargetsSectionParser()
        private val bazelPathParser = ProjectViewBazelPathSectionParser()
        private val debuggerAddressParser = ProjectViewDebuggerAddressSectionParser()
        private val javaPathParser = ProjectViewJavaPathSectionParser()
        private val buildFlagsParser = ProjectViewBuildFlagsSectionParser()
    }
}
