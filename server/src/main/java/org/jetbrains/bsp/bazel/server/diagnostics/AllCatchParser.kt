package org.jetbrains.bsp.bazel.server.diagnostics

object AllCatchParser : Parser {
  override fun tryParse(output: Output): List<Diagnostic> {
    output.tryTake()
    return emptyList()
  }
}
