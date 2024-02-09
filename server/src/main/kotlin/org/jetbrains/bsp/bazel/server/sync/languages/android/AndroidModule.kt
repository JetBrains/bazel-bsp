package org.jetbrains.bsp.bazel.server.sync.languages.android

import org.jetbrains.bsp.AndroidTargetType
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import java.net.URI

data class AndroidModule(
  val androidJar: URI,
  val androidTargetType: AndroidTargetType,
  val manifest: URI?,
  val resources: List<URI>,
  val resourceFolders: List<URI>,
  val javaModule: JavaModule?,
) : LanguageData
