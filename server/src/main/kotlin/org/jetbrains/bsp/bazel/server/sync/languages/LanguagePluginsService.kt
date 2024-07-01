package org.jetbrains.bsp.bazel.server.sync.languages

import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.languages.android.AndroidLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.python.PythonLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.python.PythonModule
import org.jetbrains.bsp.bazel.server.sync.languages.rust.RustLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.server.model.Language
import org.jetbrains.bsp.bazel.server.model.Module

class LanguagePluginsService(
    val scalaLanguagePlugin: ScalaLanguagePlugin,
    private val javaLanguagePlugin: JavaLanguagePlugin,
    val cppLanguagePlugin: CppLanguagePlugin,
    private val kotlinLanguagePlugin: KotlinLanguagePlugin,
    private val thriftLanguagePlugin: ThriftLanguagePlugin,
    val pythonLanguagePlugin: PythonLanguagePlugin,
    val rustLanguagePlugin: RustLanguagePlugin,
    private val androidLanguagePlugin: AndroidLanguagePlugin,
) {
    private val emptyLanguagePlugin: EmptyLanguagePlugin = EmptyLanguagePlugin()

    fun prepareSync(targetInfos: Sequence<BspTargetInfo.TargetInfo>) {
        scalaLanguagePlugin.prepareSync(targetInfos)
        javaLanguagePlugin.prepareSync(targetInfos)
        cppLanguagePlugin.prepareSync(targetInfos)
        thriftLanguagePlugin.prepareSync(targetInfos)
        pythonLanguagePlugin.prepareSync(targetInfos)
        rustLanguagePlugin.prepareSync(targetInfos)
    }

    fun getPlugin(languages: Set<Language>): LanguagePlugin<*> =
        when {
            languages.contains(Language.ANDROID) -> androidLanguagePlugin
            languages.contains(Language.KOTLIN) -> kotlinLanguagePlugin
            languages.contains(Language.SCALA) -> scalaLanguagePlugin
            languages.contains(Language.JAVA) -> javaLanguagePlugin
            languages.contains(Language.CPP) -> cppLanguagePlugin
            languages.contains(Language.THRIFT) -> thriftLanguagePlugin
            languages.contains(Language.PYTHON) -> pythonLanguagePlugin
            languages.contains(Language.RUST) -> rustLanguagePlugin
            else -> emptyLanguagePlugin
        }

    fun extractCppModule(module: Module): CppModule? =
        module.languageData?.let {
            when(it) {
                is CppModule -> it
                else -> null
            }
        }

    fun extractPythonModule(module: Module): PythonModule? =
        module.languageData?.let {
            when (it) {
                is PythonModule -> it
                else -> null
            }
        }
}
