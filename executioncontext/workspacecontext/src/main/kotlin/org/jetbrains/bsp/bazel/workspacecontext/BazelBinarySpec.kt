package org.jetbrains.bsp.bazel.workspacecontext

import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.commons.FileUtils
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractorException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.io.File
import java.net.URI
import java.nio.file.Path

data class BazelBinarySpec(
  override val value: Path,
) : ExecutionContextSingletonEntity<Path>()

private val log = LogManager.getLogger(BazelBinarySpec::class.java)

// TODO(abrams): update tests for the whole flow and mock different OSes
internal object BazelBinarySpecExtractor : ExecutionContextEntityExtractor<BazelBinarySpec> {

  override fun fromProjectView(projectView: ProjectView): BazelBinarySpec {
    val extracted = projectView.bazelBinary?.value
    return if (extracted != null) {
      BazelBinarySpec(extracted)
    } else {
      val path = findBazelOnPathOrNull() ?: downloadBazelisk()
      ?: throw ExecutionContextEntityExtractorException(
        "bazel path",
        "Could not find bazel on your PATH nor download bazelisk"
      )
      BazelBinarySpec(path)
    }
  }


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
    } else {
      log.info("Downloading bazelisk to the cache folder: ${bazeliskFile.path}")
      org.apache.commons.io.FileUtils.copyURLToFile(downloadLink,
        bazeliskFile,
        60 * 1000,
        60 * 1000)
      log.info("Downloaded bazelisk")
      bazeliskFile.setExecutable(true)
      log.info("Set bazelisk binary to be executable")
    }
    return bazeliskFile.toPath()
  }

  private fun calculateBazeliskDownloadLink(): String? {
    // TODO: https://youtrack.jetbrains.com/issue/BAZEL-743
    // Currently updates are checked on CI daily and automatic PR created on new version.
    // Permanent solution should be done later.
    val base = "https://github.com/bazelbuild/bazelisk/releases/download/v1.19.0/bazelisk-"
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    val suffix = when {
      os.startsWith("windows") && arch == "amd64" -> "windows-amd64.exe"
      os.startsWith("linux") && arch == "amd64" -> "linux-amd64"
      os.startsWith("linux") && arch == "arm64" -> "linux-arm64"
      os.startsWith("mac") -> "darwin"
      else -> null
    }
    if (suffix == null) {
      log.error("Could not calculate bazelisk download link (your OS should be one of: windows-amd64, linux-amd64, linux-arm64, darwin)")
    }
    return base + suffix
  }

  private fun findBazelOnPathOrNull(): Path? =
    splitPath()
      .flatMap { listOf(bazelFile(it, "bazel"), bazelFile(it, "bazelisk")) }
      .filterNotNull()
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
