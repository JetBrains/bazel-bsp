package org.jetbrains.bsp.bazel.server.sync.languages.scala

import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.bsp.bazel.commons.Format
import java.net.URI

data class ScalaSdk(
    @param:JsonProperty("organization") val organization: String,
    @param:JsonProperty("version") val version: String,
    @param:JsonProperty("binaryVersion") val binaryVersion: String,
    @param:JsonProperty("compilerJars") val compilerJars: List<URI>
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
