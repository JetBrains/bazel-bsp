package org.jetbrains.bsp.bazel.server.bloop

import bloop.config.Config.SourcesGlobs
import com.google.common.io.Files
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toOption
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toScalaList
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import scala.Option
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.max
import scala.collection.immutable.List as SList

object ReGlobber {
    fun reGlob(baseDirectory: URI, sourceSet: SourceSet): ReGlobbed {
        val sourceGlobs = reGlobImpl(baseDirectory, sourceSet.sources).toOption()
        val sources =
            if (sourceGlobs.isDefined) emptyList<Path>() else sourceSet.sources.map(Paths::get)
                .toScalaList()
        return ReGlobbed(sourceGlobs, sources)
    }

    private fun commonPrefix(basePath: Path, paths: List<Path>): Path {
        if (paths.isEmpty()) {
            return basePath
        }

        val maxPrefixLen = paths.minOf { it.nameCount } - 1
        val prefix = mutableListOf<String>()

        for (n in 0 until maxPrefixLen) {
            val prefixPart = paths[0].getName(n)
            if (paths.all { it.getName(n) == prefixPart }) {
                prefix.add(prefixPart.toString())
            } else {
                break
            }
        }

        return if (prefix.isEmpty()) basePath
        else basePath.resolve(Paths.get(prefix.first(), *prefix.drop(1).toTypedArray()))
    }

    private fun makeRelative(basePath: Path, paths: List<Path>): List<Path> =
        paths
            .asSequence()
            .map(basePath::relativize)
            .filter { !it.startsWith("..") }
            .toList()

    private fun reGlobImpl(baseDirectory: URI, sources: Set<URI>): SList<SourcesGlobs>? {
        var basePath = Paths.get(baseDirectory)
        val sourcePaths = sources.map(Paths::get)
        var relativeLevels = 0

        var relativePaths = makeRelative(basePath, sourcePaths)
        val newPrefix = commonPrefix(basePath, relativePaths)
        if (newPrefix != basePath) {
            basePath = newPrefix
            relativePaths = makeRelative(newPrefix, sourcePaths)
        }

        val extensions = relativePaths
            .mapNotNull {
                relativeLevels = max(relativeLevels, it.nameCount)
                Files.getFileExtension(it.toString()).takeUnless(String::isEmpty)
            }.distinct()
            .toList()

        return if (relativeLevels == 0) {
            null
        } else {
            val walkDepth: Option<Any>
            val globPrefix: String
            if (relativeLevels == 1) {
                walkDepth = relativeLevels.toOption()
                globPrefix = "glob:*."
            } else {
                walkDepth = Option.empty()
                globPrefix = "glob:**."
            }
            val includes = extensions.map { ext: String -> globPrefix + ext }
            val singleGlob = SourcesGlobs(basePath, walkDepth, includes.toScalaList(), emptyList())
            listOf(singleGlob).toScalaList()
        }
    }

    class ReGlobbed(
        val globs: Option<SList<SourcesGlobs>>,
        val sources: SList<Path>
    )
}
