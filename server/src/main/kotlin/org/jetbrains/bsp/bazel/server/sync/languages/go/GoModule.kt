package org.jetbrains.bsp.bazel.server.sync.languages.go

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData

data class GoModule (
  val sdkHomePath: String,
  val importPath: String,
): LanguageData