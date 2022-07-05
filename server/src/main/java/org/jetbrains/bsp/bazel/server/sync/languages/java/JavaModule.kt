package org.jetbrains.bsp.bazel.server.sync.languages.java

import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import java.net.URI

data class JavaModule(
    @param:JsonProperty("jdk") val jdk: Jdk,
    @param:JsonProperty("runtimeJdk") val runtimeJdk: Jdk?,
    @param:JsonProperty("javacOpts") val javacOpts: List<String>,
    @param:JsonProperty("jvmOps") val jvmOps: List<String>,
    @param:JsonProperty("mainOutput") val mainOutput: URI,
    @param:JsonProperty("allOutput") val allOutputs: List<URI>,
    @param:JsonProperty("mainClass") val mainClass: String?,
    @param:JsonProperty("args") val args: List<String>,
    @param:JsonProperty("runtimeClasspath") val runtimeClasspath: List<URI>,
    @param:JsonProperty("compileClasspath") val compileClasspath: List<URI>,
    @param:JsonProperty("sourcesClasspath") val sourcesClasspath: List<URI>,
    @param:JsonProperty("ideClasspath") val ideClasspath: List<URI>
) : LanguageData {

    override fun toString(): String {
        return Format.`object`(
            "JavaModule", Format.entry("jdk", jdk), Format.entry(
                "javacOpts", Format.iterableShort(
                    javacOpts.stream()
                )
            ), Format.entry(
                "jvmOps", Format.iterableShort(
                    jvmOps.stream()
                )
            ), Format.entry("mainOutput", mainOutput), Format.entry(
                "allOutputs", Format.iterableShort(
                    allOutputs.stream()
                )
            ), Format.entry(
                "mainClass", mainClass
            ), Format.entry(
                "args", Format.iterableShort(
                    args.stream()
                )
            ), Format.entry(
                "runtimeClasspath", Format.iterable(
                    runtimeClasspath.stream()
                )
            ), Format.entry(
                "compileClasspath", Format.iterable(
                    compileClasspath.stream()
                )
            ), Format.entry(
                "sourcesClasspath", Format.iterable(
                    sourcesClasspath.stream()
                )
            ), Format.entry(
                "ideClasspath", Format.iterable(
                    ideClasspath.stream()
                )
            )
        )
    }

    companion object {
        @JvmStatic
        fun fromLanguageData(languageData: LanguageData?): JavaModule? = when (languageData) {
            is JavaModule -> languageData
            is ScalaModule -> languageData.javaModule
            else -> null
        }
    }
}
