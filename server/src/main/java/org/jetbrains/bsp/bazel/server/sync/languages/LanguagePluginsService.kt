package org.jetbrains.bsp.bazel.server.sync.languages

import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.jvm.JvmLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module

class LanguagePluginsService(
    val scalaLanguagePlugin: ScalaLanguagePlugin,
    val javaLanguagePlugin: JavaLanguagePlugin,
    val cppLanguagePlugin: CppLanguagePlugin,
    jvmLanguagePlugin: JvmLanguagePlugin,
    private val thriftLanguagePlugin: ThriftLanguagePlugin
) {
    private val emptyLanguagePlugin: EmptyLanguagePlugin = EmptyLanguagePlugin()

    private val allPlugins = listOf(
            scalaLanguagePlugin,
            javaLanguagePlugin,
            cppLanguagePlugin,
            thriftLanguagePlugin,
            jvmLanguagePlugin,
    )

    fun prepareSync(targetInfos: Sequence<BspTargetInfo.TargetInfo>) {
        allPlugins.forEach { it.prepareSync(targetInfos) }
    }

    fun postProcessModules(modules: List<Module>): List<Module> {
        return allPlugins.fold(modules) { modules, plugin -> plugin.postProcessModules(modules) }
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
