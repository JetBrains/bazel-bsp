package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import com.google.gson.GsonBuilder
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.Constants
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class EnvironmentCreator(private val projectRootDir: Path,
                         private val discoveryDetails: BspConnectionDetails) {

    fun create(): Try<Void> = createDotBazelBsp().flatMap { createDotBsp() }

    private fun createDotBazelBsp(): Try<Void> =
            createDir(projectRootDir, Constants.DOT_BAZELBSP_DIR_NAME).flatMap(::createDotBazelBspFiles)

    private fun createDotBazelBspFiles(dotBazelBspDir: Path): Try<Void> =
            copyAspects(dotBazelBspDir)
                    .flatMap { createEmptyBuildFile(dotBazelBspDir) }
                    .flatMap { copyDefaultProjectViewFilePath(dotBazelBspDir) }

    private fun copyAspects(dotBazelBspDir: Path): Try<Void> {
        val resourcesAspectsPath = "/" + Constants.ASPECTS_FILE_NAME
        val destinationAspectsPath = dotBazelBspDir.resolve(Constants.ASPECTS_FILE_NAME)

        return copyFileFromResources(resourcesAspectsPath, destinationAspectsPath)
    }

    private fun createEmptyBuildFile(dotBazelBspDir: Path): Try<Void> {
        val destinationBuildFilePath = dotBazelBspDir.resolve(Constants.BUILD_FILE_NAME)

        return Try.run { destinationBuildFilePath.toFile().createNewFile() }
    }

    private fun copyDefaultProjectViewFilePath(dotBazelBspDir: Path): Try<Void> {
        val resourcesProjectViewFilePath = "/default-projectview.bazelproject"
        val destinationProjectViewFilePath = dotBazelBspDir.resolve("default-projectview.bazelproject")

        return copyFileFromResources(resourcesProjectViewFilePath, destinationProjectViewFilePath)
    }

    private fun copyFileFromResources(resourcesPath: String, destinationPath: Path): Try<Void> =
            Try.withResources { EnvironmentCreator::class.java.getResourceAsStream(resourcesPath) }
                    .of { copyFile(it, destinationPath) }
                    .flatMap { it }

    private fun copyFile(inputStream: InputStream, destinationPath: Path): Try<Void> =
            Try.run { Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING) }

    private fun createDotBsp(): Try<Void> =
            createDir(projectRootDir, Constants.DOT_BSP_DIR_NAME)
                    .flatMap(::createBspDiscoveryDetailsFile)

    private fun createDir(rootDir: Path, name: String): Try<Path> {
        val dir = rootDir.resolve(name)

        return Try.of { Files.createDirectories(dir) }
    }

    private fun createBspDiscoveryDetailsFile(dotBspDir: Path): Try<Void> {
        val destinationBspDiscoveryFilePath = dotBspDir.resolve(Constants.BAZELBSP_JSON_FILE_NAME)
        val fileContent = GsonBuilder().setPrettyPrinting().create().toJson(discoveryDetails)

        return writeStringToFile(destinationBspDiscoveryFilePath, fileContent)
    }

    private fun writeStringToFile(destinationPath: Path, string: String): Try<Void> =
            Try.run { Files.writeString(destinationPath, string) }
}
