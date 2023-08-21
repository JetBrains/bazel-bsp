package org.jetbrains.bsp.bazel.server.bsp.managers

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.server.bep.BepServer
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import java.io.IOException
import java.nio.file.Path
import java.util.Optional
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.system.exitProcess

class BazelBspCompilationManager(
    private val bazelRunner: BazelRunner, private val hasAnyProblems: Map<String, Set<TextDocumentIdentifier>>,
) {
    var client: BuildClient? = null
    var workspaceRoot: Path? = null
    fun buildTargetsWithBep(
        cancelChecker: CancelChecker, targetSpecs: TargetsSpec, originId: String,
    ): BepBuildResult {
        return buildTargetsWithBep(cancelChecker, targetSpecs, emptyList(), originId)
    }

    fun buildTargetsWithBep(
        cancelChecker: CancelChecker,
        targetSpecs: TargetsSpec,
        extraFlags: List<String>,
        originId: String?,
    ): BepBuildResult {
        val bepServer = BepServer.newBepServer(
            client, workspaceRoot, hasAnyProblems, Optional.ofNullable(originId)
        )
        val executor = Executors.newFixedThreadPool(4, threadFactory())
        val nettyServer = BepServer.nettyServerBuilder().addService(bepServer).executor(executor).build()
        try {
            nettyServer.start()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return try {
            val result = bazelRunner
                .commandBuilder()
                .build()
                .withFlags(extraFlags)
                .withTargets(targetSpecs)
                .executeBazelBesCommand(originId, nettyServer.port)
                .waitAndGetResult(cancelChecker, true)
            BepBuildResult(result, bepServer.bepOutput)
        } finally {
            nettyServer.shutdown()
            executor.shutdown()
        }
    }

    companion object {
        private fun threadFactory(): ThreadFactory =
            ThreadFactoryBuilder()
                .setNameFormat("grpc-netty-pool-%d")
                .setUncaughtExceptionHandler { _: Thread?, e: Throwable ->
                    e.printStackTrace()
                    exitProcess(1)
                }
                .build()
    }
}
