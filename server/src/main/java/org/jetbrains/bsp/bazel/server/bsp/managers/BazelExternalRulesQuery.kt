package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner

interface BazelExternalRulesQuery {
    fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String>
}

class BazelExternalRulesQueryImpl(private val bazelRunner: BazelRunner) : BazelExternalRulesQuery {

    override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> =
        bazelRunner.commandBuilder().query().withArgument("//external:*").executeBazelCommand()
            .waitAndGetResult(cancelChecker, ensureAllOutputRead = true)
            .stdoutLines
            .mapNotNull { it.split(':').getOrNull(1) }
}
