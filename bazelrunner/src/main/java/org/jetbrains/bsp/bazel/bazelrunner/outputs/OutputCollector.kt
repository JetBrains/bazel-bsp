package org.jetbrains.bsp.bazel.bazelrunner.outputs

class OutputCollector : OutputHandler {
  private val lines = mutableListOf<String>()
  private val stringBuilder = StringBuilder()

  override fun onNextLine(line: String) {
    lines.add(line)
    stringBuilder.append(line)
    stringBuilder.append(System.lineSeparator())
  }

  fun lines(): List<String> = lines.toList()

  fun output(): String = stringBuilder.toString()
}
