package org.jetbrains.bsp.bazel.server.sync

import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.model.Tag

class TargetKindResolver {
    fun resolveTags(targetInfo: BspTargetInfo.TargetInfo): Set<Tag> {
        if (targetInfo.kind == "resources_union" || targetInfo.kind == "java_import") {
            return LIBRARY
        }
        val tag = ruleSuffixToTargetType.filterKeys {
            targetInfo.kind.endsWith("_$it")
        }.values.firstOrNull() ?: NO_IDE
        return if (targetInfo.tagsList.contains("no-ide")) {
            tag + Tag.NO_IDE
        } else if (targetInfo.tagsList.contains("manual")) {
            tag + Tag.MANUAL
        } else tag
    }

    companion object {
        private val LIBRARY: Set<Tag> = hashSetOf(Tag.LIBRARY)
        private val ruleSuffixToTargetType = java.util.Map.of(
            "library", LIBRARY,
            "binary", hashSetOf(Tag.APPLICATION),
            "test", hashSetOf(Tag.TEST)
        )
        private val NO_IDE: Set<Tag> = hashSetOf(Tag.NO_IDE)
    }
}
