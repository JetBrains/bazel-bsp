package org.jetbrains.bsp.bazel.server.model

import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelRelease
import java.net.URI

/** Project is the internal model of the project. Bazel/Aspect Model -> Project -> BSP Model  */
data class Project(
    val workspaceRoot: URI,
    val modules: List<Module>,
    val sourceToTarget: Map<URI, Label>,
    val libraries: Map<Label, Library>,
    val invalidTargets: List<Label>,
    val bazelRelease: BazelRelease
) {
    private val moduleMap: Map<Label, Module> = modules.associateBy(Module::label)

    fun findModule(label: Label): Module? =
        moduleMap[label]

//    fun findNonExternalModules(): List<Module> {
//        val rustExternalModules = modules.filter {
//            it.languageData is RustModule &&
//            it.languageData.isExternalModule
//        }
//        return modules - rustExternalModules.toSet()
//    }
}
