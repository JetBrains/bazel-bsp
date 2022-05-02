package org.jetbrains.bsp.bazel.install

import io.vavr.control.Option
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.BetterFiles
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParserImpl
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.stream.Collectors


// TODO @abrams27 I'll take care of that later
class ProjectViewDefaultFromResourcesProvider(private val projectViewFilePath: Path?,
                                              private val defaultProjectViewFileResourcesPath: String) : ProjectViewProvider {
    override fun create(): Try<ProjectView> =
            readFileContentFromResources(defaultProjectViewFileResourcesPath).flatMap(::create)

    private fun readFileContentFromResources(resourcesRawPath: String): Try<String> =
            // TODO add https://youtrack.jetbrains.com/issue/BAZEL-18
            Try.of { ProjectViewDefaultFromResourcesProvider::class.java.getResourceAsStream(resourcesRawPath) }
                    .map(::InputStreamReader)
                    .map(::BufferedReader)
                    .mapTry { it.lines() }
                    .map {
                        it.collect(Collectors.joining("\n"))
                    }

    private fun create(defaultProjectViewFileContent: String): Try<ProjectView> =
            Option.of(projectViewFilePath)
                    .map { create(it!!, defaultProjectViewFileContent) }
                    .getOrElse { PARSER.parse(defaultProjectViewFileContent) }

    private fun create(projectViewFilePath: Path, defaultProjectViewFileContent: String): Try<ProjectView> =
            BetterFiles.tryReadFileContent(projectViewFilePath)
                    .flatMap { PARSER.parse(it, defaultProjectViewFileContent) }
                    .orElse { PARSER.parse(defaultProjectViewFileContent) }

    private companion object {
        private val PARSER: ProjectViewParser = ProjectViewParserImpl()
    }
}
