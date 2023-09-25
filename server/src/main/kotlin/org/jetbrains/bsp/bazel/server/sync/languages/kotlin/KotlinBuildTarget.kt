package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.JvmBuildTarget

data class KotlinBuildTarget(
    val languageVersion: String,
    val apiVersion: String,
    val kotlincOptions: List<String>,
    val associates: List<BuildTargetIdentifier>,
    var jvmBuildTarget: JvmBuildTarget? = null
)