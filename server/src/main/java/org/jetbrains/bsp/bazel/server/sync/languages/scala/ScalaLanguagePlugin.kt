package org.jetbrains.bsp.bazel.server.sync.languages.scala

import com.jetbrains.bsp.bsp4kt.BuildTarget
import com.jetbrains.bsp.bsp4kt.BuildTargetDataKind
import com.jetbrains.bsp.bsp4kt.ScalaBuildTarget
import com.jetbrains.bsp.bsp4kt.ScalaMainClass
import com.jetbrains.bsp.bsp4kt.ScalaMainClassesItem
import com.jetbrains.bsp.bsp4kt.ScalaPlatform
import com.jetbrains.bsp.bsp4kt.ScalaTestClassesItem
import com.jetbrains.bsp.bsp4kt.ScalacOptionsItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.JVMLanguagePluginParser
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import java.net.URI
import java.nio.file.Path

class ScalaLanguagePlugin(
    private val javaLanguagePlugin: JavaLanguagePlugin,
    private val bazelPathsResolver: BazelPathsResolver
) : LanguagePlugin<ScalaModule>() {

    private var scalaSdk: ScalaSdk? = null

    override fun prepareSync(targets: Sequence<BspTargetInfo.TargetInfo>) {
        scalaSdk = ScalaSdkResolver(bazelPathsResolver).resolve(targets)
    }

    override fun resolveModule(targetInfo: BspTargetInfo.TargetInfo): ScalaModule? {
        if (!targetInfo.hasScalaTargetInfo()) {
            return null
        }
        val scalaTargetInfo = targetInfo.scalaTargetInfo
        val sdk = getScalaSdk()
        val scalacOpts = scalaTargetInfo.scalacOptsList
        return ScalaModule(sdk, scalacOpts, javaLanguagePlugin.resolveModule(targetInfo))
    }

    private fun getScalaSdk(): ScalaSdk =
        scalaSdk ?: throw RuntimeException("Failed to resolve Scala SDK for project")

    override fun dependencySources(
        targetInfo: BspTargetInfo.TargetInfo,
        dependencyTree: DependencyTree
    ): Set<URI> =
        javaLanguagePlugin.dependencySources(targetInfo, dependencyTree)

    override fun applyModuleData(buildTarget: BuildTarget, moduleData: ScalaModule): BuildTarget {
        val jvmBuildTarget = moduleData.javaModule?.let(javaLanguagePlugin::toJvmBuildTarget)
        val scalaBuildTarget = with(moduleData.sdk) {
            ScalaBuildTarget(
                organization,
                version,
                binaryVersion,
                ScalaPlatform.Jvm, // TODO: Change this when we support other platforms
                compilerJars.map { it.toString() }.toList(),
                jvmBuildTarget,
            )
        }
        val data = Json.encodeToJsonElement(scalaBuildTarget)
        return buildTarget.copy(
            dataKind = BuildTargetDataKind.Scala,
            data = data
        )
    }

    override fun calculateSourceRoot(source: Path): Path? {
        return JVMLanguagePluginParser.calculateJVMSourceRoot(source, true)
    }

    fun toScalacOptionsItem(module: Module): ScalacOptionsItem? =
        withScalaAndJavaModules(module) { scalaModule: ScalaModule, javaModule: JavaModule ->
            val javacOptions = javaLanguagePlugin.toJavacOptionsItem(module, javaModule)
            ScalacOptionsItem(
                javacOptions.target,
                scalaModule.scalacOpts,
                javacOptions.classpath,
                javacOptions.classDirectory
            )
        }

    fun toScalaTestClassesItem(module: Module): ScalaTestClassesItem? =
        if (!module.tags.contains(Tag.TEST) || !module.languages
                .contains(Language.SCALA)
        )  null
        else withScalaAndJavaModules(module) { _, javaModule: JavaModule ->
            val mainClasses: List<String> = listOfNotNull(javaModule.mainClass)
            val id = BspMappings.toBspId(module)
            ScalaTestClassesItem(id, null, mainClasses)
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
