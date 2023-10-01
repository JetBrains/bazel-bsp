package org.jetbrains.bsp.bazel.bazelrunner.utils

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelQueryKindParameters

object BazelArgumentsUtils {
  private const val FUNCTIONS_DELIMITER = " union "
  private const val EXCEPT_COMMAND = "except"

  fun joinBazelTargets(
      includedTargets: List<BuildTargetIdentifier>,
      excludedTargets: List<BuildTargetIdentifier>
  ): String {
    val rawIncludedTargets = toRawUris(includedTargets)
    val rawExcludedTargets = toRawUris(excludedTargets)
    if (rawExcludedTargets.isEmpty()) {
      return getJoinedBazelTargets(rawIncludedTargets)
    }
    val joinedIncludedTargets = joinBazelTargets(rawIncludedTargets)
    val joinedExcludedTargets = rawExcludedTargets.joinToString(" - ")
    return "($joinedIncludedTargets - $joinedExcludedTargets)"
  }

  fun toRawUris(targets: List<BuildTargetIdentifier>): List<String> =
      targets.map { it.uri }

  fun getJoinedBazelTargets(targets: List<String>): String {
    val joinedTargets = joinBazelTargets(targets)
    return "($joinedTargets)"
  }

  fun calculateExcludedTargetsWithExcludedPrefix(targets: List<BuildTargetIdentifier>): List<String> =
      toRawUris(targets).map { "-$it" }

  fun getMnemonicWithJoinedTargets(
      targets: List<String>, languageIds: List<String>): String {
    val joinedTargets = joinBazelTargets(targets)
    return getMnemonicsForTargets(joinedTargets, languageIds)
  }

  private fun getMnemonicsForTargets(targets: String, languageIds: List<String>) =
      languageIds.joinToString(FUNCTIONS_DELIMITER) { getMnemonicForLanguageAndTargets(it, targets) }

  fun getQueryKindForPatternsAndExpressions(parameters: List<BazelQueryKindParameters>): String =
      parameters.joinToString(FUNCTIONS_DELIMITER) { getQueryKind(it) }

  fun getQueryKindForPatternsAndExpressionsWithException(
      parameters: List<BazelQueryKindParameters>, exception: String): String {
    val kind = getQueryKindForPatternsAndExpressions(parameters)
    return "$kind $EXCEPT_COMMAND $exception"
  }

  private fun getQueryKind(parameter: BazelQueryKindParameters) =
      "kind(${parameter.pattern}, ${parameter.input})"

  private fun joinBazelTargets(targets: List<String>) =
      targets.joinToString(" + ")

  private fun getMnemonicForLanguageAndTargets(languageId: String, targets: String) =
      "mnemonic($languageId, $targets)"
}
