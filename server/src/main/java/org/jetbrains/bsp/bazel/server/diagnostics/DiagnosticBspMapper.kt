package org.jetbrains.bsp.bazel.server.diagnostics

import com.jetbrains.bsp.bsp4kt.Diagnostic as BspDiagnostic
import com.jetbrains.bsp.bsp4kt.DiagnosticSeverity as BspDiagnosticSeverity
import com.jetbrains.bsp.bsp4kt.Position as BspPosition
import com.jetbrains.bsp.bsp4kt.Range as BspRange
import com.jetbrains.bsp.bsp4kt.BuildTargetIdentifier
import com.jetbrains.bsp.bsp4kt.PublishDiagnosticsParams
import com.jetbrains.bsp.bsp4kt.TextDocumentIdentifier
import java.nio.file.Paths
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import java.nio.file.Path

class DiagnosticBspMapper(private val workspaceRoot: Path) {

    fun createDiagnostics(diagnostics: List<Diagnostic>, originId: String?): List<PublishDiagnosticsParams> {
        return diagnostics
            .groupBy { Pair(it.fileLocation, it.targetLabel) }
            .map { kv ->
                val bspDiagnostics = kv.value.map { createDiagnostic(it) }
                val doc = TextDocumentIdentifier(toAbsoluteUri(kv.key.first))
                val publishDiagnosticsParams =
                    PublishDiagnosticsParams(doc, BuildTargetIdentifier(kv.key.second), diagnostics = bspDiagnostics, reset = true, originId = originId)
                publishDiagnosticsParams
            }
    }

    private fun createDiagnostic(it: Diagnostic): BspDiagnostic {
        val position = BspPosition(it.position.line - 1, it.position.character - 1)
        val range = BspRange(position, position)
        val severity = when (it.level) {
            Level.Error -> BspDiagnosticSeverity.Error
            Level.Warning -> BspDiagnosticSeverity.Warning
        }
        return BspDiagnostic(range, message = it.message, severity = severity)
    }

    private fun toAbsoluteUri(rawFileLocation: String): String {
        var path = Paths.get(rawFileLocation)
        if (!path.isAbsolute) {
            path = workspaceRoot.resolve(path)
        }
        return path.toUri().toString()
    }

}
