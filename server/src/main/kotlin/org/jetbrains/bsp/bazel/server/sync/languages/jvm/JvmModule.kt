package org.jetbrains.bsp.bazel.server.sync.languages.jvm

import org.jetbrains.bsp.bazel.server.sync.languages.android.AndroidModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.model.Module

val Module.javaModule: JavaModule?
    get() {
        return when (languageData) {
            is JavaModule -> languageData
            is ScalaModule -> languageData.javaModule
            is KotlinModule -> languageData.javaModule
            is AndroidModule -> languageData.javaModule
            else -> null
        }
    }
