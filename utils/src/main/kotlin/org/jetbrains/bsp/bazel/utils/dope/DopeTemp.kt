package org.jetbrains.bsp.bazel.utils.dope

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

object DopeTemp {

    fun createTempFile(rawPath: String, writable: Boolean = true): Path {
        val tempFile = createTmpFileWithDirs(rawPath)
        tempFile.deleteOnExit()
        tempFile.setWritable(writable)

        return tempFile.toPath()
    }

    fun createTempPath(rawPath: String): Path {
        val tempFile = createTmpFileWithDirs(rawPath)
        tempFile.delete()
        tempFile.deleteOnExit()

        return tempFile.toPath()
    }

    private fun createTmpFileWithDirs(rawPath: String): File {
        val path = Path(rawPath)
        val dirs = createTempDirs(path)

        return createTempFile(dirs, path)
    }

    private fun createTempDirs(path: Path): Path? {
        val pathList = path.toList()
        val firstDir = getFirstDirAndMapToString(pathList)
        val subdirectories = removeFirstDirAndFileFromPathListAndMapToString(pathList)

        return createFirstDirAndSubdirectories(firstDir, subdirectories)
    }

    private fun getFirstDirAndMapToString(pathList: List<Path>): String? =
        pathList.firstOrNull()?.let(Path::toString)

    private fun removeFirstDirAndFileFromPathListAndMapToString(pathList: List<Path>): List<String> =
        pathList.drop(1).dropLast(1).map(Path::toString)

    private fun createFirstDirAndSubdirectories(firstDir: String?, subdirectories: List<String>): Path? =
        firstDir?.let(Files::createTempDirectory)
            ?.let { createSubdirectories(it, subdirectories) }

    private fun createSubdirectories(firstDir: Path, subdirectories: List<String>): Path =
        subdirectories.fold(firstDir, Files::createTempDirectory)

    private fun createTempFile(dir: Path?, path: Path): File {
        val fileName = calculateFileName(path)
        val extension = calculateExtension(path)
        val dirFile = dir?.toFile()

        return File.createTempFile(fileName, extension, dirFile)
    }

    private fun calculateFileName(path: Path): String =
        path.fileName.nameWithoutExtension

    private fun calculateExtension(path: Path): String =
        ".${path.extension}"
}
