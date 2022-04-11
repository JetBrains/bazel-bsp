package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import java.nio.file.Paths
import ch.epfl.scala.bsp4j.Diagnostic as BspDiagnostic
import ch.epfl.scala.bsp4j.DiagnosticSeverity as BspDiagnosticSeverity
import ch.epfl.scala.bsp4j.Position as BspPosition
import ch.epfl.scala.bsp4j.Range as BspRange

class DiagnosticBspMapper(private val bazelInfo: BazelInfo) {

  fun diagnostics(parsedDiagnostics: List<Diagnostic>): List<PublishDiagnosticsParams> =
      sequence {
        val grouped = parsedDiagnostics.groupBy { it.fileLocation }
        for ((location, diagnostics) in grouped) {
          yield(createParams(location, diagnostics))
        }
      }.toList()

  private fun createParams(
      rawFileLocation: String,
      fileDiagnostics: List<Diagnostic>
  ): PublishDiagnosticsParams {
    val fileLocation = TextDocumentIdentifier(toAbsoluteUri(rawFileLocation))
    val targetId = BuildTargetIdentifier(fileDiagnostics[0].targetLabel ?: "")
    val diagnostics = fileDiagnostics.map { createDiagnostic(it) }
    return PublishDiagnosticsParams(fileLocation, targetId, diagnostics, false)
  }

  private fun createDiagnostic(it: Diagnostic): BspDiagnostic {
    val position = BspPosition(it.position.line - 1, it.position.character - 1)
    val range = BspRange(position, position)
    return BspDiagnostic(range, it.message).apply {
      severity = when (it.level) {
        Level.Error -> BspDiagnosticSeverity.ERROR
        Level.Warning -> BspDiagnosticSeverity.WARNING
      }
    }
  }

  private fun toAbsoluteUri(rawFileLocation: String): String {
    var path = Paths.get(rawFileLocation)
    if (!path.isAbsolute) {
      path = bazelInfo.workspaceRoot().resolve(path)
    }
    return path.toUri().toString()
  }

}
