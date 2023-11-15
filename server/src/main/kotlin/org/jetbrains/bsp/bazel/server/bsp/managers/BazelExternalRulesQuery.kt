package org.jetbrains.bsp.bazel.server.bsp.managers

import org.apache.logging.log4j.LogManager
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.commons.escapeNewLines

interface BazelExternalRulesQuery {
  fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String>
}

class BazelExternalRulesQueryImpl(
  private val bazelRunner: BazelRunner,
  private val isBzlModEnabled: Boolean) : BazelExternalRulesQuery {
  override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> = when (isBzlModEnabled) {
    true -> BazelBzlModExternalRulesQueryImpl(bazelRunner).fetchExternalRuleNames(cancelChecker) +
      BazelWorkspaceExternalRulesQueryImpl(bazelRunner).fetchExternalRuleNames(cancelChecker)

    false -> BazelWorkspaceExternalRulesQueryImpl(bazelRunner).fetchExternalRuleNames(cancelChecker)
  }
}

class BazelWorkspaceExternalRulesQueryImpl(private val bazelRunner: BazelRunner) : BazelExternalRulesQuery {
  override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> =
    bazelRunner.commandBuilder().query()
      .withArgument("kind(http_archive, //external:*)")
      .withFlag("--order_output=no")
      .executeBazelCommand(parseProcessOutput = false, useBuildFlags = false)
      .waitAndGetResult(cancelChecker, ensureAllOutputRead = true).let { result ->
        if (result.isNotSuccess) {
          log.warn("Bazel query failed with output: '${result.stderr.escapeNewLines()}'")
          null
        } else result.stdoutLines.mapNotNull { it.split(':').getOrNull(1) }
      } ?: listOf()

  companion object {
    private val log = LogManager.getLogger(BazelExternalRulesQueryImpl::class.java)
  }
}

class BazelBzlModExternalRulesQueryImpl(private val bazelRunner: BazelRunner) : BazelExternalRulesQuery {
  override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> {
    val jsonElement = bazelRunner.commandBuilder().graph()
      .withFlag("--output=json")
      .executeBazelCommand(parseProcessOutput = false, useBuildFlags = false)
      .waitAndGetResult(cancelChecker, ensureAllOutputRead = true).let { result ->
        if (result.isNotSuccess) {
          log.warn("Bazel query failed with output: '${result.stderr.escapeNewLines()}'")
          null
        } else result.stdout.toJson(log)
      }
    return jsonElement.extractValuesFromKey("key")
      .map { it.substringBefore('@') } // the element has the format <DEP_NAME>@<DEP_VERSION>
      .distinct()
  }

  companion object {
    private val log = LogManager.getLogger(BazelBzlModExternalRulesQueryImpl::class.java)
  }
}
