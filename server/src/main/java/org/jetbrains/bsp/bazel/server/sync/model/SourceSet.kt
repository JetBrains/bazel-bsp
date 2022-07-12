package org.jetbrains.bsp.bazel.server.sync.model

import org.jetbrains.bsp.bazel.commons.Format
import java.net.URI

data class SourceSet(
    val sources: Set<URI>,
    val sourceRoots: Set<URI>
) {
    override fun toString(): String =
        Format.`object`(
            "SourceSet",
            Format.entry(
                "sources", Format.iterable(sources.stream())
            ),
            Format.entry(
                "sourceRoots", Format.iterable(sourceRoots.stream())
            )
        )
}
