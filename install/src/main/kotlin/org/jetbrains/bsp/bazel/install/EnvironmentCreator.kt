package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import com.google.gson.GsonBuilder
import org.jetbrains.bsp.bazel.commons.Constants
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.copyTo

abstract class EnvironmentCreator(private val projectRootDir: Path) {

    abstract fun create()

    protected fun createDotBazelBsp(): Path {
        removeOldDotBazelBspIfExists()

        val bazelBspDir = createDir(projectRootDir, Constants.DOT_BAZELBSP_DIR_NAME)
        createDotBazelBspFiles(bazelBspDir)
        return bazelBspDir
    }

    private fun removeOldDotBazelBspIfExists() {
        val oldBazelBspDir = projectRootDir.resolve(Constants.DOT_BAZELBSP_DIR_NAME)
        oldBazelBspDir.toFile().deleteRecursively()
    }

    private fun createDotBazelBspFiles(dotBazelBspDir: Path) {
        copyAspects(dotBazelBspDir)
        createEmptyBuildFile(dotBazelBspDir)
    }

    private fun copyAspects(dotBazelBspDir: Path) {
        val destinationAspectsPath = dotBazelBspDir.resolve(Constants.ASPECTS_ROOT)
        copyAspectsFromResources("/" + Constants.ASPECTS_ROOT, destinationAspectsPath)
    }

    private fun createEmptyBuildFile(dotBazelBspDir: Path) {
        val destinationBuildFilePath = dotBazelBspDir.resolve(Constants.BUILD_FILE_NAME)
        val destinationWorkspaceFilePath = dotBazelBspDir.resolve(Constants.WORKSPACE_FILE_NAME)
            destinationBuildFilePath.toFile().createNewFile()
            destinationWorkspaceFilePath.toFile().createNewFile()
    }

    private fun copyAspectsFromResources(aspectsJarPath: String, destinationPath: Path) =
        javaClass.getResource(aspectsJarPath)?.let {
            val fileSystem = FileSystems.newFileSystem(it.toURI(), emptyMap<String, String>())
            copyFileTree(fileSystem.getPath(aspectsJarPath), destinationPath)
            fileSystem.close()
        } ?: error("Missing aspects resource")


    private fun copyFileTree(source: Path, destination: Path): Unit =
        Files.walk(source).forEach { copyUsingRelativePath(source, it, destination) }

    private fun copyUsingRelativePath(sourcePrefix: Path, source: Path, destination: Path) {
        val sourceRelativePath = sourcePrefix.relativize(source).toString()
        val destinationAbsolutePath = Paths.get(destination.toString(), sourceRelativePath)
        source.copyTo(destinationAbsolutePath, overwrite = true)
    }

    protected fun createDotBsp(discoveryDetails: BspConnectionDetails) {
        val dir = createDir(projectRootDir, Constants.DOT_BSP_DIR_NAME)
        createBspDiscoveryDetailsFile(dir, discoveryDetails)
    }

    private fun createDir(rootDir: Path, name: String): Path {
        val dir = rootDir.resolve(name)

        return Files.createDirectories(dir)
    }

    private fun createBspDiscoveryDetailsFile(
        dotBspDir: Path,
        discoveryDetails: BspConnectionDetails,
    ) {
        val destinationBspDiscoveryFilePath = dotBspDir.resolve(Constants.BAZELBSP_JSON_FILE_NAME)
        writeJsonToFile(destinationBspDiscoveryFilePath, discoveryDetails)
    }

    private fun <T> writeJsonToFile(destinationPath: Path, data: T) {
        val fileContent = GsonBuilder().setPrettyPrinting().create().toJson(data)
        Files.writeString(destinationPath, fileContent)
    }

}
