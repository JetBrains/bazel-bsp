package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import java.nio.file.Path

class DiagnosticsService(workspaceRoot: Path) {

    private val parser = DiagnosticsParser()
    private val mapper = DiagnosticBspMapper(workspaceRoot)

    fun extractDiagnostics(
        bazelOutput: String,
        targetLabel: String,
        originId: String?
    ): List<PublishDiagnosticsParams> {
        val parsedDiagnostics = parser.parse(bazelOutput, targetLabel)
        return mapper.createDiagnostics(parsedDiagnostics, originId)
    }

}
