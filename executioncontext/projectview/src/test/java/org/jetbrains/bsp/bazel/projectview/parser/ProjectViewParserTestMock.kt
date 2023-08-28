package org.jetbrains.bsp.bazel.projectview.parser

import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempFile

object ProjectViewParserTestMock : DefaultProjectViewParser() {

    override fun parse(projectViewFilePath: Path): ProjectView =
        copyResourcesFileToTmpFile(projectViewFilePath).let { super.parse(it) }

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
        content?.let { createTempFileWithContent(it) } ?: path

    private fun createTempFileWithContent(content: String): Path {
        val tempFile = createTempFile("ProjectViewParserTestMock")

        Files.createDirectories(tempFile.parent)
        Files.writeString(tempFile, content)

        return tempFile
    }
}
