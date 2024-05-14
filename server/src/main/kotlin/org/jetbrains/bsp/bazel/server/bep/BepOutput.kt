package org.jetbrains.bsp.bazel.server.bep

import org.jetbrains.bsp.bazel.server.model.Label
import java.nio.file.Path

class BepOutput(
    private val outputGroups: Map<String, Set<String>> = emptyMap(),
    private val textProtoFileSets: Map<String, TextProtoDepSet> = emptyMap(),
    private val rootTargets: Set<Label> = emptySet()
) {
    fun rootTargets(): Set<Label> {
        return rootTargets
    }

    fun filesByOutputGroupNameTransitive(outputGroup: String): Set<Path> {
        val rootIds = outputGroups.getOrDefault(outputGroup, emptySet())
        if (rootIds.isEmpty()) {
            return emptySet()
        }
        val result = HashSet<Path>(rootIds.size)
        val toVisit = ArrayDeque<String>(rootIds.size)
        val visited = HashSet<String>(rootIds)
        while (!toVisit.isEmpty()) {
            val fileSetId = toVisit.removeFirst()
            val fileSet = textProtoFileSets[fileSetId]
            result.addAll(fileSet!!.files)
            val children = fileSet.children
            children.asSequence()
                .filter { child: String -> !visited.contains(child) }
                .forEach { e: String -> visited.add(e); toVisit.add(e) }
        }
        return result
    }
}
