package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import com.google.gson.GsonBuilder
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.Constants
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.copyTo

abstract class EnvironmentCreator(private val projectRootDir: Path) {

    abstract fun create(): Try<Void>

    protected fun createDotBazelBsp(): Try<Path> {
        val bazelBspDir = createDir(projectRootDir, Constants.DOT_BAZELBSP_DIR_NAME)
        return bazelBspDir.flatMap(::createDotBazelBspFiles).flatMap { bazelBspDir }
    }

    private fun createDotBazelBspFiles(dotBazelBspDir: Path): Try<Void> =
        Try.of { copyAspects(dotBazelBspDir) }.flatMap { createEmptyBuildFile(dotBazelBspDir) }

    private fun copyAspects(dotBazelBspDir: Path) {
        val destinationAspectsPath = dotBazelBspDir.resolve(Constants.ASPECTS_ROOT)
        copyAspectsFromResources("/" + Constants.ASPECTS_ROOT, destinationAspectsPath)
    }

    private fun createEmptyBuildFile(dotBazelBspDir: Path): Try<Void> {
        val destinationBuildFilePath = dotBazelBspDir.resolve(Constants.BUILD_FILE_NAME)
        val destinationWorkspaceFilePath = dotBazelBspDir.resolve(Constants.WORKSPACE_FILE_NAME)
        return Try.run {
            destinationBuildFilePath.toFile().createNewFile()
            destinationWorkspaceFilePath.toFile().createNewFile()
        }
    }

    private fun copyAspectsFromResources(aspectsJarPath: String, destinationPath: Path) =
        javaClass.getResource(aspectsJarPath)?.let {
            val fileSystem = FileSystems.newFileSystem(it.toURI(), emptyMap<String, String>())
            copyFileTree(fileSystem.getPath(aspectsJarPath), destinationPath)
        } ?: error("Missing aspects resource")


    private fun copyFileTree(source: Path, destination: Path): Unit =
        Files.walk(source).forEach { copyUsingRelativePath(source, it, destination) }

    private fun copyUsingRelativePath(sourcePrefix: Path, source: Path, destination: Path) {
        val sourceRelativePath = sourcePrefix.relativize(source).toString()
        val destinationAbsolutePath = Paths.get(destination.toString(), sourceRelativePath)
        source.copyTo(destinationAbsolutePath, overwrite = true)
    }

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
