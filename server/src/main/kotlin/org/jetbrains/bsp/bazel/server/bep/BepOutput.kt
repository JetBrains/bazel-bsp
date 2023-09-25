package org.jetbrains.bsp.bazel.server.bep

import com.google.common.collect.Queues
import java.net.URI

class BepOutput(
    private val outputGroups: Map<String, Set<String>> = emptyMap(),
    private val textProtoFileSets: Map<String, TextProtoDepSet> = emptyMap(),
    private val rootTargets: Set<String> = emptySet()
) {
    fun rootTargets(): Set<String> {
        return rootTargets
    }

    fun filesByOutputGroupNameTransitive(outputGroup: String): Set<URI> {
        val rootIds = outputGroups.getOrDefault(outputGroup, emptySet())
        if (rootIds.isEmpty()) {
            return emptySet()
        }
        val result = HashSet<URI>(rootIds.size)
        val toVisit = Queues.newArrayDeque(rootIds)
        val visited = HashSet<String>(rootIds.size)
        while (!toVisit.isEmpty()) {
            val fileSetId = toVisit.remove()
            val fileSet = textProtoFileSets[fileSetId]
            result.addAll(fileSet!!.files)
            visited.add(fileSetId)
            val children = fileSet.children
            children.filter { child: String -> !visited.contains(child) }
                .forEach { e: String -> toVisit.add(e) }
        }
        return result
    }
}
