package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.*
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo

class DiagnosticsService(bazelInfo: BazelInfo) {

  private val parser = DiagnosticsParser()
  private val mapper = DiagnosticBspMapper(bazelInfo)

  fun extractDiagnostics(bazelOutput: String): List<PublishDiagnosticsParams> {
    val parsedDiagnostics = parser.parse(bazelOutput)
    return mapper.diagnostics(parsedDiagnostics)
  }

}
