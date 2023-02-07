package org.jetbrains.bsp.bazel.server.sync.model

data class Label(val value: String) {

  // TODO TEST
  fun targetName(): String =
    value.substringAfterLast(":", "")
}
