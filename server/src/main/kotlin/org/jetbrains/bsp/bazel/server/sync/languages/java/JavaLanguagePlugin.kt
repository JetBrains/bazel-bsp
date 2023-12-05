package org.jetbrains.bsp.bazel.server.sync.languages.java

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.JvmBuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JvmOutputsOrBuilder
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JvmTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.JVMLanguagePluginParser
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import java.net.URI
import java.nio.file.Path

class JavaLanguagePlugin(
    private val bazelPathsResolver: BazelPathsResolver,
    private val jdkResolver: JdkResolver,
) : LanguagePlugin<JavaModule>() {
    private var jdk: Jdk? = null

    override fun prepareSync(targets: Sequence<TargetInfo>) {
        jdk = jdkResolver.resolve(targets)
    }

    override fun resolveModule(targetInfo: TargetInfo): JavaModule? =
        targetInfo.takeIf(TargetInfo::hasJvmTargetInfo)?.jvmTargetInfo?.run {
            val mainOutput = bazelPathsResolver.resolveUri(getJars(0).getBinaryJars(0))
            val allOutputs = jarsList.flatMap {
                it.interfaceJarsList + it.binaryJarsList
            }.map(bazelPathsResolver::resolveUri)
            val mainClass = getMainClass(this)
            val runtimeJdk = jdkResolver.resolveJdk(targetInfo)

            JavaModule(
                getJdk(),
                runtimeJdk,
                javacOptsList,
                jvmFlagsList,
                mainOutput,
                allOutputs,
                mainClass,
                argsList,
            )
        }


    override fun calculateSourceRoot(source: Path): Path =
        JVMLanguagePluginParser.calculateJVMSourceRoot(source)

    private fun getMainClass(jvmTargetInfo: JvmTargetInfo): String? =
        jvmTargetInfo.mainClass.takeUnless { jvmTargetInfo.mainClass.isBlank() }

    private fun getJdk(): Jdk = jdk ?: throw RuntimeException("Failed to resolve JDK for project")

    override fun dependencySources(
        targetInfo: TargetInfo, dependencyTree: DependencyTree
    ): Set<URI> =
        targetInfo.getJvmTargetInfoOrNull()?.run {
            dependencyTree.transitiveDependenciesWithoutRootTargets(targetInfo.id)
                .flatMap(::getSourceJars)
                .map(bazelPathsResolver::resolveUri)
                .toSet()
        }.orEmpty()

    private fun getSourceJars(targetInfo: TargetInfo): List<FileLocation> =
        targetInfo.getJvmTargetInfoOrNull()
            ?.run {  jarsOrBuilderList + generatedJarsList }
            ?.flatMap(JvmOutputsOrBuilder::getSourceJarsList)
            .orEmpty()

    private fun TargetInfo.getJvmTargetInfoOrNull(): JvmTargetInfo? =
        this.takeIf(TargetInfo::hasJvmTargetInfo)?.jvmTargetInfo

    override fun applyModuleData(moduleData: JavaModule, buildTarget: BuildTarget) {
        val jvmBuildTarget = toJvmBuildTarget(moduleData)
        buildTarget.dataKind = BuildTargetDataKind.JVM
        buildTarget.data = jvmBuildTarget
    }

    fun toJvmBuildTarget(javaModule: JavaModule): JvmBuildTarget {
        val jdk = javaModule.jdk
        val javaHome = jdk.javaHome?.toString()
        return JvmBuildTarget().also {
            it.javaVersion = jdk.version
            it.javaHome = javaHome
        }
    }
}
