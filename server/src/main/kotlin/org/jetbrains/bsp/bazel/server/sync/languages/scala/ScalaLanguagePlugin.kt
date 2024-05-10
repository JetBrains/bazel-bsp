package org.jetbrains.bsp.bazel.server.sync.languages.scala

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.ScalaBuildTarget
import ch.epfl.scala.bsp4j.ScalaMainClass
import ch.epfl.scala.bsp4j.ScalaMainClassesItem
import ch.epfl.scala.bsp4j.ScalaPlatform
import ch.epfl.scala.bsp4j.ScalaTestClassesItem
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.dependencygraph.DependencyGraph
import org.jetbrains.bsp.bazel.server.model.BspMappings
import org.jetbrains.bsp.bazel.server.sync.languages.JVMLanguagePluginParser
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.model.Language
import org.jetbrains.bsp.bazel.server.model.Module
import org.jetbrains.bsp.bazel.server.model.Tag
import java.net.URI
import java.nio.file.Path

class ScalaLanguagePlugin(
    private val javaLanguagePlugin: JavaLanguagePlugin,
    private val bazelPathsResolver: BazelPathsResolver
) : LanguagePlugin<ScalaModule>() {

    var scalaSdks: Map<String, ScalaSdk> = emptyMap()

    override fun prepareSync(targets: Sequence<BspTargetInfo.TargetInfo>) {
        scalaSdks = targets.associateBy(
            { it.id },
            ScalaSdkResolver(bazelPathsResolver)::resolveSdk
        ).filterValuesNotNull()
    }

    private fun <K, V> Map<K, V?>.filterValuesNotNull(): Map<K, V> =
        filterValues { it != null }.mapValues { it.value!! }

    override fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): ScalaModule? {
        if (!targetInfo.hasScalaTargetInfo()) {
            return null
        }
        val scalaTargetInfo = targetInfo.scalaTargetInfo
        val sdk = scalaSdks[targetInfo.id] ?: return null
        val scalacOpts = scalaTargetInfo.scalacOptsList
        return ScalaModule(sdk, scalacOpts, javaLanguagePlugin.resolveModule(targetInfo))
    }

    override fun dependencySources(
        targetInfo: BspTargetInfo.TargetInfo,
        dependencyGraph: DependencyGraph
    ): Set<URI> =
        javaLanguagePlugin.dependencySources(targetInfo, dependencyGraph)

    override fun applyModuleData(moduleData: ScalaModule, buildTarget: BuildTarget) {
        val scalaBuildTarget = with(moduleData.sdk) {
            ScalaBuildTarget(
                organization,
                version,
                binaryVersion,
                ScalaPlatform.JVM,
                compilerJars.map { it.toString() }.toList()
            )
        }
        moduleData.javaModule?.let(javaLanguagePlugin::toJvmBuildTarget)?.let {
            scalaBuildTarget.jvmBuildTarget = it
        }
        buildTarget.dataKind = BuildTargetDataKind.SCALA
        buildTarget.data = scalaBuildTarget
    }

    override fun calculateSourceRoot(source: Path): Path? {
        return JVMLanguagePluginParser.calculateJVMSourceRoot(source, true)
    }

    fun toScalaTestClassesItem(module: Module): ScalaTestClassesItem? =
        if (!module.tags.contains(Tag.TEST) || !module.languages
                .contains(Language.SCALA)
        )  null
        else withScalaAndJavaModules(module) { _, javaModule: JavaModule ->
            val mainClasses: List<String> = listOfNotNull(javaModule.mainClass)
            val id = BspMappings.toBspId(module)
            ScalaTestClassesItem(id, mainClasses)
        }

    fun toScalaMainClassesItem(module: Module): ScalaMainClassesItem? =
        if (!module.tags.contains(Tag.APPLICATION) || !module.languages
                .contains(Language.SCALA)
        ) null
        else withScalaAndJavaModulesOpt(module) { _, javaModule: JavaModule ->
            javaModule.mainClass?.let { mainClass: String ->
                val id = BspMappings.toBspId(module)
                val args = javaModule.args
                val jvmOpts = javaModule.jvmOps
                val scalaMainClass = ScalaMainClass(mainClass, args.toList(), jvmOpts.toList())
                val mainClasses = listOf(scalaMainClass)
                ScalaMainClassesItem(id, mainClasses)
            }
        }

    private fun <T> withScalaAndJavaModules(
        module: Module,
        f: (ScalaModule, JavaModule) -> T
    ): T? = getScalaAndJavaModules(module)?.let { (a, b) -> f(a, b) }

    private fun <T> withScalaAndJavaModulesOpt(
        module: Module,
        f: (ScalaModule, JavaModule) -> T?
    ): T? = getScalaAndJavaModules(module)?.let { (a, b) -> f(a, b) }

    private fun getScalaAndJavaModules(module: Module): Pair<ScalaModule, JavaModule>? =
        (module.languageData as? ScalaModule)?.let { scala ->
            scala.javaModule?.let { Pair(scala, it) }
        }
}
