package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.model.Label

data class KotlinModule(
    val languageVersion: String,
    val apiVersion: String,
    val kotlincOptions: List<String>,
    val associates: List<Label>,
    val javaModule: JavaModule?
) : LanguageData
