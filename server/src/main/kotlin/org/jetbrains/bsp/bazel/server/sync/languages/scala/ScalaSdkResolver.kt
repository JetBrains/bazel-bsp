package org.jetbrains.bsp.bazel.server.sync.languages.scala

import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.math.min

class ScalaSdkResolver(private val bazelPathsResolver: BazelPathsResolver) {

    fun resolve(targets: Sequence<BspTargetInfo.TargetInfo>): ScalaSdk? =
        targets
            .mapNotNull(::resolveSdk)
            .distinct()
            .sortedWith(SCALA_VERSION_COMPARATOR)
            .lastOrNull()

    private fun resolveSdk(targetInfo: BspTargetInfo.TargetInfo): ScalaSdk? {
        if (!targetInfo.hasScalaToolchainInfo()) {
            return null
        }
        val scalaToolchain = targetInfo.scalaToolchainInfo
        val compilerJars =
            bazelPathsResolver.resolvePaths(scalaToolchain.compilerClasspathList).sorted()
        val maybeVersions = compilerJars.mapNotNull(::extractVersion)
        if (maybeVersions.none()) {
            return null
        }
        val version = maybeVersions.distinct().maxOf { it }
        val binaryVersion = toBinaryVersion(version)
        return ScalaSdk(
            "org.scala-lang",
            version,
            binaryVersion,
            compilerJars.map(bazelPathsResolver::resolveUri)
        )
    }

    private fun extractVersion(path: Path): String? {
        val name = path.fileName.toString()
        val matcher = VERSION_PATTERN.matcher(name)
        return if (matcher.matches()) matcher.group(1) else null
    }

    private fun toBinaryVersion(version: String): String =
        version.split("\\.".toRegex()).toTypedArray().take(2).joinToString(".")

    companion object {
        private val SCALA_VERSION_COMPARATOR = Comparator { a: ScalaSdk, b: ScalaSdk ->
            val aParts = a.version.split("\\.".toRegex()).toTypedArray()
            val bParts = b.version.split("\\.".toRegex()).toTypedArray()
            var i = 0
            while (i < min(aParts.size, bParts.size)) {
                val result = aParts[i].toInt().compareTo(bParts[i].toInt())
                if (result != 0) return@Comparator result
                i++
            }
            0
        }
        private val VERSION_PATTERN =
            Pattern.compile("(?:processed_)?scala3?-(?:library|compiler|reflect)(?:_3)?-([.\\d]+)\\.jar")
    }
}
