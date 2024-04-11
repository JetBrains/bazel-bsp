package org.jetbrains.bsp.bazel.server.bsp.managers

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.server.bep.BepServer
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import java.nio.file.Path

// TODO: remove this file once we untangle the spaghetti and use the method from ExecuteService

class BazelBspCompilationManager(
    private val bazelRunner: BazelRunner,
    private val bazelPathsResolver: BazelPathsResolver,
    private val hasAnyProblems: MutableMap<String, Set<TextDocumentIdentifier>>,
    val client: BuildClient,
    val workspaceRoot: Path,
) {
    fun buildTargetsWithBep(
        cancelChecker: CancelChecker,
        targetSpecs: TargetsSpec,
        extraFlags: List<String> = emptyList(),
        originId: String? = null,
        environment: List<Pair<String, String>> = emptyList()
    ): BepBuildResult {
        val target = targetSpecs.values.firstOrNull()
        val diagnosticsService = DiagnosticsService(workspaceRoot, hasAnyProblems)
        val bepServer = BepServer(client, diagnosticsService, originId, target, bazelPathsResolver)
        val bepReader = BepReader(bepServer)
        return try {
            bepReader.start()
            val result = bazelRunner
                .commandBuilder()
                .build()
                .withFlags(extraFlags)
                .withTargets(targetSpecs)
                .withEnvironment(environment)
                .executeBazelBesCommand(originId, bepReader.eventFile.toPath().toAbsolutePath())
                .waitAndGetResult(cancelChecker, true)
            bepReader.finishBuild()
            bepReader.await()
            BepBuildResult(result, bepServer.bepOutput)
        } finally {
            bepReader.finishBuild()
        }
    }

}
