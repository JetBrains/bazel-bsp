package org.jetbrains.bsp.bazel.server.sync.languages.java

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.JavacOptionsItem
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.JvmEnvironmentItem
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BspMappings
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
    private var jdk: Jdk? = null

    override fun prepareSync(targets: Sequence<TargetInfo>) {
        jdk = jdkResolver.resolve(targets)
    }

    override fun resolveModule(targetInfo: TargetInfo): JavaModule? =
        targetInfo.takeIf(TargetInfo::hasJavaTargetInfo)?.javaTargetInfo?.run {
            val mainOutput = bazelPathsResolver.resolveUri(getJars(0).getBinaryJars(0))
            val allOutputs = bazelPathsResolver.resolveUris(jarsList.flatMap { it.interfaceJarsList + it.binaryJarsList })
            val mainClass = mainClass.takeUnless { it.isBlank() }
            val runtimeClasspath = bazelPathsResolver.resolveUris(runtimeClasspathList)
            val compileClasspath = bazelPathsResolver.resolveUris(compileClasspathList)
            val sourcesClasspath = bazelPathsResolver.resolveUris(sourceClasspathList)
            val ideClasspath = resolveIdeClasspath(runtimeClasspath, compileClasspath)
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

    private fun getJdk(): Jdk = jdk ?: throw RuntimeException("Failed to resolve JDK for project")

    private fun resolveIdeClasspath(runtimeClasspath: List<URI>, compileClasspath: List<URI>): List<URI> {
        return IdeClasspathResolver(
                runtimeClasspath.asSequence(),
                compileClasspath.asSequence()
        ).resolve().toList()
    }

    override fun applyModuleData(moduleData: JavaModule, buildTarget: BuildTarget) {
        val jvmBuildTarget = toJvmBuildTarget(moduleData)
        buildTarget.dataKind = BuildTargetDataKind.JVM
        buildTarget.data = jvmBuildTarget
    }

    fun toJvmBuildTarget(javaModule: JavaModule): JvmBuildTarget {
        val jdk = javaModule.jdk
        val javaHome = jdk.javaHome?.let(URI::toString)
        return JvmBuildTarget(javaHome, jdk.version)
    }

    fun toJvmEnvironmentItem(module: Module, javaModule: JavaModule): JvmEnvironmentItem =
        JvmEnvironmentItem(
            BspMappings.toBspId(module),
            javaModule.runtimeClasspath.map(URI::toString),
            javaModule.jvmOps,
            bazelInfo.workspaceRoot.toString(),
            module.environmentVariables
        )

    fun toJavacOptionsItem(module: Module, javaModule: JavaModule): JavacOptionsItem =
        JavacOptionsItem(
            BspMappings.toBspId(module),
            javaModule.javacOpts,
            javaModule.ideClasspath.map(URI::toString),
            javaModule.mainOutput.toString()
        )
}
