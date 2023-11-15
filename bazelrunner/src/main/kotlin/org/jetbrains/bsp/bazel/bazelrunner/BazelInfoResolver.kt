package org.jetbrains.bsp.bazel.bazelrunner

import ch.epfl.scala.bsp4j.StatusCode
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.commons.escapeNewLines
import java.nio.file.Paths

class BazelInfoResolver(
  private val bazelRunner: BazelRunner,
  private val storage: BazelInfoStorage
) {

  fun resolveBazelInfo(cancelChecker: CancelChecker): BazelInfo {
    return LazyBazelInfo { storage.load() ?: bazelInfoFromBazel(cancelChecker) }
  }

  private fun bazelInfoFromBazel(cancelChecker: CancelChecker): BazelInfo {
    val isBzlModEnabled = calculateBzlModEnabled(cancelChecker)
    val processResult = bazelRunner.commandBuilder()
        .info().executeBazelCommand(useBuildFlags = false)
        .waitAndGetResult(cancelChecker,true)
    return parseBazelInfo(processResult, isBzlModEnabled).also { storage.store(it) }
  }

  private fun parseBazelInfo(bazelProcessResult: BazelProcessResult, isBzlModEnabled: Boolean): BasicBazelInfo {
    val outputMap = bazelProcessResult
      .stdoutLines
      .mapNotNull { line ->
        InfoLinePattern.matchEntire(line)?.let { it.groupValues[1] to it.groupValues[2] }
      }.toMap()

    fun BazelProcessResult.meaningfulOutput() = if (isNotSuccess) stderr else stdout

    fun extract(name: String): String =
      outputMap[name]
        ?: error("Failed to resolve $name from bazel info in ${bazelRunner.workspaceRoot}. " +
          "Bazel Info output: '${bazelProcessResult.meaningfulOutput().escapeNewLines()}'")

    fun obtainBazelReleaseVersion() = BazelRelease.fromReleaseString(extract("release")) ?:
      bazelRunner.workspaceRoot?.let { BazelRelease.fromBazelVersionFile(it) }.orLatestSupported()

    return BasicBazelInfo(
      execRoot = extract("execution_root"),
      outputBase = Paths.get(extract("output_base")),
      workspaceRoot = Paths.get(extract("workspace")),
      release = obtainBazelReleaseVersion(),
      isBzlModEnabled = isBzlModEnabled
    )
  }

  // this method does a small check whether bzlmod is enabled in the project
  // by running an arbitrary a bazel mod command and check for ok status code
  private fun calculateBzlModEnabled(cancelChecker: CancelChecker) =
    bazelRunner.commandBuilder().showRepo().executeBazelCommand(parseProcessOutput = false, useBuildFlags = false).waitAndGetResult(cancelChecker).statusCode == StatusCode.OK

  companion object {
    private val InfoLinePattern = "([\\w-]+): (.*)".toRegex()
  }
}
