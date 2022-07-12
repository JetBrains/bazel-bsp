package org.jetbrains.bsp.bazel.server.sync.languages.scala

import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule

data class ScalaModule(
    val sdk: ScalaSdk,
    val scalacOpts: List<String>,
    val javaModule: JavaModule?
) : LanguageData {

    override fun toString(): String {
        return Format.`object`(
            "ScalaModule",
            Format.entry("sdk", sdk),
            Format.entry("scalacOpts", scalacOpts),
            Format.entry("javaModule", javaModule)
        )
    }

    companion object {
        @JvmStatic
        fun fromLanguageData(languageData: LanguageData?) = languageData as? ScalaModule
    }
}
