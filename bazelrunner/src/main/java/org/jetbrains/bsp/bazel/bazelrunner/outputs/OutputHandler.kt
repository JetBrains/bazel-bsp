package org.jetbrains.bsp.bazel.bazelrunner.outputs

fun interface OutputHandler {
  fun onNextLine(line: String)
}
