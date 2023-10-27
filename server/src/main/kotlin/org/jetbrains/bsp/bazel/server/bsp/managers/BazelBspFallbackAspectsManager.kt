package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

class BazelBspFallbackAspectsManager(
    private val bazelRunner: BazelRunner,
    private val workspaceContextProvider: WorkspaceContextProvider
) {
    fun getAllPossibleTargets(cancelChecker: CancelChecker): List<String> {
        val targets = workspaceContextProvider.currentWorkspaceContext().targets
        return bazelRunner.commandBuilder().query()
            .withTargets(targets)
            .withFlags(listOf("--output=label", "--keep_going"))
            .executeBazelCommand(parseProcessOutput = false)
            .waitAndGetResult(cancelChecker, ensureAllOutputRead = true)
            .stdoutLines
    }
}
