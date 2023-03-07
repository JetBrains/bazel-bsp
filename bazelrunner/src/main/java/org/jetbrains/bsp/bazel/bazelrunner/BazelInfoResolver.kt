package org.jetbrains.bsp.bazel.bazelrunner

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import java.nio.file.Paths

class BazelInfoResolver(
    private val bazelRunner: BazelRunner,
    private val storage: BazelInfoStorage
) {

  fun resolveBazelInfo(cancelChecker: CancelChecker): BazelInfo {
    return LazyBazelInfo { storage.load() ?: bazelInfoFromBazel(cancelChecker) }
  }

  private fun bazelInfoFromBazel(cancelChecker: CancelChecker): BazelInfo {
    val processResult = bazelRunner.commandBuilder().info().executeBazelCommand().waitAndGetResult(cancelChecker,true)
    return parseBazelInfo(processResult).also { storage.store(it) }
  }

  private fun parseBazelInfo(bazelProcessResult: BazelProcessResult): BasicBazelInfo {
    val outputMap = bazelProcessResult
        .stdoutLines
        .mapNotNull { line ->
          InfoLinePattern.matchEntire(line)?.let { it.groupValues[1] to it.groupValues[2] }
        }.toMap()

    fun extract(name: String): String =
        outputMap[name]
            ?: throw RuntimeException("Failed to resolve ${name} from bazel info in ${bazelRunner.workspaceRoot}. Bazel Info output:\n${bazelProcessResult.stdout}\n")

    return BasicBazelInfo(
        execRoot = extract("execution_root"),
        workspaceRoot = Paths.get(extract("workspace")),
        release = BazelRelease.fromReleaseString(extract("release"))
    )
  }

  companion object {
    private val InfoLinePattern = "([\\w-]+): (.*)".toRegex()
  }
}
