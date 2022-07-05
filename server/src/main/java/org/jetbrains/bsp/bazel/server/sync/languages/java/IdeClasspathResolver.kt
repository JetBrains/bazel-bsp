package org.jetbrains.bsp.bazel.server.sync.languages.java

import java.net.URI

class IdeClasspathResolver(runtimeClasspath: Sequence<URI>, compileClasspath: Sequence<URI>) {
    private val runtimeJars: Set<String>
    private val runtimeMavenJarSuffixes: Set<String>
    private val compileJars: Sequence<String>

    init {
        runtimeJars = runtimeClasspath.map { obj: URI -> obj.toString() }
            .toSet()
        runtimeMavenJarSuffixes = runtimeJars.mapNotNull(::toMavenSuffix).toSet()
        compileJars = compileClasspath.map { obj: URI -> obj.toString() }
    }

    fun resolve(): Sequence<URI> =
        compileJars
            .map(::findRuntimeEquivalent)
            .map(URI::create)

    private fun findRuntimeEquivalent(compileJar: String): String {
        val runtimeJar = compileJar.replace(JAR_PATTERN, ".jar")
        if (runtimeJars.contains(runtimeJar)) {
            return runtimeJar
        }
        val headerSuffix = toMavenSuffix(compileJar)
        val mavenJarSuffix = headerSuffix?.let { s: String ->
            s.replace(
                "/header_([^/]+)\\.jar$".toRegex(),
                "/$1.jar"
            )
        }
        return mavenJarSuffix?.takeIf(runtimeMavenJarSuffixes::contains)?.let { suffix ->
            runtimeJars.find { jar:String -> jar.endsWith(suffix) }
        } ?: compileJar
    }

    private fun toMavenSuffix(uri: String): String? {
        val indicator = "/maven2/"
        val index = uri.lastIndexOf(indicator)
        return if (index < 0) {
            null
        } else {
            uri.substring(index + indicator.length)
        }
    }

    companion object {
        private val JAR_PATTERN = ("-[hi]jar\\.jar$").toRegex()
    }
}
