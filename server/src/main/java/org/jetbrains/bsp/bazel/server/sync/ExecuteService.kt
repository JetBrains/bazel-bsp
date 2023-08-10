package org.jetbrains.bsp.bazel.server.sync

import com.jetbrains.bsp.bsp4kt.BuildTargetIdentifier
import com.jetbrains.bsp.bsp4kt.CleanCacheParams
import com.jetbrains.bsp.bsp4kt.CleanCacheResult
import com.jetbrains.bsp.bsp4kt.CompileParams
import com.jetbrains.bsp.bsp4kt.CompileResult
import com.jetbrains.bsp.bsp4kt.RunParams
import com.jetbrains.bsp.bsp4kt.RunResult
import com.jetbrains.bsp.bsp4kt.StatusCode
import com.jetbrains.bsp.bsp4kt.TaskId
import com.jetbrains.bsp.bsp4kt.TestParams
import com.jetbrains.bsp.bsp4kt.TestResult
import com.jetbrains.bsp.bsp4kt.TestStatus
import com.jetbrains.bsp.bsp4kt.TextDocumentIdentifier
import io.grpc.Server
import com.jetbrains.jsonrpc4kt.CancelChecker
import com.jetbrains.jsonrpc4kt.ResponseErrorException
import com.jetbrains.jsonrpc4kt.messages.ResponseError
import com.jetbrains.jsonrpc4kt.messages.ResponseErrorCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.bazel.server.bep.BepServer
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.util.Optional

class ExecuteService(
    private val compilationManager: BazelBspCompilationManager,
    private val projectProvider: ProjectProvider,
    private val bazelRunner: BazelRunner,
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bspClientTestNotifier: BspClientTestNotifier,
    private val hasAnyProblems: Map<String, Set<TextDocumentIdentifier>>
) {
    private fun <T> withBepServer(body : (Server) -> T) :T {
        val server = BepServer.newBepServer(compilationManager.client, compilationManager.workspaceRoot, hasAnyProblems, Optional.empty())
        val nettyServer = BepServer.nettyServerBuilder().addService(server).build()
        nettyServer.start()
        try {
            return body(nettyServer)
        } finally {
            nettyServer.shutdown()
        }
    }

    fun compile(cancelChecker: CancelChecker, params: CompileParams): CompileResult {
        val targets = selectTargets(cancelChecker, params.targets)

        return if (targets.isNotEmpty()) {
            val result = build(cancelChecker, targets, params.originId)
            CompileResult(statusCode = result.statusCode, originId = params.originId)
        } else {
            CompileResult(statusCode = StatusCode.Error, originId = params.originId)
        }
    }

    fun test(cancelChecker: CancelChecker, params: TestParams): TestResult {
        val targets = selectTargets(cancelChecker, params.targets)
        var result = build(cancelChecker, targets, params.originId)
        if (result.isNotSuccess) {
            return TestResult(statusCode = result.statusCode)
        }
        val targetsSpec = TargetsSpec(targets, emptyList())
        val taskId = TaskId(id = params.originId ?: TODO())
        val displayName = targets.joinToString(", ") { it.uri }
        bspClientTestNotifier.startTest(
            isSuite = false,
            displayName = displayName,
            taskId = taskId
        )

        result = bazelRunner.commandBuilder().test()
            .withTargets(targetsSpec)
            .withArguments(params.arguments)
            .withFlag(BazelFlag.testOutputAll())
            .withFlag(BazelFlag.color(true))
            .executeBazelCommand(params.originId)
            .waitAndGetResult(cancelChecker, true)

        bspClientTestNotifier.finishTest(
            isSuite = false,
            displayName = displayName,
            taskId = taskId,
            status = if (result.statusCode == StatusCode.Ok) TestStatus.Passed else TestStatus.Failed,
            message = null,
        )
        val data = Json.encodeToJsonElement(result)
        return TestResult(statusCode = result.statusCode, originId = params.originId, data = data)
    }

    fun run(cancelChecker: CancelChecker, params: RunParams): RunResult {
        val targets = selectTargets(cancelChecker, listOf(params.target))
        if (targets.isEmpty()) {
            throw ResponseErrorException(
                ResponseError(
                    ResponseErrorCode.InvalidRequest.code,
                    "No supported target found for " + params.target.uri,
                    null
                )
            )
        }
        val bspId = targets.single()
        val result = build(cancelChecker, targets, params.originId)
        if (result.isNotSuccess) {
            return RunResult(statusCode = result.statusCode)
        }
        val bazelProcessResult =
            bazelRunner.commandBuilder()
                .run()
                .withArgument(BspMappings.toBspUri(bspId))
                .withArguments(params.arguments)
                .withFlag(BazelFlag.color(true))
                .executeBazelCommand(params.originId)
                .waitAndGetResult(cancelChecker)
        return RunResult(originId = params.originId, bazelProcessResult.statusCode)
    }

    fun clean(cancelChecker: CancelChecker, params: CleanCacheParams?): CleanCacheResult {
        val bazelResult = withBepServer { server ->
            bazelRunner.commandBuilder().clean()
                .executeBazelBesCommand(bazelBesPort = server.port).waitAndGetResult(cancelChecker)
        }
        return CleanCacheResult(bazelResult.stdout, true)
    }

    private fun build(cancelChecker: CancelChecker, bspIds: List<BuildTargetIdentifier>, originId: String?): BazelProcessResult {
        val targetsSpec = TargetsSpec(bspIds, emptyList())
        return compilationManager.buildTargetsWithBep(
                cancelChecker,
            targetsSpec,
            originId
        ).processResult()
    }

    private fun selectTargets(cancelChecker: CancelChecker, targets: List<BuildTargetIdentifier>): List<BuildTargetIdentifier> {
        val project = projectProvider.get(cancelChecker)
        val modules = BspMappings.getModules(project, targets)
        val modulesToBuild = modules.filter { isBuildable(it) }
        return modulesToBuild.map(::toBspId)
    }

    private fun isBuildable(m: Module): Boolean =
        !m.isSynthetic && !m.tags.contains(Tag.NO_BUILD) && isBuildableIfManual(m)


    private fun isBuildableIfManual(m: Module): Boolean =
        (!m.tags.contains(Tag.MANUAL) ||
                workspaceContextProvider.currentWorkspaceContext().buildManualTargets.value)

}
