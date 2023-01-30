package org.jetbrains.bsp.bazel.projectview.parser

import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.utils.dope.DopeFiles
import org.jetbrains.bsp.bazel.utils.dope.DopeTemp
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path

object ProjectViewParserTestMock : DefaultProjectViewParser() {

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
            ProjectViewParserTestMock::class.java.getResourceAsStream(filePath.toString())

        return inputStream
            ?.let { InputStreamReader(it, Charsets.UTF_8) }
            ?.let { CharStreams.toString(it) }
    }

    private fun createTempFileWithContentIfContentExists(path: Path, content: String?): Path =
        content?.let { createTempFileWithContent(path, it) } ?: path

    private fun createTempFileWithContent(path: Path, content: String): Path {
        val tempFile = DopeTemp.createTempFile(path.toString())
        DopeFiles.writeText(tempFile, content)

        return tempFile
    }
}
