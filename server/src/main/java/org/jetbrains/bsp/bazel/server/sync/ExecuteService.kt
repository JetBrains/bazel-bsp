package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.*
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
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
    private val workspaceContextProvider: WorkspaceContextProvider
) {
    fun compile(params: CompileParams): CompileResult {
        val targets = selectTargets(params.targets)
        val result = build(targets)
        return CompileResult(result.statusCode)
    }

    fun test(params: TestParams): TestResult {
        val targets = selectTargets(params.targets)
        var result = build(targets)
        if (result.isNotSuccess) {
            return TestResult(result.statusCode)
        }
        result = bazelRunner.commandBuilder().test().withTargets(
            targets.map(BspMappings::toBspUri)
        ).withArguments(params.arguments).executeBazelBesCommand().waitAndGetResult()
        return TestResult(result.statusCode)
    }

    fun run(params: RunParams): RunResult {
        val targets = selectTargets(listOf(params.target))
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
        val result = build(targets)
        if (result.isNotSuccess) {
            return RunResult(result.statusCode)
        }
        val bazelProcessResult =
            bazelRunner.commandBuilder().run().withArgument(BspMappings.toBspUri(bspId))
                .withArguments(params.arguments).executeBazelBesCommand().waitAndGetResult()
        return RunResult(bazelProcessResult.statusCode)
    }

    fun clean(params: CleanCacheParams?): CleanCacheResult {
        val bazelResult =
            bazelRunner.commandBuilder().clean().executeBazelBesCommand().waitAndGetResult()
        return CleanCacheResult(bazelResult.stdout, true)
    }

    private fun build(bspIds: List<BuildTargetIdentifier>): BazelProcessResult {
        val targetsSpec = TargetsSpec(bspIds, emptyList())
        return compilationManager.buildTargetsWithBep(targetsSpec).processResult()
    }

    private fun selectTargets(targets: List<BuildTargetIdentifier>): List<BuildTargetIdentifier> {
        val project = projectProvider.get()
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
