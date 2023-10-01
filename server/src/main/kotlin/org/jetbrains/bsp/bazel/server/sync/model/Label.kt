package org.jetbrains.bsp.bazel.server.sync.model

data class Label(val value: String) {

  fun targetName(): String =
    value.substringAfterLast(":", "")
}
