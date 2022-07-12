package org.jetbrains.bsp.bazel.server.bsp.utils

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths

object SourceRootGuesser {
    fun getSourcesRoot(sourcePath: Path): Path {
        val fs = FileSystems.getDefault()
        val sourceRootPattern = fs.getPathMatcher(
            "glob:**/"
                    + "{main,test,tests,src,3rdparty,3rd_party,thirdparty,third_party}/"
                    + "{*resources,scala,java,kotlin,jvm,proto,python,protobuf,py}"
        )
        val defaultTestRootPattern = fs.getPathMatcher("glob:**/{test,tests}")
        val sourceRootGuess = sequenceOf(sourceRootPattern, defaultTestRootPattern)
            .mapNotNull { pattern: PathMatcher -> approximateSourceRoot(sourcePath, pattern) }
            .firstOrNull()
        return (sourceRootGuess ?: sourcePath.parent).toAbsolutePath()
    }

    fun getSourcesRoot(sourceUri: URI): String =
        getSourcesRoot(Paths.get(sourceUri)).toString()

    private fun approximateSourceRoot(dir: Path, matcher: PathMatcher): Path? {
        var guess: Path? = dir
        while (guess != null) {
            guess = if (matcher.matches(guess)) {
                return guess
            } else {
                guess.parent
            }
        }
        return null
    }
}
