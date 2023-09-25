package org.jetbrains.bsp.bazel.server.diagnostics

interface Parser {
  fun tryParse(output: Output): List<Diagnostic>
}
