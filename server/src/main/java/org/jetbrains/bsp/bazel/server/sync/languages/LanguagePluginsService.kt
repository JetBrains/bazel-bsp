package org.jetbrains.bsp.bazel.server.sync.languages

import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module

class LanguagePluginsService(
    val scalaLanguagePlugin: ScalaLanguagePlugin,
    val javaLanguagePlugin: JavaLanguagePlugin,
    private val cppLanguagePlugin: CppLanguagePlugin,
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
        if (languages.contains(Language.SCALA)) {
            scalaLanguagePlugin
        } else if (languages.contains(Language.JAVA) || languages.contains(
                Language.KOTLIN
            )
        ) {
            javaLanguagePlugin
            // TODO https://youtrack.jetbrains.com/issue/BAZEL-25
            //    } else if (languages.contains(Language.CPP)) {
            //      return cppLanguagePlugin;
        } else if (languages.contains(Language.THRIFT)) {
            thriftLanguagePlugin
        } else {
            emptyLanguagePlugin
        }

    fun extractJavaModule(module: Module): JavaModule? =
        module.languageData?.let {
            when (it) {
                is JavaModule -> it
                is ScalaModule -> it.javaModule
                else -> null
            }
        }
}
