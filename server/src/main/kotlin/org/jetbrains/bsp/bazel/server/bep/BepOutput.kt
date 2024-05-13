package org.jetbrains.bsp.bazel.server.bep

import java.nio.file.Path

class BepOutput(
    private val outputGroups: Map<String, Set<String>> = emptyMap(),
    private val textProtoFileSets: Map<String, TextProtoDepSet> = emptyMap(),
    val rootTargets: Set<String> = emptySet()
) {
    // TODO could be a lazy stream/flow?
    fun filesByOutputGroupNameTransitive(outputGroup: String): Set<Path> {
        val rootIds = outputGroups.getOrDefault(outputGroup, emptySet())
        return filesRec(rootIds, HashSet(), emptyList()).toSet()
    }

    private tailrec fun filesRec(toVisit: Collection<String>, visited: Set<String>, acc: Collection<Path>): Collection<Path> {
        if (toVisit.isEmpty()) {
            return acc
        } else {
            val filesets = toVisit.map { textProtoFileSets[it] }
            val visitNext = filesets.flatMap { it?.children.orEmpty() }.filterNot { visited.contains(it) }
            val result = acc.plus(filesets.flatMap { it?.files.orEmpty() })
            return filesRec(visitNext, visited.plus(toVisit), result)
        }
    }
}
