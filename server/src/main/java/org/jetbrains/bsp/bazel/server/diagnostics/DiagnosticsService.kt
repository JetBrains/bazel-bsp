package org.jetbrains.bsp.bazel.server.diagnostics

import com.jetbrains.bsp.bsp4kt.BuildTargetIdentifier
import com.jetbrains.bsp.bsp4kt.Diagnostic
import com.jetbrains.bsp.bsp4kt.PublishDiagnosticsParams
import com.jetbrains.bsp.bsp4kt.TextDocumentIdentifier
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

/**
 * @property hasAnyProblems keeps track of problems in given file so BSP reporter
 * can publish diagnostics with an empty array, to clear up former diagnostics.
 * see: https://youtrack.jetbrains.com/issue/BAZEL-376
 */
class DiagnosticsService(workspaceRoot: Path, private val hasAnyProblems: MutableMap<String, Set<TextDocumentIdentifier>>) {

    private val parser = DiagnosticsParser()
    private val mapper = DiagnosticBspMapper(workspaceRoot)

    fun getBspState() = Collections.unmodifiableMap(hasAnyProblems)

    fun extractDiagnostics(
        bazelOutput: String,
        targetLabel: String,
        originId: String?
    ): List<PublishDiagnosticsParams> {
        val parsedDiagnostics = parser.parse(bazelOutput, targetLabel)
        val events = mapper.createDiagnostics(parsedDiagnostics, originId)
        updateProblemState(events)
        return events
    }

    fun clearFormerDiagnostics(targetLabel: String): List<PublishDiagnosticsParams> {
	    val docs = hasAnyProblems[targetLabel]
	    hasAnyProblems.remove(targetLabel)
	    return docs
	      ?.map { PublishDiagnosticsParams(it, BuildTargetIdentifier(targetLabel), diagnostics = emptyList(), reset = true)}
	      .orEmpty()
	}

    private fun updateProblemState(events: List<PublishDiagnosticsParams>) {
        events
            .groupBy { it.buildTarget.uri }
            .forEach {
                val buildTarget = it.key
                val paramss = it.value
                val docs = paramss.map { it.textDocument }.toSet()
                hasAnyProblems[buildTarget] = docs
            }
    }
}
