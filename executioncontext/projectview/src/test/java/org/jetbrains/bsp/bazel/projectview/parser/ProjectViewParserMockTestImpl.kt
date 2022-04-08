package org.jetbrains.bsp.bazel.projectview.parser

import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import io.vavr.control.Option
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.writeText

class ProjectViewParserMockTestImpl : ProjectViewParserImpl() {

    override fun parse(projectViewFilePath: Path, defaultProjectViewFilePath: Path): Try<ProjectView> =
        Try.success(copyResourcesFileToTmpFile(projectViewFilePath))
            .flatMap { parseWithCopiedProjectViewFile(it, defaultProjectViewFilePath) }

    private fun parseWithCopiedProjectViewFile(
        copiedProjectViewFilePath: Path,
        defaultProjectViewFile: Path
    ): Try<ProjectView> =
        Try.success(copyResourcesFileToTmpFile(defaultProjectViewFile))
            .flatMap { super.parse(copiedProjectViewFilePath, it) }

    override fun parse(projectViewFilePath: Path): Try<ProjectView> =
        Try.success(copyResourcesFileToTmpFile(projectViewFilePath))
            .flatMap { super.parse(it) }

    private fun copyResourcesFileToTmpFile(resourcesFile: Path): Path {
        val resourcesFileContent = readFileContent(resourcesFile)

        return createTempFileWithContentIfContentExists(resourcesFile, resourcesFileContent)
    }

    // TODO @abrams27 - move to utils
    private fun readFileContent(filePath: Path): String? {
        // we read file content instead of passing plain file due to bazel resources packaging
        val inputStream: InputStream? =
            ProjectViewParserMockTestImpl::class.java.getResourceAsStream(filePath.toString())

        return inputStream
            ?.let { InputStreamReader(it, Charsets.UTF_8) }
            ?.let { CharStreams.toString(it) }
    }

    private fun createTempFileWithContentIfContentExists(path: Path, content: String?): Path =
        content?.let { createTempFileWithContent(path, it) } ?: path

    private fun createTempFileWithContent(path: Path, content: String): Path {
        val tempFile = File.createTempFile(path.toString(), "")
        tempFile.deleteOnExit()
        tempFile.writeText(content)

        return tempFile.toPath()
    }
}
