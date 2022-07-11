package org.jetbrains.bsp.bazel.server.bloop

import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import java.nio.file.Path
import java.nio.file.Paths

class SourceSetRewriter(private val ignoredSources: Set<Path>) {
    fun rewrite(input: SourceSet): SourceSet {
        if (input.sources.size == 1) {
            val singleSource: Path = Paths.get(input.sources.first())
            if (ignoredSources.any(singleSource::endsWith)) {
                return EMPTY_SOURCE_SET
            }
        }
        return input
    }

    companion object {
        private val EMPTY_SOURCE_SET = SourceSet(HashSet(), HashSet())
    }
}
