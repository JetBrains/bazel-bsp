package org.jetbrains.bsp.bazel.server.sync.languages.java

import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.bsp.bazel.commons.Format
import java.net.URI

data class Jdk(
    @param:JsonProperty("version") val version: String,
    @param:JsonProperty("javaHome") val javaHome: URI?
) {

    override fun toString(): String {
        return Format.`object`(
            "Jdk", Format.entry("version", version), Format.entry("javaHome", javaHome)
        )
    }
}
