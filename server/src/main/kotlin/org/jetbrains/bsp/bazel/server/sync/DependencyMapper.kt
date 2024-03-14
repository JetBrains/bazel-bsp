package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.MavenDependencyModule
import ch.epfl.scala.bsp4j.MavenDependencyModuleArtifact
import org.jetbrains.bsp.bazel.server.sync.model.Library
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import java.util.*
import kotlin.collections.HashSet

object DependencyMapper {

    fun extractMavenDependencyInfo(lib: Library): MavenDependencyModule? {
        if (lib.outputs.isEmpty()) return null
        val jars = lib.outputs.map { uri -> uri.toString() }.map {
            MavenDependencyModuleArtifact(it)
        }
        val sourceJars = lib.sources.map { uri -> uri.toString() }.map {
            val artifact = MavenDependencyModuleArtifact(it)
            artifact.classifier = "sources"
            artifact
        }

        // Matches the Maven group (organization), artifact, and version in the Bazel dependency
        // string such as .../execroot/monorepo/bazel-out/k8-fastbuild/bin/external/maven/com/google/guava/guava/31.1-jre/processed_guava-31.1-jre.jar
        val regexPattern = """.*/maven/(.+)/([^/]+)/([^/]+)/[^/]+.jar""".toRegex()
        val dependencyPath = lib.outputs.first().toString()
        // Find matches in the dependency path
        val matchResult = regexPattern.find(dependencyPath)

        // If a match is found, group values are extracted; otherwise, null is returned
        return matchResult?.let {
            val (organization, artifact, version) = it.destructured
            MavenDependencyModule(organization.replace("/", "."), artifact, version, jars + sourceJars)
        }
    }


    fun allModuleDependencies(project: Project, module: Module): HashSet<Library> {
        val toResolve = LinkedList<String>()
        toResolve.addAll(module.directDependencies.map { it.value })
        val accumulator = HashSet<Library>()
        while (toResolve.isNotEmpty()){
            val lib = project.libraries[toResolve.pop()]
            if (lib != null && !accumulator.contains(lib)) {
                accumulator.add(lib)
                toResolve.addAll(lib.dependencies)
            }
        }
        return accumulator
    }
}
