package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo

class DiagnosticsService(bazelInfo: BazelInfo) {

  private val parser = DiagnosticsParser()
  private val mapper = DiagnosticBspMapper(bazelInfo)

  fun extractDiagnostics(bazelOutput: String, target: String): List<PublishDiagnosticsParams> {
    val parsedDiagnostics = parser.parse(bazelOutput)
    if (parsedDiagnostics.isEmpty()) {
      return listOf(
        PublishDiagnosticsParams(
          TextDocumentIdentifier("unknown"),
          BuildTargetIdentifier(target),
          listOf(),
          false
        )
      )
    }
    return mapper.createDiagnostics(parsedDiagnostics, target)
  }

}
