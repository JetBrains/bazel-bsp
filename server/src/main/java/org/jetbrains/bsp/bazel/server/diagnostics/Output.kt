package org.jetbrains.bsp.bazel.server.diagnostics

class Output(private val lines: List<String>, val targetLabel: String) {

  private var pointer = 0

  fun nonEmpty(): Boolean =
      pointer < lines.size

  fun peek(): String? =
      lines.getOrNull(pointer)

  fun take(): String =
      lines[pointer++]

  fun peek(limit: Int): List<String> =
      lines.subList(pointer, (pointer + limit).coerceAtMost(lines.size))

  fun take(count: Int): List<String> =
      peek(count).also { pointer += count }

  fun tryTake(): String? =
      peek()?.also { take() }

  fun tryTake(regex: Regex): MatchResult? =
      peek()?.let { regex.matchEntire(it) }?.also { take() }

  fun fullOutput(): String = lines.joinToString(System.lineSeparator())
}
