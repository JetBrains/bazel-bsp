package org.jetbrains.bsp.bazel.server.sync.languages.java

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.JavacOptionsItem
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.JvmEnvironmentItem
import ch.epfl.scala.bsp4j.JvmMainClass
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JavaTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JvmOutputsOrBuilder
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree
import org.jetbrains.bsp.bazel.server.sync.languages.JVMLanguagePluginParser
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Module
import java.net.URI
import java.nio.file.Path

class JavaLanguagePlugin(
    private val bazelPathsResolver: BazelPathsResolver,
    private val jdkResolver: JdkResolver,
    private val bazelInfo: BazelInfo
) : LanguagePlugin<JavaModule>() {
    private val environment = System.getenv()
    private var jdk: Jdk? = null

    override fun prepareSync(targets: Sequence<TargetInfo>) {
        jdk = jdkResolver.resolve(targets)
    }

    override fun resolveModule(targetInfo: TargetInfo): JavaModule? =
        targetInfo.takeIf(TargetInfo::hasJavaTargetInfo)?.javaTargetInfo?.run {
            val mainOutput = bazelPathsResolver.resolveUri(getJars(0).getBinaryJars(0))
            val allOutputs = jarsList.flatMap {
                it.interfaceJarsList + it.binaryJarsList
            }.map(bazelPathsResolver::resolveUri)
            val mainClass = getMainClass(this)
            val runtimeClasspath = bazelPathsResolver.resolveUris(runtimeClasspathList)
            val compileClasspath = bazelPathsResolver.resolveUris(compileClasspathList)
            val sourcesClasspath = bazelPathsResolver.resolveUris(sourceClasspathList)
            val ideClasspath = resolveIdeClasspath(
                runtimeClasspath.asSequence(), compileClasspath.asSequence()
            )
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
                runtimeClasspath,
                compileClasspath,
                sourcesClasspath,
                ideClasspath
            )
        }


    override fun calculateSourceRoot(source: Path): Path =
        JVMLanguagePluginParser.calculateJVMSourceRoot(source)

    private fun getMainClass(javaTargetInfo: JavaTargetInfo): String? =
        javaTargetInfo.mainClass.takeUnless { javaTargetInfo.mainClass.isBlank() }

    private fun getJdk(): Jdk = jdk ?: throw RuntimeException("Failed to resolve JDK for project")

    private fun resolveIdeClasspath(
        runtimeClasspath: Sequence<URI>, compileClasspath: Sequence<URI>
    ): List<URI> = IdeClasspathResolver(runtimeClasspath, compileClasspath).resolve().toList()

    override fun dependencySources(
        targetInfo: TargetInfo, dependencyTree: DependencyTree
    ): Set<URI> =
        targetInfo.getJavaTargetInfoOrNull()?.run {
            dependencyTree.transitiveDependenciesWithoutRootTargets(targetInfo.id)
                .flatMap(::getSourceJars)
                .map(bazelPathsResolver::resolveUri)
                .toSet()
        }.orEmpty()

    private fun getSourceJars(targetInfo: TargetInfo): List<FileLocation> =
        targetInfo.getJavaTargetInfoOrNull()
            ?.run {  jarsOrBuilderList + generatedJarsList }
            ?.flatMap(JvmOutputsOrBuilder::getSourceJarsList)
            .orEmpty()

    private fun TargetInfo.getJavaTargetInfoOrNull(): JavaTargetInfo? =
        this.takeIf(TargetInfo::hasJavaTargetInfo)?.javaTargetInfo

    override fun applyModuleData(moduleData: JavaModule, buildTarget: BuildTarget) {
        val jvmBuildTarget = toJvmBuildTarget(moduleData)
        buildTarget.dataKind = BuildTargetDataKind.JVM
        buildTarget.data = jvmBuildTarget
    }

    fun toJvmBuildTarget(javaModule: JavaModule): JvmBuildTarget {
        val jdk = javaModule.jdk
        val javaHome = jdk.javaHome?.let { obj: URI -> obj.toString() }
        return JvmBuildTarget(javaHome, jdk.version)
    }

    fun toJvmEnvironmentItem(module: Module, javaModule: JavaModule): JvmEnvironmentItem =
        JvmEnvironmentItem(
            BspMappings.toBspId(module),
            javaModule.runtimeClasspath.map { obj: URI -> obj.toString() }.toList(),
            javaModule.jvmOps.toList(),
            bazelInfo.workspaceRoot.toString(),
            module.environmentVariables
        ).apply {
            mainClasses = javaModule.mainClass?.let { listOf(JvmMainClass(it, javaModule.args)) }.orEmpty()
        }
    // FIXME: figure out what we should pass here, because passing the environment
    // of the *SERVER* makes little sense

    fun toJavacOptionsItem(module: Module, javaModule: JavaModule): JavacOptionsItem =
        JavacOptionsItem(
            BspMappings.toBspId(module),
            javaModule.javacOpts.toList(),
            javaModule.ideClasspath.map { obj: URI -> obj.toString() }.toList(),
            javaModule.mainOutput.toString()
        )
}
