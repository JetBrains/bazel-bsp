package org.jetbrains.bsp.bazel.server.sync

import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Module

class ModuleGraph(modules: List<Module>) {

    companion object {
        fun transformBottomUp(modules: List<Module>, transformer: (ModuleNode) -> Module): List<Module> {
            return ModuleGraph(modules).transformBottomUp(transformer)
        }
    }

    data class ModuleNode(val module: Module, val children: List<Module>)

    private val moduleMap = modules.associateBy { it.label }.toMutableMap()
    private val visited = mutableSetOf<Label>()
    private val toVisit = ArrayDeque(moduleMap.keys)

   private fun transformBottomUp(transformer: (ModuleNode) -> Module): List<Module> {
       while (toVisit.isNotEmpty()) {
           val top = toVisit.last()
           if (visited.contains(top)) {
               toVisit.removeLast()
           } else {
               val module = moduleMap[top]!!
               val unvisitedChildren = module.directDependencies
                       .filter { !visited.contains(it) && moduleMap.containsKey(it) }

               if (unvisitedChildren.isNotEmpty()) {
                   toVisit.addAll(unvisitedChildren)
               } else {
                   val node = ModuleNode(module, module.directDependencies.mapNotNull { moduleMap[it] })
                   val transformedModule = transformer(node)
                   if (transformedModule.label != module.label) {
                       error("Cannot change label of module during graph transformation")
                   }
                   moduleMap[top] = transformedModule
                   toVisit.removeLast()
                   visited.add(top)
               }
           }
       }

       return moduleMap.values.toList()
   }

}
