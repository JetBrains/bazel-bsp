package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.Diagnostic
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import java.nio.file.Path
import org.jetbrains.bsp.bazel.commons.BspCompileState

class DiagnosticsService(workspaceRoot: Path, private val state: BspCompileState) {

    private val parser = DiagnosticsParser()
    private val mapper = DiagnosticBspMapper(workspaceRoot)

    fun extractDiagnostics(
        bazelOutput: String,
        targetLabel: String,
        originId: String?
    ): List<PublishDiagnosticsParams> {
        val parsedDiagnostics = parser.parse(bazelOutput, targetLabel)
        val events = mapper.createDiagnostics(parsedDiagnostics, originId)
        events
            .groupBy { it.getBuildTarget().getUri() }
            .forEach {
                val buildTarget = it.key
                val paramss = it.value
                val docs = paramss.map { it.getTextDocument() }.toSet()
                state.hasAnyProblems.put(buildTarget, docs)
            }
        return events
    }

    fun clearFormerDiagnostics(targetLabel: String): List<PublishDiagnosticsParams> {
        val id = targetLabel
        val docs = state.hasAnyProblems[id]
        if (docs != null && docs.isNotEmpty()) {
            val diagnostics = emptyList<Diagnostic>()
            state.hasAnyProblems.remove(id)
            return docs.map { PublishDiagnosticsParams(it, BuildTargetIdentifier(id), diagnostics, true) }
        } else {
            return emptyList<PublishDiagnosticsParams>()
        }
    }
}
