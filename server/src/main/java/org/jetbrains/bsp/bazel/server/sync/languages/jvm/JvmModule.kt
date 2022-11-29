package org.jetbrains.bsp.bazel.server.sync.languages.jvm

import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule


val Module.javaModule: JavaModule?
    get() {
        return when (languageData) {
            is JavaModule -> languageData
            is ScalaModule -> languageData.javaModule
            else -> null
        }
    }

fun Module.withJavaModule(javaModule: JavaModule): Module {
    return when (languageData) {
        is JavaModule -> this.copy(languageData = javaModule)
        is ScalaModule -> this.copy(languageData = languageData.copy(javaModule = javaModule))
        else -> this
    }
}