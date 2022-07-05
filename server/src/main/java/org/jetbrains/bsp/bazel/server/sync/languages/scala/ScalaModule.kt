package org.jetbrains.bsp.bazel.server.sync.languages.scala

import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule

data class ScalaModule(
    @param:JsonProperty("sdk") val sdk: ScalaSdk,
    @param:JsonProperty("scalacOpts") val scalacOpts: List<String>,
    @param:JsonProperty("javaModule") val javaModule: JavaModule?
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
