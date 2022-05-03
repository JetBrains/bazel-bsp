package org.jetbrains.bsp.bazel.bazelrunner

import java.nio.file.Paths

class BazelInfoResolver(
    private val bazelRunner: BazelRunner,
    private val storage: BazelInfoStorage
) {

  fun resolveBazelInfo(): BazelInfo {
    return LazyBazelInfo { storage.load() ?: bazelInfoFromBazel() }
  }

  private fun bazelInfoFromBazel(): BazelInfo {
    val processResult = bazelRunner.commandBuilder().info().executeBazelCommand().waitAndGetResult()
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
            ?: throw RuntimeException(String.format("Failed to resolve %s from bazel info", name))

    return BasicBazelInfo(
        execRoot = extract("execution_root"),
        workspaceRoot = Paths.get(extract("workspace"))
    )
  }

  companion object {
    private val InfoLinePattern = "([\\w-]+): (.*)".toRegex()
  }
}
