package org.jetbrains.bsp.bazel.commons

fun String.escapeNewLines(): String =
  this.replace("\n", "\\n")
