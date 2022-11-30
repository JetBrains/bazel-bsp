package org.jetbrains.bsp.bazel.server.sync.languages

import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module

class LanguagePluginsService(
    val scalaLanguagePlugin: ScalaLanguagePlugin,
    val javaLanguagePlugin: JavaLanguagePlugin,
    val cppLanguagePlugin: CppLanguagePlugin,
    private val thriftLanguagePlugin: ThriftLanguagePlugin
) {
    private val emptyLanguagePlugin: EmptyLanguagePlugin = EmptyLanguagePlugin()

    fun prepareSync(targetInfos: Sequence<BspTargetInfo.TargetInfo>) {
        scalaLanguagePlugin.prepareSync(targetInfos)
        javaLanguagePlugin.prepareSync(targetInfos)
        cppLanguagePlugin.prepareSync(targetInfos)
        thriftLanguagePlugin.prepareSync(targetInfos)
    }

    fun getPlugin(languages: Set<Language>): LanguagePlugin<*> =
        when {
            languages.contains(Language.SCALA) -> scalaLanguagePlugin
            (languages.contains(Language.JAVA) || languages.contains(Language.KOTLIN)) -> javaLanguagePlugin
            languages.contains(Language.CPP) -> cppLanguagePlugin
            languages.contains(Language.THRIFT) -> thriftLanguagePlugin
            else -> emptyLanguagePlugin
        }

    fun extractCppModule(module: Module): CppModule? =
        module.languageData?.let {
            when(it) {
                is CppModule -> it
                else -> null
            }
        }
}
