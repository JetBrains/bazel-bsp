package org.jetbrains.bsp.bazel.server.sync.languages

import org.jetbrains.bsp.bazel.server.sync.languages.jvm.SourceRootGuesser
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths

object JVMLanguagePluginParser {

    private val PACKAGE_PATTERN = Regex("^\\s*package\\s+([\\p{L}0-9_.]+)")
    private val ONE_BYTE_CHARSET = Charset.forName("ISO-8859-1")

    fun calculateJVMSourceRoot(source: Path, multipleLines: Boolean = false): Path {
        val sourcePackage =
            findPackage(source, multipleLines) ?: return SourceRootGuesser.getSourcesRoot(source)
        val sourcePackagePath = Paths.get(sourcePackage.replace(".", "/"))
        val sourceRootEndIndex = source.nameCount - sourcePackagePath.nameCount - 1
        return if (!source.parent.endsWith(sourcePackagePath)) source.parent
        else Paths.get("/").resolve(source.subpath(0, sourceRootEndIndex))
    }

    private fun findPackage(
        source: Path,
        multipleLines: Boolean,
    ): String? = File(source.toUri()).useLines(ONE_BYTE_CHARSET) { lines ->
        // Not using UTF-8 charset because it is slower to decode
        val packages = lines.mapNotNull { line ->
            if (!line.trimStart().startsWith("package")) return@mapNotNull null
            val decodedLine = line.toByteArray(ONE_BYTE_CHARSET).decodeToString()
            PACKAGE_PATTERN.find(decodedLine)?.groups?.get(1)?.value
        }
        return if (multipleLines) {
            packages.joinToString(".").takeIf { it.isNotEmpty() }
        } else {
            packages.firstOrNull()
        }
    }
}
