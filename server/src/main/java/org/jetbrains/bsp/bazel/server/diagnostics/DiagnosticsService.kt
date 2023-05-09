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
                updateProblemState(buildTarget, docs)
            }
        return events
    }

    fun clearFormerDiagnostics(targetLabel: String): List<PublishDiagnosticsParams> {
	    val docs = state.hasAnyProblems[targetLabel]
	    state.hasAnyProblems.remove(targetLabel)
	    return docs
	      ?.map { PublishDiagnosticsParams(it, BuildTargetIdentifier(targetLabel), emptyList(), true)}
	      .orEmpty()
	}

    private fun updateProblemState(buildTarget: String, docs: Set<TextDocumentIdentifier>) {
        state.hasAnyProblems[buildTarget] = docs
    }
}
