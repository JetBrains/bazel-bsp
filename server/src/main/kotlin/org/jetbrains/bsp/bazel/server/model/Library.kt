package org.jetbrains.bsp.bazel.server.sync.model

import java.net.URI

data class Library(
        val label: Label,
        val outputs: Set<URI>,
        val sources: Set<URI>,
        val dependencies: List<Label>,
        val interfaceJars: Set<URI> = emptySet(),
)
