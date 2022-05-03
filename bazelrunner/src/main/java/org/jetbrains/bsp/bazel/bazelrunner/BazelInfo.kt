package org.jetbrains.bsp.bazel.bazelrunner

import java.nio.file.Path

interface BazelInfo {
  val execRoot: String
  val workspaceRoot: Path
}


data class BasicBazelInfo(
    override val execRoot: String,
    override val workspaceRoot: Path
) : BazelInfo


class LazyBazelInfo(bazelInfoSupplier: () -> BazelInfo) : BazelInfo {
  private val bazelInfo: BazelInfo by lazy { bazelInfoSupplier() }

  override val execRoot: String
    get() = bazelInfo.execRoot
  override val workspaceRoot: Path
    get() = bazelInfo.workspaceRoot

}
