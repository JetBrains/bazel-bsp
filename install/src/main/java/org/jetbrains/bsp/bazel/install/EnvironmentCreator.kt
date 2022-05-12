package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import com.google.gson.GsonBuilder
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.Constants
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

abstract class EnvironmentCreator(private val projectRootDir: Path) {

    abstract fun create(): Try<Void>

    protected fun createDotBazelBsp(): Try<Path> {
        val bazelBspDir = createDir(projectRootDir, Constants.DOT_BAZELBSP_DIR_NAME)
        return bazelBspDir.flatMap(::createDotBazelBspFiles)
            .flatMap { bazelBspDir }
    }

    private fun createDotBazelBspFiles(dotBazelBspDir: Path): Try<Void> =
        copyAspects(dotBazelBspDir)
            .flatMap { createEmptyBuildFile(dotBazelBspDir) }

    private fun copyAspects(dotBazelBspDir: Path): Try<Void> {
        val resourcesAspectsPath = "/" + Constants.ASPECTS_FILE_NAME
        val destinationAspectsPath = dotBazelBspDir.resolve(Constants.ASPECTS_FILE_NAME)

        return copyFileFromResources(resourcesAspectsPath, destinationAspectsPath)
    }

    private fun createEmptyBuildFile(dotBazelBspDir: Path): Try<Void> {
        val destinationBuildFilePath = dotBazelBspDir.resolve(Constants.BUILD_FILE_NAME)
        val destinationWorkspaceFilePath = dotBazelBspDir.resolve(Constants.WORKSPACE_FILE_NAME)
        return Try.run {
            destinationBuildFilePath.toFile().createNewFile()
            destinationWorkspaceFilePath.toFile().createNewFile()
        }
    }

    private fun copyFileFromResources(resourcesPath: String, destinationPath: Path): Try<Void> =
        Try.withResources { BazelBspEnvironmentCreator::class.java.getResourceAsStream(resourcesPath) }
            .of { copyFile(it, destinationPath) }
            .flatMap { it }

    private fun copyFile(inputStream: InputStream, destinationPath: Path): Try<Void> =
        Try.run { Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING) }

    protected fun createDotBsp(discoveryDetails: BspConnectionDetails): Try<Void> =
        createDir(projectRootDir, Constants.DOT_BSP_DIR_NAME)
            .flatMap { createBspDiscoveryDetailsFile(it, discoveryDetails) }

    protected fun createDir(rootDir: Path, name: String): Try<Path> {
        val dir = rootDir.resolve(name)

        return Try.of { Files.createDirectories(dir) }
    }

    private fun createBspDiscoveryDetailsFile(
        dotBspDir: Path,
        discoveryDetails: BspConnectionDetails
    ): Try<Void> {
        val destinationBspDiscoveryFilePath = dotBspDir.resolve(Constants.BAZELBSP_JSON_FILE_NAME)
        return writeJsonToFile(destinationBspDiscoveryFilePath, discoveryDetails)
    }

    protected fun <T> writeJsonToFile(destinationPath: Path, data: T): Try<Void> {
        val fileContent = GsonBuilder().setPrettyPrinting().create().toJson(data)
        return writeStringToFile(destinationPath, fileContent)
    }

    protected fun writeStringToFile(destinationPath: Path, string: String): Try<Void> =
        Try.run { Files.writeString(destinationPath, string) }

}
