package org.jetbrains.bsp.bazel.server.sync.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.bsp.bazel.commons.Format
import java.net.URI

/** Project is the internal model of the project. Bazel/Aspect Model -> Project -> BSP Model  */
data class Project(
    @param:JsonProperty("workspaceRoot") val workspaceRoot: URI,
    @param:JsonProperty("modules") val modules: List<Module>,
    @param:JsonProperty("sourceToTarget") val sourceToTarget: Map<URI, Label>
) {
    @JsonIgnore
    private val moduleMap: Map<Label, Module> = modules.associateBy(Module::label)

    fun findModule(label: Label): Module? =
        moduleMap[label]

    fun findTargetBySource(documentUri: URI): Label? =
        sourceToTarget[documentUri]

    override fun toString(): String {
        return Format.`object`("Project", Format.entry("workspaceRoot", workspaceRoot))
    }
}
