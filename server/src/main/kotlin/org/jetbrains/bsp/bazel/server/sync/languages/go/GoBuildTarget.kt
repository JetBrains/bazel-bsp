package org.jetbrains.bsp.bazel.server.sync.languages.go

data class GoBuildTarget(
  val sdkHomePath: String,
  val importPath: String,
)
