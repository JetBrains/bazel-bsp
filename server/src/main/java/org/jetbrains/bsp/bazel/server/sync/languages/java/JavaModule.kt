package org.jetbrains.bsp.bazel.server.sync.languages.java

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import java.net.URI

data class Jdk(
    val version: String,
    val javaHome: URI?
)

data class JavaModule(
    val jdk: Jdk,
    val runtimeJdk: Jdk?,
    val javacOpts: List<String>,
    val jvmOps: List<String>,
    val mainOutput: URI,
    val allOutputs: List<URI>,
    val mainClass: String?,
    val args: List<String>,
    val runtimeClasspath: List<URI>,
    val compileClasspath: List<URI>,
    val sourcesClasspath: List<URI>,
    val ideClasspath: List<URI>
) : LanguageData
