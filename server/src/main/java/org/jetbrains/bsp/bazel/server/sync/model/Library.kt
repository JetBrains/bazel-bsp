package org.jetbrains.bsp.bazel.server.sync.model

import java.net.URI

data class Library(
        val label: String,
        val outputs: Set<URI>,
        val dependencies: List<String>
)
