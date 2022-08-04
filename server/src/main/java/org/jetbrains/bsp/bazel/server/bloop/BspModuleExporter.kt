package org.jetbrains.bsp.bazel.server.bloop

import bloop.config.Config
import bloop.config.Config.*
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toListOption
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toOption
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toScalaList
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule.Companion.fromLanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import scala.Option
import scala.Some
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

class BspModuleExporter(
    private val project: Project,
    private val module: Module,
    private val bloopRoot: Path,
    private val classpathRewriter: ClasspathRewriter,
    private val sourceSetRewriter: SourceSetRewriter,
    private val defaultScalaModule: ScalaModule?
) {
    private val extraJvmOptions = listOf(
        "-Duser.dir=" + Paths.get(
            project.workspaceRoot
        ).toAbsolutePath()
    )

    fun export(): Config.Project {
        if (module.tags.contains(Tag.NO_BUILD)) {
            return createNoBuildModule()
        }
        val name = Naming.safeName(module.label)
        val directory = Paths.get(module.baseDirectory)
        val adjustedSourceSet = sourceSetRewriter.rewrite(
            module.sourceSet
        )
        val workspaceDir = project.workspaceRoot
        val sourceRoots = adjustedSourceSet.sourceRoots.toScalaList()
        val reGlobbed = ReGlobber.reGlob(module.baseDirectory, adjustedSourceSet)
        val dependencies =
            module.directDependencies.filter(::isIncludedDependency).map(Naming::safeName)
        val compileClassPath =
            module.languageData?.let(::extractCompileClassPathFromLanguage).orEmpty()
                .let(classpathRewriter::rewrite)
        val out = bloopRoot.resolve(
            Naming.compilerOutputNameFor(
                module.label
            )
        )
        val classesDir = out.resolve("classes")
        val resources = module.resources.map(SourceRootGuesser::getSourcesRoot).toSet()
        val scalaMod = module.languageData?.let(::createScalaConfig)
        val javaMod = module.languageData?.let(::createJavaConfig)
        val platform = module.languageData?.let(::createPlatform)
        val resolution = createResolution()
        val testFramework = createTestFramework()
        val tags = toBloopTags(module.tags)
        return Project(
            name,
            directory,
            Option.apply(Paths.get(workspaceDir)),
            reGlobbed.sources,
            reGlobbed.globs,
            sourceRoots.map(Paths::get).toOption(),
            dependencies.toScalaList(),
            compileClassPath.toScalaList(),
            out,
            classesDir,
            resources.map(Paths::get).toListOption(),
            scalaMod.toOption(),
            javaMod.toOption(),
            Option.empty(),
            testFramework.toOption(),
            platform.toOption(),
            resolution.toOption(),
            tags.toListOption()
        )
    }

    private fun createNoBuildModule(): Config.Project {
        val out = bloopRoot.resolve(
            Naming.compilerOutputNameFor(
                module.label
            )
        )
        val classesDir = out.resolve("classes")
        val resources = module.resources
        return Project(
            module.label.value,
            Paths.get(module.baseDirectory),
            Option.apply(Paths.get(project.workspaceRoot)),
            ScalaInterop.emptyList(),
            Option.empty(),
            Option.empty(),
            ScalaInterop.emptyList(),
            ScalaInterop.emptyList(),
            out,
            classesDir,
            resources.map(Paths::get).toListOption(),
            Option.empty(),
            Option.empty(),
            Option.empty(),
            Option.empty(),
            Option.empty(),
            Option.empty(),
            Option.empty()
        )
    }

    private fun extractCompileClassPathFromLanguage(languageData: LanguageData): List<URI> =
        fromLanguageData(languageData)?.let(JavaModule::compileClasspath).orEmpty()

    private fun isIncludedDependency(label: Label): Boolean = project.findModule(label) != null

    private fun createTestFramework(): Test? = module.tags.takeIf { it.contains(Tag.TEST) }?.let {
        val framework = TestFramework(
            setOf("munit.internal.junitinterface.PantsFramework").toScalaList()
        )
        Test(
            setOf(framework).toScalaList(), TestOptions(
                ScalaInterop.emptyList<String>(), ScalaInterop.emptyList<TestArgument>()
            )
        )
    }

    private fun toBloopTags(tags: Set<Tag>): Set<String> = if (tags.contains(Tag.TEST)) {
        hashSetOf("test")
    } else {
        hashSetOf("library")
    }

    private fun createJavaConfig(languageData: LanguageData): Java? =
        fromLanguageData(languageData)?.let {
            Java(sanitizeJavacOpts(it.javacOpts).toScalaList())
        }

    private fun sanitizeJavacOpts(opts: List<String>): List<String> =
        opts.filter { p: String -> !BAD_JAVAC_OPTS.contains(p) }

    private fun createScalaConfig(data: LanguageData): Scala? =
        (data as? ScalaModule)?.let(::createScalaConfig)
            ?: defaultScalaModule?.let(::createScalaConfig)

    private fun createScalaConfig(scalaModule: ScalaModule): Scala = Scala(
        scalaModule.sdk.organization,
        "scala-compiler",
        scalaModule.sdk.version,
        scalaModule.scalacOpts.toScalaList(),
        scalaModule.sdk.compilerJars.map(Paths::get).toScalaList(),
        Option.empty(),
        Some.apply(
            `CompileSetup$`.`MODULE$`.apply(
                `Mixed$`.`MODULE$`, true, false, false, true, true
            )
        )
    )

    private fun createPlatform(languageData: LanguageData): Platform? =
        fromLanguageData(languageData)?.run {
            val runtimeJdk = runtimeJdk ?: jdk
            val jvmConfig = JvmConfig(
                runtimeJdk.javaHome?.let(Paths::get).toOption(),
                (extraJvmOptions + jvmOps).toScalaList()
            )
            val runtimeClassPath = classpathRewriter.rewrite(runtimeClasspath).toScalaList()
            bloop.config.`Config$Platform$Jvm`(
                    jvmConfig,
                    mainClass.toOption(),
                    jvmConfig.toOption(),
                    runtimeClassPath.toOption(),
                    Option.empty()
                )
            }

    private fun createResolution(): Resolution = module.sourceDependencies.map { sourceDep ->
            val artifact = Artifact(
                "", Option.apply("sources"), Option.empty(), Paths.get(sourceDep)
            )
            Module(
                "", "", "", Option.empty(), setOf(artifact).toScalaList()
            )
        }.toScalaList().let(::Resolution)


    companion object {
        private val BAD_JAVAC_OPTS: Set<String> = hashSetOf(
            "-XepAllErrorsAsWarnings", "-Xep:PreconditionsInvalidPlaceholder:OFF", "-Werror:-path"
        )
    }
}
