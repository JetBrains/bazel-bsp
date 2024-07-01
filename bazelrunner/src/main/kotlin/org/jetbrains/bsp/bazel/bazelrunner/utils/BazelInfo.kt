package org.jetbrains.bsp.bazel.bazelrunner.utils

import java.nio.file.Path
import kotlin.io.path.isReadable
import kotlin.io.path.readText

interface BazelInfo {
  val execRoot: String
  val outputBase: Path
  val workspaceRoot: Path
  val release: BazelRelease
  val isBzlModEnabled: Boolean
}

data class BazelRelease(
  val major: Int
) {

  fun isRelativeWorkspacePath(label: String) = when (major) {
    in 0..3 -> throw RuntimeException("Unsupported Bazel version, use Bazel 4 or newer")
    in 4..5 -> label.startsWith("//")
    else -> label.startsWith("@//") || label.startsWith("@@//")
  }

  fun stripPrefix(label: String) = when (major) {
    in 0..3 -> throw RuntimeException("Unsupported Bazel version, use Bazel 4 or newer")
    in 4..5 -> label.removePrefix("//")
    else -> label.dropWhile { it == '@' }.removePrefix("//")
  }

  companion object {
    fun fromReleaseString(versionString: String): BazelRelease? =
      VERSION_REGEX.find(versionString)?.toBazelRelease()

    fun fromBazelVersionFile(workspacePath: Path): BazelRelease? {
      val versionString = workspacePath.resolve(".bazelversion")
              .takeIf { it.isReadable() }
              ?.readText()
              .orEmpty()
      return BAZEL_VERSION_MAJOR_REGEX.find(versionString)?.toBazelRelease()
    }

    private fun MatchResult.toBazelRelease() =
            BazelRelease(value.toInt())

    internal val LATEST_SUPPORTED_MAJOR = BazelRelease(6)

    private val BAZEL_VERSION_MAJOR_REGEX = """^\d+""".toRegex()

    private val VERSION_REGEX = """(?<=release )\d+(?=[0-9.]*)""".toRegex()
  }
}


fun BazelRelease?.orLatestSupported() = this ?: BazelRelease.LATEST_SUPPORTED_MAJOR

data class BasicBazelInfo(
  override val execRoot: String,
  override val outputBase: Path,
  override val workspaceRoot: Path,
  override val release: BazelRelease,
  override val isBzlModEnabled: Boolean,
) : BazelInfo

class LazyBazelInfo(bazelInfoSupplier: () -> BazelInfo) : BazelInfo {
  private val bazelInfo: BazelInfo by lazy { bazelInfoSupplier() }

  override val execRoot: String
    get() = bazelInfo.execRoot

  override val outputBase: Path
    get() = bazelInfo.outputBase

  override val workspaceRoot: Path
    get() = bazelInfo.workspaceRoot

  override val release: BazelRelease
    get() = bazelInfo.release

  override val isBzlModEnabled: Boolean
    get() = bazelInfo.isBzlModEnabled
}
