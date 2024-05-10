package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.jetbrains.bsp.bazel.server.model.Label
import java.nio.file.Path
import java.util.Collections

/**
 * @property hasAnyProblems keeps track of problems in given file so BSP reporter
 * can publish diagnostics with an empty array, to clear up former diagnostics.
 * see: https://youtrack.jetbrains.com/issue/BAZEL-376
 */
class DiagnosticsService(workspaceRoot: Path, private val hasAnyProblems: MutableMap<Label, Set<TextDocumentIdentifier>>) {

    private val parser = DiagnosticsParser()
    private val mapper = DiagnosticBspMapper(workspaceRoot)
    /* Warnings are reported before the target completed event, when everything is cleared. so we want to avoid removing them */
    private val updatedInThisRun = mutableSetOf<PublishDiagnosticsParams>()

    val bspState: Map<Label, Set<TextDocumentIdentifier>>
        get() = Collections.unmodifiableMap(hasAnyProblems)

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

    fun clearFormerDiagnostics(targetLabel: Label): List<PublishDiagnosticsParams> {
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
	      ?.map { PublishDiagnosticsParams(it, BuildTargetIdentifier(targetLabel.value), emptyList(), true)}
	      .orEmpty()
	}

    private fun updateProblemState(events: List<PublishDiagnosticsParams>) {
        events
            .groupBy { it.buildTarget.uri }
            .forEach { group ->
                val buildTarget = Label.parse(group.key)
                val params = group.value
                val docs = params.map { it.textDocument }.toSet()
                hasAnyProblems[buildTarget] = docs
            }
    }
}
