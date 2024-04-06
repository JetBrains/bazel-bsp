package org.jetbrains.bsp.bazel.android

import org.apache.commons.io.FileUtils.copyURLToFile
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.commons.FileUtils
import java.net.URI
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.setPosixFilePermissions

object AndroidSdkDownloader {
  private val log = LogManager.getLogger(AndroidSdkDownloader::class.java)

  private val androidHome: Path? = System.getenv("ANDROID_HOME")?.let { Path(it) }
  val androidSdkPath: Path = androidHome ?:
    checkNotNull(FileUtils.getCacheDirectory("bazel_android_sdk")?.toPath()) { "Couldn't get Android SDK cache dir" }

  fun downloadAndroidSdkIfNeeded() {
    if (androidHome != null) {
      log.info("Using Android SDK from \$ANDROID_HOME=$androidHome")
      return
    }
    if (androidSdkPath.resolve("platforms").exists()) {
      log.info("Android SDK already downloaded. Delete $androidSdkPath to re-download")
      return
    }

    val androidCommandLineToolsZip = downloadAndroidCommandLineToolsZip()
    unpackAndroidCommandLineToolsZip(androidCommandLineToolsZip)
    downloadSdkWithSdkManager()
  }

  private fun downloadAndroidCommandLineToolsZip(): Path {
    val commandLineToolsDownloadLink = getCommandLineToolsDownloadLink()
    log.info("Downloading $commandLineToolsDownloadLink")
    val commandLineToolsZip = androidSdkPath.resolve("command_line_tools.zip")
    copyURLToFile(
      URI(commandLineToolsDownloadLink).toURL(),
      commandLineToolsZip.toFile(),
      60 * 1000,
      300 * 1000,
    )
    return commandLineToolsZip
  }

  private fun getCommandLineToolsDownloadLink(): String {
    val os = System.getProperty("os.name").lowercase()
    val osPart = when {
      os.startsWith("linux") -> "linux"
      os.startsWith("mac") -> "mac"
      else -> error("Can't download the Android SDK on OS $os. Set the \$ANDROID_HOME environment variable manually.")
    }
    return "https://dl.google.com/android/repository/commandlinetools-$osPart-11076708_latest.zip"
  }

  @OptIn(ExperimentalPathApi::class)
  private fun unpackAndroidCommandLineToolsZip(androidCommandLineToolsZip: Path) {
    val outputDirectory = androidSdkPath.resolve("cmdline-tools")
    outputDirectory.deleteRecursively()
    unzipArchive(androidCommandLineToolsZip, outputDirectory)
    androidCommandLineToolsZip.deleteExisting()
    // Read https://stackoverflow.com/a/67413427/6120487 on why this line is needed
    outputDirectory.resolve("cmdline-tools").moveTo(outputDirectory.resolve("latest"))
  }

  private fun unzipArchive(zip: Path, outputDirectory: Path) =
    runShellCommand("unzip '$zip' -d '$outputDirectory'")

  private fun downloadSdkWithSdkManager() {
    val sdkManager = androidSdkPath.resolve("cmdline-tools/latest/bin/sdkmanager")
    sdkManager.setPosixFilePermissions(
      setOf(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE))
    runShellCommand("yes | '$sdkManager' --licenses")
    runShellCommand("'$sdkManager' 'platforms;android-34' 'build-tools;34.0.0'")
  }

  private fun runShellCommand(command: String) {
    log.info("Invoking $command")
    val process = ProcessBuilder("/bin/sh", "-c", command)
      .inheritIO()
      .start()
    process.waitFor(5, TimeUnit.MINUTES)
    check(process.exitValue() == 0) { "$command exited with exit code ${process.exitValue()}" }
  }
}
