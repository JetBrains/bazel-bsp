package org.jetbrains.bsp.bazel.server.sync.model

import java.net.URI

data class SourceSet(
    val sources: Set<URI>,
    val sourceRoots: Set<URI>
)
