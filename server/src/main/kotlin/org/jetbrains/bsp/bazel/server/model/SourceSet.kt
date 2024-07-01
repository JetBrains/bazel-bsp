package org.jetbrains.bsp.bazel.server.model

import java.net.URI

data class SourceSet(
    val sources: Set<URI>,
    val generatedSources: Set<URI>,
    val sourceRoots: Set<URI>
)
