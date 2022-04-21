package org.jetbrains.bsp.bazel.server.diagnostics

class DiagnosticsParser {

  fun parse(bazelOutput: String): List<Diagnostic> {
    val output = prepareOutput(bazelOutput)
    val diagnostics = collectDiagnostics(output)
    return deduplicate(diagnostics)
  }

  private fun prepareOutput(bazelOutput: String): Output {
    val lines = bazelOutput.lines()
    val relevantLines = lines.filterNot { line -> IgnoredLines.any { it.matches(line) } }
    return Output(relevantLines)
  }

  private fun collectDiagnostics(output: Output): List<Diagnostic> {
    val diagnostics = mutableListOf<Diagnostic>()
    while (output.nonEmpty()) {
      for (parser in Parsers) {
        val result = parser.tryParse(output)
        if (result.isNotEmpty()) {
          diagnostics.addAll(result)
          break
        }
      }
    }
    return diagnostics.toList()
  }

  private fun deduplicate(parsedDiagnostics: List<Diagnostic>): List<Diagnostic> =
      parsedDiagnostics
          .groupBy { Triple(it.fileLocation, it.message, it.position) }
          .values
          .map { similar ->
            if (similar.size == 1)
              similar.first()
            else
              similar.find { it.targetLabel != null } ?: similar.first()
          }

  companion object {
    private val Parsers = listOf(
        BazelRootMessageParser,
        CompilerDiagnosticParser,
        AllCatchParser
    )
    private val IgnoredLines = listOf(
        "^$".toRegex(),
        "Use --sandbox_debug to see verbose messages from the sandbox".toRegex()
    )
  }

}
