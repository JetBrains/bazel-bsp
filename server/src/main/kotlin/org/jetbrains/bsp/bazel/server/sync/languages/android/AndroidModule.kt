package org.jetbrains.bsp.bazel.server.sync.languages.android

import org.jetbrains.bsp.AndroidTargetType
import org.jetbrains.bsp.bazel.server.model.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinModule
import java.net.URI

data class AndroidModule(
  val androidJar: URI,
  val androidTargetType: AndroidTargetType,
  val manifest: URI?,
  val resourceDirectories: List<URI>,
  val resourceJavaPackage: String?,
  val assetsDirectories: List<URI>,
  val javaModule: JavaModule?,
  val kotlinModule: KotlinModule?,
) : LanguageData
