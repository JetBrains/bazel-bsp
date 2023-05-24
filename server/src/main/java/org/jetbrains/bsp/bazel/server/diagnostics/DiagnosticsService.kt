package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.Diagnostic
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * @property hasAnyProblems keeps track of problems in given file so BSP reporter
 * can publish diagnostics with an empty array, to clear up former diagnostics.
 * see: https://youtrack.jetbrains.com/issue/BAZEL-376
 */
class DiagnosticsService(workspaceRoot: Path, private val hasAnyProblems: ConcurrentHashMap<String, Set<TextDocumentIdentifier>>) {

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
	    val docs = hasAnyProblems[targetLabel]
	    hasAnyProblems.remove(targetLabel)
	    return docs
	      ?.map { PublishDiagnosticsParams(it, BuildTargetIdentifier(targetLabel), emptyList(), true)}
	      .orEmpty()
	}

    private fun updateProblemState(buildTarget: String, docs: Set<TextDocumentIdentifier>) {
        hasAnyProblems[buildTarget] = docs
    }
}
