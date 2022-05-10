package org.jetbrains.bsp.bazel.server.sync.languages.java

import io.vavr.control.Option
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import java.net.URI
import kotlin.io.path.toPath

class JdkResolver(
    private val bazelPathsResolver: BazelPathsResolver,
    private val jdkVersionResolver: JdkVersionResolver
) {

  fun resolve(targets: List<TargetInfo>): Jdk? {
    val allCandidates = targets.mapNotNull { resolveJdkData(it) }.distinct().map { JdkCandidate(it) }
    if (allCandidates.isEmpty()) return null
    val latestVersion = candidatesWithLatestVersion(allCandidates)
    val complete = allCandidates.filter { it.isComplete }
    val latestVersionAndComplete = latestVersion.filter { it.isComplete }
    return (
        pickCandidateFromJvmRuntime(latestVersionAndComplete) ?:
        pickAnyCandidate(latestVersionAndComplete) ?:
        pickCandidateFromJvmRuntime(complete) ?:
        pickAnyCandidate(complete) ?:
        pickAnyCandidate(allCandidates)
      )?.asJdk()
  }

  private fun candidatesWithLatestVersion(candidates: List<JdkCandidate>): List<JdkCandidate> =
      findLatestVersion(candidates)
        ?.let { version -> candidates.filter { it.version == version } }
        .orEmpty()
        .sortedBy { it.javaHome.toString() } // for predictable results

  private fun findLatestVersion(candidates: List<JdkCandidate>): String? =
      candidates.mapNotNull { it.version }.maxByOrNull { Integer.parseInt(it) }

  private fun pickCandidateFromJvmRuntime(candidates: List<JdkCandidate>) =
      candidates.find { it.isRuntime }

  private fun pickAnyCandidate(candidates: List<JdkCandidate>): JdkCandidate? =
      candidates.firstOrNull()

  private fun resolveJdkData(targetInfo: TargetInfo): JdkCandidateData? {
    val hasRuntimeJavaHome = targetInfo.hasJavaRuntimeInfo() &&
        targetInfo.javaRuntimeInfo.hasJavaHome()
    val hasToolchainJavaHome = targetInfo.hasJavaToolchainInfo() &&
        targetInfo.javaToolchainInfo.hasJavaHome()

    val javaHomeFile =
        if (hasRuntimeJavaHome)
          targetInfo.javaRuntimeInfo.javaHome
        else if (hasToolchainJavaHome)
          targetInfo.javaToolchainInfo.javaHome
        else
          null
    val javaHome = javaHomeFile?.let { bazelPathsResolver.resolveUri(it) }

    val version =
        if (targetInfo.hasJavaToolchainInfo())
          targetInfo.javaToolchainInfo.sourceVersion
        else
          null

    return JdkCandidateData(hasRuntimeJavaHome, javaHome, version)
        .takeIf { javaHome != null || version != null }
  }

  private inner class JdkCandidate(private val data: JdkCandidateData) {
    val version = data.version ?: data.javaHome?.let { jdkVersionResolver.resolve(it.toPath()) }?.toString()
    val javaHome by data::javaHome
    val isRuntime by data::isRuntime
    val isComplete = javaHome != null && version != null
    fun asJdk(): Jdk? = version?.let { Jdk(it, Option.of(javaHome)) }
  }

  private data class JdkCandidateData(
      val isRuntime: Boolean,
      val javaHome: URI?,
      val version: String?
  )

}
