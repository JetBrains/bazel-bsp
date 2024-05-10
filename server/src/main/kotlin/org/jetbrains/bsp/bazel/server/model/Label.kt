package org.jetbrains.bsp.bazel.server.sync.model

@JvmInline
value class Label private constructor(val value: String) {

  fun targetName(): String =
    value.substringAfterLast(":", "")

  companion object {
    fun parse(value: String): Label =
      Label(value.intern())
  }
}
