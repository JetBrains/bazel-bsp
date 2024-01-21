package org.jetbrains.bsp

import java.net.URI

data class GoBuildTarget(
  val sdkHomePath: URI?,
  val importPath: String?,
)

