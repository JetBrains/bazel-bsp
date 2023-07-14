package org.jetbrains.bsp.bazel.server.sync.model

import java.net.URI

/** Project is the internal model of the project. Bazel/Aspect Model -> Project -> BSP Model  */
data class Project(
    val workspaceRoot: URI,
    val modules: List<Module>,
    val sourceToTarget: Map<URI, Label>,
    val libraries: Map<String, Library>
) {
    private val moduleMap: Map<Label, Module> = modules.associateBy(Module::label)

    fun findModule(label: Label): Module? =
        moduleMap[label]

    fun findTargetBySource(documentUri: URI): Label? =
        sourceToTarget[documentUri]
}
