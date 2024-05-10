package org.jetbrains.bsp.bazel.server.sync.languages.jvm

import org.jetbrains.bsp.bazel.server.sync.languages.android.AndroidModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.model.Module

val Module.javaModule: JavaModule?
    get() {
        return when (val data = languageData) {
            is JavaModule -> data
            is ScalaModule -> data.javaModule
            is KotlinModule -> data.javaModule
            is AndroidModule -> data.javaModule
            else -> null
        }
    }
