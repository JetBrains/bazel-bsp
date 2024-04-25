package org.jetbrains.bsp.bazel.server.sync.model

import java.net.URI

data class Library(
        val label: String,
        val outputs: Set<URI>,
        val sources: Set<URI>,
        val dependencies: List<String>,
        val interfaceJars: Set<URI> = emptySet(),
        val goImportPath: String? = "",
        val goRoot: URI? = URI(""),
)


