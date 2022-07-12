package org.jetbrains.bsp.bazel.server.sync.languages.scala

import java.net.URI

data class ScalaSdk(
    val organization: String,
    val version: String,
    val binaryVersion: String,
    val compilerJars: List<URI>
)
