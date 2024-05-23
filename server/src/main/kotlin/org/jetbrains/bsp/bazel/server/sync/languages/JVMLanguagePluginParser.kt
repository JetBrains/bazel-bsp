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
        val sourceRootEndIndex = source.nameCount - sourcePackagePath.nameCount - 1

        return if (!source.parent.endsWith(sourcePackagePath)) source.parent
        else Paths.get("/").resolve(source.subpath(0, sourceRootEndIndex))
    }

    private fun findPackage(source: Path, multipleLines: Boolean): String? = File(source.toUri()).useLines { lines ->
        val packages = lines.mapNotNull {
            PACKAGE_PATTERN.find(it)?.groups?.get(1)?.value
        }
        return if (multipleLines) {
            packages.joinToString(".").takeIf { it.isNotEmpty() }
        } else {
            packages.firstOrNull()
        }
    }
}
