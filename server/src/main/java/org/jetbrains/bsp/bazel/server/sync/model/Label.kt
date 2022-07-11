package org.jetbrains.bsp.bazel.server.sync.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Label(@param:JsonProperty("value") val value: String) {

    override fun toString(): String {
        return value
    }
}
