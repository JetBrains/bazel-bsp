package org.jetbrains.bsp.bazel.server.sync.languages.scala

import org.jetbrains.bsp.bazel.commons.Format
import java.net.URI

data class ScalaSdk(
    val organization: String,
    val version: String,
    val binaryVersion: String,
    val compilerJars: List<URI>
) {

    override fun toString(): String {
        return Format.`object`(
            "ScalaSdk",
            Format.entry("organization", organization),
            Format.entry("version", version),
            Format.entry("binaryVersion", binaryVersion),
            Format.entry("compilerJars", compilerJars)
        )
    }
}
