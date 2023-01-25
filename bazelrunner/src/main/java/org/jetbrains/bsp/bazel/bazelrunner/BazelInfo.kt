package org.jetbrains.bsp.bazel.bazelrunner

import java.lang.RuntimeException
import java.nio.file.Path

interface BazelInfo {
  val execRoot: String
  val workspaceRoot: Path
  val release: BazelRelease
}

data class BazelRelease(
        val versionString: String
) {
  val major: Int by lazy {
    """(?<=release )\d+(?=[0-9.]*)""".toRegex().find(versionString)?.value?.toInt()!!
  }

  fun mainRepositoryReferencePrefix() = when(major) {
    in 0..3 -> throw RuntimeException("Unsupported Bazel version, use Bazel 4 or newer")
    in 4..5 -> "//"
    else -> "@//"
  }
}
data class BasicBazelInfo(
    override val execRoot: String,
    override val workspaceRoot: Path,
    override val release: BazelRelease
) : BazelInfo


class LazyBazelInfo(bazelInfoSupplier: () -> BazelInfo) : BazelInfo {
  private val bazelInfo: BazelInfo by lazy { bazelInfoSupplier() }

  override val execRoot: String
    get() = bazelInfo.execRoot
  override val workspaceRoot: Path
    get() = bazelInfo.workspaceRoot
  override val release: BazelRelease
    get() = bazelInfo.release

}
