package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.CleanCacheParams
import ch.epfl.scala.bsp4j.CleanCacheResult
import ch.epfl.scala.bsp4j.CompileParams
import ch.epfl.scala.bsp4j.CompileResult
import ch.epfl.scala.bsp4j.RunParams
import ch.epfl.scala.bsp4j.RunResult
import ch.epfl.scala.bsp4j.StatusCode
import ch.epfl.scala.bsp4j.TestParams
import ch.epfl.scala.bsp4j.TestResult
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.jetbrains.bsp.MobileInstallParams
import org.jetbrains.bsp.MobileInstallResult
import org.jetbrains.bsp.MobileInstallStartType
import org.jetbrains.bsp.RunWithDebugParams
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bep.BepServer
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BepReader
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId
import org.jetbrains.bsp.bazel.server.sync.languages.android.AdditionalAndroidBuildTargetsProvider
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import org.jetbrains.bsp.bazel.workspacecontext.isAndroidEnabled

class ExecuteService(
    private val compilationManager: BazelBspCompilationManager,
    private val projectProvider: ProjectProvider,
    private val bazelRunner: BazelRunner,
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bspClientLogger: BspClientLogger,
    private val bazelPathsResolver: BazelPathsResolver,
    private val additionalBuildTargetsProvider: AdditionalAndroidBuildTargetsProvider,
    private val hasAnyProblems: MutableMap<String, Set<TextDocumentIdentifier>>
) {
    private val debugRunner = DebugRunner(bazelRunner) { message, originId ->
        bspClientLogger.copy(originId = originId).error(message)
    }

    private fun <T> withBepServer(originId: String?, target: BuildTargetIdentifier?, body : (BepReader) -> T): T {
        val diagnosticsService = DiagnosticsService(compilationManager.workspaceRoot, hasAnyProblems)
        val server = BepServer(compilationManager.client,  diagnosticsService, originId, target, bazelPathsResolver)
        val bepReader = BepReader(server)

        try {
            bepReader.start()
            return body(bepReader)
        } finally {
            bepReader.finishBuild()
            bepReader.await()
        }
    }

    fun compile(cancelChecker: CancelChecker, params: CompileParams): CompileResult {
        val targets = selectTargets(cancelChecker, params.targets)

        return if (targets.isNotEmpty()) {
            val result = build(cancelChecker, targets, params.originId)
            CompileResult(result.statusCode).apply { originId = params.originId }
        } else {
            CompileResult(StatusCode.ERROR).apply { originId = params.originId }
        }
    }

    fun test(cancelChecker: CancelChecker, params: TestParams): TestResult {
        val targets = selectTargets(cancelChecker, params.targets)
        var result = build(cancelChecker, targets, params.originId)
        if (result.isNotSuccess) {
            return TestResult(result.statusCode)
        }
        val targetsSpec = TargetsSpec(targets, emptyList())

        // TODO: handle multiple targets
        withBepServer(params.originId, params.targets.single()) { bepReader ->
            result = bazelRunner.commandBuilder().test()
                .withTargets(targetsSpec)
                .withArguments(params.arguments)
                .withFlag(BazelFlag.testOutputAll())
                .withFlag(BazelFlag.color(true))
                .executeBazelBesCommand(params.originId, bepReader.eventFile.toPath())
                .waitAndGetResult(cancelChecker, true)
        }

        return TestResult(result.statusCode).apply {
            originId = originId
            data = result
        }
    }

    fun run(cancelChecker: CancelChecker, params: RunParams): RunResult {
        val targets = selectTargets(cancelChecker, listOf(params.target))
        val bspId = targets.singleOrResponseError(params.target)
        val result = build(cancelChecker, targets, params.originId)
        if (result.isNotSuccess) {
            return RunResult(result.statusCode)
        }
        val bazelProcessResult =
            bazelRunner.commandBuilder()
                .run()
                .withArgument(BspMappings.toBspUri(bspId))
                .withArguments(params.arguments)
                .withFlag(BazelFlag.color(true))
                .executeBazelCommand(params.originId)
                .waitAndGetResult(cancelChecker)
        return RunResult(bazelProcessResult.statusCode).apply { originId = originId }
    }

    fun runWithDebug(cancelChecker: CancelChecker, params: RunWithDebugParams): RunResult {
        val modules = selectModules(cancelChecker, listOf(params.runParams.target))
        val singleModule = modules.singleOrResponseError(params.runParams.target)
        val bspId = toBspId(singleModule)
        val result = build(cancelChecker, listOf(bspId), params.originId)
        if (result.isNotSuccess) {
            return RunResult(result.statusCode)
        }
        return debugRunner.runWithDebug(cancelChecker, params, singleModule)
    }

    fun mobileInstall(cancelChecker: CancelChecker, params: MobileInstallParams): MobileInstallResult {
        val targets = selectTargets(cancelChecker, listOf(params.target))
        val bspId = targets.singleOrResponseError(params.target)

        val startType = when (params.startType) {
            MobileInstallStartType.NO -> "no"
            MobileInstallStartType.COLD -> "cold"
            MobileInstallStartType.WARM -> "warm"
            MobileInstallStartType.DEBUG -> "debug"
        }

        val bazelProcessResult = bazelRunner.commandBuilder()
            .mobileInstall()
            .withArgument(BspMappings.toBspUri(bspId))
            .withArgument(BazelFlag.device(params.targetDeviceSerialNumber))
            .withArgument(BazelFlag.start(startType))
            .withFlag(BazelFlag.color(true))
            .executeBazelCommand(params.originId)
            .waitAndGetResult(cancelChecker)
        return MobileInstallResult(bazelProcessResult.statusCode, params.originId)
    }

    @Suppress("UNUSED_PARAMETER")  // params is used by BspRequestsRunner.handleRequest
    fun clean(cancelChecker: CancelChecker, params: CleanCacheParams?): CleanCacheResult {
        withBepServer(null, null) { bepReader ->
            bazelRunner.commandBuilder().clean()
                .executeBazelBesCommand(buildEventFile = bepReader.eventFile.toPath()).waitAndGetResult(cancelChecker)
        }
        return CleanCacheResult(true)
    }

    private fun build(cancelChecker: CancelChecker, bspIds: List<BuildTargetIdentifier>, originId: String): BazelProcessResult {
        val targets = bspIds + getAdditionalBuildTargets(cancelChecker, bspIds)
        val targetsSpec = TargetsSpec(targets, emptyList())
        // TODO: what if there's more than one target?
        //  (it was like this in now-deleted BazelBspCompilationManager.buildTargetsWithBep)
        return withBepServer(originId, bspIds.firstOrNull()) { bepReader ->
            bazelRunner
                .commandBuilder()
                .build()
                .withTargets(targetsSpec)
                .executeBazelBesCommand(originId, bepReader.eventFile.toPath().toAbsolutePath())
                .waitAndGetResult(cancelChecker, true)
        }
    }

    private fun getAdditionalBuildTargets(
        cancelChecker: CancelChecker,
        bspIds: List<BuildTargetIdentifier>,
    ): List<BuildTargetIdentifier> {
        val workspaceContext = workspaceContextProvider.currentWorkspaceContext()

        return if (workspaceContext.isAndroidEnabled) {
          additionalBuildTargetsProvider.getAdditionalBuildTargets(cancelChecker, bspIds)
        } else {
          emptyList()
        }
    }

    private fun selectTargets(cancelChecker: CancelChecker, targets: List<BuildTargetIdentifier>): List<BuildTargetIdentifier> =
            selectModules(cancelChecker, targets).map { toBspId(it) }

    private fun selectModules(cancelChecker: CancelChecker, targets: List<BuildTargetIdentifier>): List<Module> {
        val project = projectProvider.get(cancelChecker)
        val modules = BspMappings.getModules(project, targets)
        return modules.filter { isBuildable(it) }
    }

    private fun isBuildable(m: Module): Boolean =
        !m.isSynthetic && !m.tags.contains(Tag.NO_BUILD) && isBuildableIfManual(m)


    private fun isBuildableIfManual(m: Module): Boolean =
        (!m.tags.contains(Tag.MANUAL) ||
                workspaceContextProvider.currentWorkspaceContext().buildManualTargets.value)

}

private fun <T> List<T>.singleOrResponseError(
    requestedTarget: BuildTargetIdentifier,
): T =
    when {
        this.isEmpty() -> throwResponseError("No supported target found for ${requestedTarget.uri}")
        this.size == 1 -> this.single()
        else -> throwResponseError("More than one supported target found for ${requestedTarget.uri}")
    }

private fun throwResponseError(message: String, data: Any? = null): Nothing =
    throw ResponseErrorException(
        ResponseError(ResponseErrorCode.InvalidRequest, message, data)
    )
