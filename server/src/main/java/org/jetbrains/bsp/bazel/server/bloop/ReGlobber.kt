package org.jetbrains.bsp.bazel.server.bloop

import bloop.config.Config.SourcesGlobs
import com.google.common.io.Files
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toOption
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toScalaList
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import scala.Option
import scala.collection.immutable.List
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.max

object ReGlobber {
    fun reGlob(baseDirectory: URI, sourceSet: SourceSet): ReGlobbed {
        val sourceGlobs = reGlobImpl(baseDirectory, sourceSet.sources).toOption()
        val sources =
            if (sourceGlobs.isDefined) emptyList<Path>() else sourceSet.sources.map(Paths::get)
                .toScalaList()
        return ReGlobbed(sourceGlobs, sources)
    }

    private fun reGlobImpl(baseDirectory: URI, sources: Set<URI>): List<SourcesGlobs>? {
        val basePath = Paths.get(baseDirectory)
        val sourcePaths = sources.map(Paths::get)
        var relativeLevels = 0
        val extensions = sourcePaths
            .asSequence()
            .map(basePath::relativize)
            .filter { !it.startsWith("..") }
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
        val globs: Option<List<SourcesGlobs>>,
        val sources: List<Path>
    )
}
