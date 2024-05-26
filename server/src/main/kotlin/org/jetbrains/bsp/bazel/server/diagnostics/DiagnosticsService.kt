package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import java.nio.file.Path
import java.util.Collections

/**
 * @property hasAnyProblems keeps track of problems in given file so BSP reporter
 * can publish diagnostics with an empty array, to clear up former diagnostics.
 * see: https://youtrack.jetbrains.com/issue/BAZEL-376
 */
class DiagnosticsService(workspaceRoot: Path, private val hasAnyProblems: MutableMap<String, Set<TextDocumentIdentifier>>) {

    private val parser = DiagnosticsParser()
    private val mapper = DiagnosticBspMapper(workspaceRoot)
    /* Warnings are reported before the target completed event, when everything is cleared. so we want to avoid removing them */
    private val updatedInThisRun = mutableSetOf<PublishDiagnosticsParams>()

    fun getBspState() = Collections.unmodifiableMap(hasAnyProblems)

    fun extractDiagnostics(
        bazelOutput: String,
        targetLabel: String,
        originId: String?,
        diagnosticsFromProgress: Boolean
    ): List<PublishDiagnosticsParams> {
        val parsedDiagnostics = parser.parse(bazelOutput, targetLabel, diagnosticsFromProgress)
        val events = mapper.createDiagnostics(parsedDiagnostics, originId)
        if (diagnosticsFromProgress) updatedInThisRun.addAll(events)
        updateProblemState(events)
        return events
    }

    fun clearFormerDiagnostics(targetLabel: String): List<PublishDiagnosticsParams> {
        val docs = hasAnyProblems[targetLabel]
	    hasAnyProblems.remove(targetLabel)
        val toClear = if (updatedInThisRun.isNotEmpty()) {
            val updatedDocs = updatedInThisRun.map { it.textDocument }.toSet()
            hasAnyProblems[targetLabel] = updatedDocs
            updatedInThisRun.clear()
            docs?.subtract(updatedDocs)
        } else {
            docs
        }
	    return toClear
	      ?.map { PublishDiagnosticsParams(it, BuildTargetIdentifier(targetLabel), emptyList(), true)}
	      .orEmpty()
	}

    private fun updateProblemState(events: List<PublishDiagnosticsParams>) {
        events
            .groupBy { it.getBuildTarget().getUri() }
            .forEach {
                val buildTarget = it.key
                val paramss = it.value
                val docs = paramss.map { it.getTextDocument() }.toSet()
                hasAnyProblems[buildTarget] = docs
            }
    }
}
