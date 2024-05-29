package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.MavenDependencyModule
import ch.epfl.scala.bsp4j.MavenDependencyModuleArtifact
import org.jetbrains.bsp.bazel.server.model.Label
import org.jetbrains.bsp.bazel.server.model.Library
import org.jetbrains.bsp.bazel.server.model.Module
import org.jetbrains.bsp.bazel.server.model.Project
import java.util.*

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
        /* For example:
         * @@rules_jvm_external~override~maven~maven//:org_apache_commons_commons_lang3
         * @maven//:org_scala_lang_scala_library
         **/
        val orgStart = lib.label.value.split("//:").lastOrNull()?.split('_')?.firstOrNull() ?: "org"
        // Matches the Maven group (organization), artifact, and version in the Bazel dependency
        // string such as .../execroot/monorepo/bazel-out/k8-fastbuild/bin/external/maven/com/google/guava/guava/31.1-jre/processed_guava-31.1-jre.jar
        // bazel-out/k8-fastbuild/bin/external/rules_jvm_external~~maven~name/v1/https/repo1.maven.org/maven2/com/google/auto/service/auto-service-annotations/1.1.1/header_auto-service-annotations-1.1.1.jar
        val regexPattern = """.*/($orgStart/.+)/([^/]+)/([^/]+)/[^/]+.jar""".toRegex()
        val dependencyPath = lib.outputs.firstOrNull()?.toString()
        // Find matches in the dependency path
        if (dependencyPath != null) {
            val matchResult = regexPattern.find(dependencyPath)
            // If a match is found, group values are extracted; otherwise, null is returned
            return matchResult?.let {
                val (organization, artifact, version) = it.destructured
                MavenDependencyModule(organization.replace("/", "."), artifact, version, jars + sourceJars)
            }
        } else {
            return null
        }
    }


    fun allModuleDependencies(project: Project, module: Module): HashSet<Library> {
        val toResolve = mutableListOf<Label>()
        toResolve.addAll(module.directDependencies)
        val accumulator = HashSet<Library>()
        while (toResolve.isNotEmpty()) {
            val lib = project.libraries[toResolve.removeLast()]
            if (lib != null && !accumulator.contains(lib)) {
                accumulator.add(lib)
                toResolve.addAll(lib.dependencies)
            }
        }
        return accumulator
    }
}
