package org.jetbrains.bsp.bazel.server.sync.model

data class Label(val value: String) {
    override fun toString(): String = value
}
