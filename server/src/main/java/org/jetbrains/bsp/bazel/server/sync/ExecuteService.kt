package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.CleanCacheParams
import ch.epfl.scala.bsp4j.CleanCacheResult
import ch.epfl.scala.bsp4j.CompileParams
import ch.epfl.scala.bsp4j.CompileResult
import ch.epfl.scala.bsp4j.RunParams
import ch.epfl.scala.bsp4j.RunResult
import ch.epfl.scala.bsp4j.TestParams
import ch.epfl.scala.bsp4j.TestResult
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

class ExecuteService(
    private val compilationManager: BazelBspCompilationManager,
    private val projectProvider: ProjectProvider,
    private val bazelRunner: BazelRunner,
    private val workspaceContextProvider: WorkspaceContextProvider,
    private val bspClientTestNotifier: BspClientTestNotifier
) {
    fun compile(cancelChecker: CancelChecker, params: CompileParams): CompileResult {
        val targets = selectTargets(cancelChecker, params.targets)
        val result = build(cancelChecker, targets, params.originId)
        return CompileResult(result.statusCode).apply { originId = params.originId }
    }

    fun test(cancelChecker: CancelChecker, params: TestParams): TestResult {
        val targets = selectTargets(cancelChecker, params.targets)
        var result = build(cancelChecker, targets, params.originId)
        if (result.isNotSuccess) {
            return TestResult(result.statusCode)
        }
        val targetsSpec = TargetsSpec(targets, emptyList())
        result = bazelRunner.commandBuilder().test()
            .withTargets(targetsSpec)
            .withArguments(params.arguments)
            .withFlag(BazelFlag.testOutputAll())
            .executeBazelBesCommand(params.originId)
            .waitAndGetResult(cancelChecker, true)
        JUnit5TestParser(bspClientTestNotifier).processTestOutput(result)
        return TestResult(result.statusCode).apply {
            originId = originId
            data = result
        }
    }

    fun run(cancelChecker: CancelChecker, params: RunParams): RunResult {
        val targets = selectTargets(cancelChecker, listOf(params.target))
        if (targets.isEmpty()) {
            throw ResponseErrorException(
                ResponseError(
                    ResponseErrorCode.InvalidRequest,
                    "No supported target found for " + params.target.uri,
                    null
                )
            )
        }
        val bspId = targets.single()
        val result = build(cancelChecker, targets, params.originId)
        if (result.isNotSuccess) {
            return RunResult(result.statusCode)
        }
        val bazelProcessResult =
            bazelRunner.commandBuilder().run().withArgument(BspMappings.toBspUri(bspId))
                .withArguments(params.arguments).executeBazelBesCommand(params.originId).waitAndGetResult(cancelChecker)
        return RunResult(bazelProcessResult.statusCode).apply { originId = originId }
    }

    fun clean(cancelChecker: CancelChecker, params: CleanCacheParams?): CleanCacheResult {
        val bazelResult =
            bazelRunner.commandBuilder().clean().executeBazelBesCommand().waitAndGetResult(cancelChecker)
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
        val modules: Set<Module> = BspMappings.getModules(project, targets)
        val modulesToBuild = modules.filter { m: Module -> isBuildable(m) }
        return modulesToBuild.map(::toBspId)
    }

    private fun isBuildable(m: Module): Boolean =
        !m.isSynthetic && !m.tags.contains(Tag.NO_BUILD) && isBuildableIfManual(m)


    private fun isBuildableIfManual(m: Module): Boolean =
        (!m.tags.contains(Tag.MANUAL) ||
                workspaceContextProvider.currentWorkspaceContext().buildManualTargets.value)

}
