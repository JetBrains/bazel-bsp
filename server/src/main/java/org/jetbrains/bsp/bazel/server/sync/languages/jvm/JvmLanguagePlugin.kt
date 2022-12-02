package org.jetbrains.bsp.bazel.server.sync.languages.jvm

import org.jetbrains.bsp.bazel.server.sync.ModuleGraph
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.model.Module
import java.net.URI

class JvmLanguagePlugin : LanguagePlugin<LanguageData>() {

    override fun postProcessModules(modules: List<Module>): List<Module> {
        return ModuleGraph.transformBottomUp(modules) { node ->
            val javaModule = node.module.javaModule ?: return@transformBottomUp node.module
            val newIdeClasspath = removeTransitiveDependenciesFromClasspath(node, javaModule) { it.ideClasspath }
            val newSourceClasspath = removeTransitiveDependenciesFromClasspath(node, javaModule) { it.sourcesClasspath }
            val newJavaModule = javaModule.copy(ideClasspath = newIdeClasspath, sourcesClasspath = newSourceClasspath)
            node.module.withJavaModule(newJavaModule)
        }
    }

    private fun removeTransitiveDependenciesFromClasspath(
            node: ModuleGraph.ModuleNode,
            javaModule: JavaModule,
            extractClasspath: (JavaModule) -> List<URI>
    ): List<URI> {
        val childrenClasspath = node.children.mapNotNull { it.javaModule?.let(extractClasspath) }.flatten().toSet()
        return extractClasspath(javaModule).filterNot { childrenClasspath.contains(it) }
    }
}
