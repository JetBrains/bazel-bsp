package org.jetbrains.bsp.bazel.server.sync.languages.java

import org.jetbrains.bsp.bazel.commons.Format
import java.net.URI

data class Jdk(
    val version: String,
    val javaHome: URI?
) {

    override fun toString(): String {
        return Format.`object`(
            "Jdk", Format.entry("version", version), Format.entry("javaHome", javaHome)
        )
    }
}
