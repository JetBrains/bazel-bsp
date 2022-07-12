package org.jetbrains.bsp.bazel.server.sync.languages.scala

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule

data class ScalaModule(
    val sdk: ScalaSdk,
    val scalacOpts: List<String>,
    val javaModule: JavaModule?
) : LanguageData {

    companion object {
        @JvmStatic
        fun fromLanguageData(languageData: LanguageData?) = languageData as? ScalaModule
    }
}
