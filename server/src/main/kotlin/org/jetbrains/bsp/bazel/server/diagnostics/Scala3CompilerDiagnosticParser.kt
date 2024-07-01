package org.jetbrains.bsp.bazel.server.diagnostics

object Scala3CompilerDiagnosticParser : Parser {

    override fun tryParse(output: Output): List<Diagnostic> =
        listOfNotNull(tryParseOne(output))

    private val DiagnosticHeader = """
      ^--\       # "-- " diagnostic start 
      \[E\d+\]   # "[E008]" code 
      ([^:]+): # (1) type of diagnostic
      ([^:]+):(\d+):(\d+) # (2) path, (3) line, (4) column
      [\s-]*$ # " -----------------" ending 
      """.toRegex(RegexOption.COMMENTS)

    // Scala 3 diagnostics have additional color printed, since Bazel uses renderedMessage field
    private val colorRegex = "\u001b\\[1A\u001b\\[K|\u001B\\[[;\\d]*m".toRegex()

    fun tryTake(output: Output, regex: Regex): MatchResult? =
        output.peek()?.let { regex.matchEntire(it.replace(colorRegex, "")) }?.also { output.take() }

    fun tryParseOne(output: Output): Diagnostic? {
        return tryTake(output, DiagnosticHeader)
            ?.let { match ->
                val level = if (match.groupValues[1].contains("Error")) Level.Error else Level.Warning
                val path = match.groupValues[2].trim()
                val line = match.groupValues[3].toInt()
                val messageLines = collectMessageLines(match.groupValues[1].trim(), output)
                val column = match.groupValues[4].toIntOrNull() ?: tryFindColumnNumber(messageLines) ?: 1
                val message = messageLines.joinToString("\n")
                Diagnostic(Position(line, column), message, level, path, output.targetLabel)
            }
    }

    private fun collectMessageLines(header: String, output: Output): List<String> {
        val lines = mutableListOf<String>()
        fun String.cleanLine(): String  =
            this.replace(colorRegex, "").trim()

        // skip lines with numbers which show the source and skip the next ^^^^ line
        if (output.peek()?.cleanLine()?.startsWith('|') == false) output.take(2)
        while (output.nonEmpty() && output.peek()?.cleanLine()?.startsWith('|') == true) {
            lines.add(output.take().cleanLine().removePrefix("|").trim())
        }
        lines.add(0, header)
        return lines
    }

    private val IssuePositionMarker = """^[\s\|]*\^\s*$""".toRegex() // ^ surrounded by whitespace only

    private fun tryFindColumnNumber(messageLines: List<String>): Int? {
        val line = messageLines.find { IssuePositionMarker.matches(it.replace(colorRegex, "")) }
        return line?.indexOf("^")?.let { it + 1 }
    }
}
