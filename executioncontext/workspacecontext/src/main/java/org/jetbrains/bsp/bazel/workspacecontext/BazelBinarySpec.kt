package org.jetbrains.bsp.bazel.workspacecontext

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.commons.FileUtils
import org.jetbrains.bsp.bazel.commons.FileUtils.downloadFile
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.io.File
import java.net.URI
import java.nio.file.Path

data class BazelBinarySpec(
    override val value: Path,
) : ExecutionContextSingletonEntity<Path>()

val log = LogManager.getLogger(BazelBinarySpec::class.java)

internal object BazelBinarySpecMapper : ProjectViewToExecutionContextEntityMapper<BazelBinarySpec> {

    override fun map(projectView: ProjectView): Result<BazelBinarySpec> =
        (projectView.bazelBinary?.value
            ?: findBazelOnPathOrNull()
            ?: downloadBazelisk())
            ?.let { Result.success(BazelBinarySpec(it)) }
            ?: Result.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    "bazel path",
                    "Could not find bazel on your PATH and could not download bazelisk"
                )
            )

    override fun default(): Result<BazelBinarySpec> = findBazelOnPath()

    // TODO: update tests for the whole flow and mock different OSes
    private fun findBazelOnPath(): Result<BazelBinarySpec> =
        findBazelOnPathOrNull()?.let { Result.success(BazelBinarySpec(it)) }
            ?: Result.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    "bazel path",
                    "Could not find bazel on your PATH"
                )
            )

    private fun downloadBazelisk(): Path? {
        log.info("Downloading bazelisk")
        val downloadLink = calculateBazeliskDownloadLink()?.let {
            try {
                URI(it).toURL()
            } catch (e: Exception) {
                log.error("Could not parse bazelisk download link: $it")
                return null
            }
        }
        if (downloadLink == null) {
            log.error("Could not calculate bazelisk download link (your OS should be one of: windows-amd64, linux-amd64, linux-arm64, darwin)")
            return null
        }
        val cache = FileUtils.getCacheDirectory("bazelbsp")
        if (cache == null) {
            log.error("Could not find cache directory")
            return null
        }
        // Download bazelisk to the cache folder
        val bazeliskFile = File(cache, "bazelisk")
        if (bazeliskFile.exists()) {
            log.info("Bazelisk already exists in the cache folder: ${bazeliskFile.path}")
            return null
        }
        downloadFile(downloadLink, bazeliskFile)
        return bazeliskFile.toPath()
    }

    private fun calculateBazeliskDownloadLink(): String? {
        // TODO: probably we don't want to hardcode this
        val base = "https://github.com/bazelbuild/bazelisk/releases/download/v1.18.0/bazelisk-"
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        return when {
            os.startsWith("windows") -> if (arch == "amd64") base + "windows-amd64.exe" else null
            os.startsWith("linux") -> if (arch == "amd64") base + "linux-amd64" else if (arch == "arm64") base + "linux-arm64" else null
            os.startsWith("mac") -> base + "darwin"
            else -> null
        }
    }

    private fun findBazelOnPathOrNull(): Path? =
        splitPath()
            .flatMap { listOf(bazelFile(it, "bazel"), bazelFile(it, "bazelisk")) }
            .firstOrNull()

    private fun splitPath(): List<String> = System.getenv("PATH").split(File.pathSeparator)

    private fun bazelFile(path: String, executable: String): Path? {
        val file = File(path, calculateExecutableName(executable))
        return if (file.exists() && file.canExecute()) file.toPath() else null
    }

    private fun calculateExecutableName(name: String): String = when {
        System.getProperty("os.name").lowercase().startsWith("windows") -> "$name.exe"
        else -> name
    }

}
