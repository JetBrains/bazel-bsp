package org.jetbrains.bsp.bazel.server.bsp.managers

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.server.bep.BepServer
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import java.nio.file.Path
import java.util.Optional

class BazelBspCompilationManager(
    private val bazelRunner: BazelRunner,
    private val hasAnyProblems: Map<String, Set<TextDocumentIdentifier>>,
) {
    var client: BuildClient? = null
    var workspaceRoot: Path? = null
    fun buildTargetsWithBep(
        cancelChecker: CancelChecker, targetSpecs: TargetsSpec, originId: String,
    ): BepBuildResult {
        return buildTargetsWithBep(cancelChecker, targetSpecs, emptyList(), originId, emptyList())
    }

    fun buildTargetsWithBep(
        cancelChecker: CancelChecker,
        targetSpecs: TargetsSpec,
        extraFlags: List<String>,
        originId: String?,
        environment: List<Pair<String, String>>
    ): BepBuildResult {
        val target = targetSpecs.values.firstOrNull()
        val bepServer = BepServer.newBepServer(client, workspaceRoot, hasAnyProblems, Optional.ofNullable(originId), Optional.ofNullable(target))
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
