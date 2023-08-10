package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import com.jetbrains.bsp.bsp4kt.BuildTargetIdentifier
import com.jetbrains.bsp.bsp4kt.JvmBuildTarget
import kotlinx.serialization.Serializable

@Serializable
data class KotlinBuildTarget(
    val languageVersion: String,
    val apiVersion: String,
    val kotlincOptions: KotlincOpts? = null,
    val associates: List<BuildTargetIdentifier>,
    val jvmBuildTarget: JvmBuildTarget? = null
)