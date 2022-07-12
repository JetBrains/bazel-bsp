package org.jetbrains.bsp.bazel.server.sync.languages

import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object JVMLanguagePluginParser {

    private val PACKAGE_PATTERN = Regex("^\\s*package\\s+([\\w.]+)")

    fun calculateJVMSourceRoot(source: Path, multipleLines: Boolean = false): Path {
        val sourcePackage =
            findPackage(source, multipleLines) ?: return SourceRootGuesser.getSourcesRoot(source)
        val sourcePackagePath = Paths.get(sourcePackage.replace(".", "/"))
        return Paths.get("/").resolve(
            source.subpath(0, source.nameCount - sourcePackagePath.nameCount - 1)
        )
    }

    private fun findPackage(source: Path, multipleLines: Boolean): String? {
        val packages = File(source.toUri())
            .useLines(block = ::findPackages)
        return when {
            packages.isEmpty() -> null
            multipleLines -> packages.joinToString(".")
            else -> packages.first()
        }
    }

    private fun findPackages(lines: Sequence<String>): List<String> = lines.mapNotNull {
        PACKAGE_PATTERN.find(it)?.groups?.get(1)?.value
    }.toList()
}
