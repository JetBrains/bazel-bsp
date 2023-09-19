package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule

data class KotlinModule(
    val languageVersion: String,
    val apiVersion: String,
    val kotlincOptions: List<String>,
    val associates: List<BuildTargetIdentifier>,
    val javaModule: JavaModule?
) : LanguageData
