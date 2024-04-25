package org.jetbrains.bsp.bazel.server.sync.languages.go

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import java.net.URI
import java.nio.file.Path

data class GoModule (
  val sdkHomePath: URI?,
  val importPath: String,
  val targetRoot: Path?,
): LanguageData
