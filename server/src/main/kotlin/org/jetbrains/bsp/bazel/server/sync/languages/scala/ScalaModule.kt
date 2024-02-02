package org.jetbrains.bsp.bazel.server.sync.languages.scala

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import java.net.URI

data class ScalaSdk(
    val organization: String,
    val version: String,
    val binaryVersion: String,
    val compilerJars: List<URI>
)

data class ScalaModule(
    val sdk: ScalaSdk,
    val scalacOpts: List<String>,
    val javaModule: JavaModule?
) : LanguageData
