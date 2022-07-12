package org.jetbrains.bsp.bazel.server.sync.languages.java

import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import java.net.URI

data class JavaModule(
    val jdk: Jdk,
    val runtimeJdk: Jdk?,
    val javacOpts: List<String>,
    val jvmOps: List<String>,
    val mainOutput: URI,
    val allOutputs: List<URI>,
    val mainClass: String?,
    val args: List<String>,
    val runtimeClasspath: List<URI>,
    val compileClasspath: List<URI>,
    val sourcesClasspath: List<URI>,
    val ideClasspath: List<URI>
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
