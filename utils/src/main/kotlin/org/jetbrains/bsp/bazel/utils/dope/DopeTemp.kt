package org.jetbrains.bsp.bazel.utils.dope

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Trys are dope, exceptions are not... Soooo use `DopeTemp` to create temp files / dirs to be dope!
 */
@Deprecated("dont use it")
object DopeTemp {

    /**
     * Creates a temp file, allows to edit writable flag.
     * Sets `.deleteOnExit()`.
     *
     * @param rawPath - path to the place where file should be created
     * @param writable - flag set using `setWritable`
     *
     * @return Path to the created temp file
     */
    fun createTempFile(rawPath: String, writable: Boolean = true): Path {
        val path = Path(rawPath)
        val dirs = createTempDirs(path.parent)
        val tempFile = createTempFileInDirs(dirs, path.fileName)

        tempFile.deleteOnExit()
        tempFile.setWritable(writable)

        return tempFile.toPath()
    }

    /**
     * Creates a temp path - it means that file nor directories don't exist.
     *
     * @param rawPath - path which should be used to create a temp path
     * @return temp path
     */
    fun createTempPath(rawPath: String): Path {
        val path = Path(rawPath)
        val dirs = createTempDirs(path.parent)
        val tempFile = createTempFileInDirs(dirs, path.fileName)

        tempFile.delete()
        tempFile.deleteOnExit()
        deleteAllDirs(dirs)


        return tempFile.toPath()
    }

    private fun createTempDirs(path: Path?): List<Path>? =
        path?.let { createTempDirsNotNull(it) }

    private fun createTempDirsNotNull(path: Path): List<Path>? {
        val pathList = path.toList().map { it.toString() }
        val firstDir = pathList.firstOrNull()
        val subdirectories = pathList.drop(1)

        return createFirstDirAndSubdirectories(firstDir, subdirectories)
    }

    private fun createFirstDirAndSubdirectories(firstDir: String?, subdirectories: List<String>): List<Path>? =
        firstDir?.let { Files.createTempDirectory(it) }
            ?.let { createSubdirectories(it, subdirectories) }

    private fun createSubdirectories(firstDir: Path, subdirectories: List<String>): List<Path> =
        subdirectories.runningFold(firstDir, Files::createTempDirectory)

    private fun createTempFileInDirs(dirs: List<Path>?, fileNamePath: Path): File {
        val dir = dirs?.lastOrNull()

        return createTempFileInDir(dir, fileNamePath)
    }

    private fun createTempFileInDir(dir: Path?, fileNamePath: Path): File {
        val fileName = calculateFileName(fileNamePath)
        val extension = calculateExtension(fileNamePath)
        val dirFile = dir?.toFile()

        return File.createTempFile(fileName, extension, dirFile)
    }

    // TODO we can do it in more kotlin way - https://youtrack.jetbrains.com/issue/BAZEL-58
    private fun calculateFileName(fileNamePath: Path): String =
        fileNamePath.toString().substringBeforeLast(".")

    // TODO we can do it in more kotlin way - https://youtrack.jetbrains.com/issue/BAZEL-58
    private fun calculateExtension(fileNamePath: Path): String =
        ".${fileNamePath.toString().substringAfterLast('.', "")}"

    private fun deleteAllDirs(dirs: List<Path>?) =
        dirs?.reversed()
            ?.map { it.toFile() }
            ?.forEach {
                it.delete()
                it.deleteOnExit()
            }
}
