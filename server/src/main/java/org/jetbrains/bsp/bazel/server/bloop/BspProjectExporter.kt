package org.jetbrains.bsp.bazel.server.bloop

import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.jvm.javaModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

internal class BspProjectExporter(private val project: Project, private val bloopRoot: Path) {
    fun export(): Set<Path> {
        val bloopProjectWriter = BloopProjectWriter(bloopRoot)
        val classpathRewriter = buildClassPathRewriter()
        val sourceSetRewriter = SourceSetRewriter(IGNORED_SOURCES)
        val anyScalaModule = project
            .modules.map {
                ScalaModule.fromLanguageData(it.languageData)
            }.firstOrNull()
        return project
            .modules
            .map{
                    BspModuleExporter(
                        project,
                        it,
                        bloopRoot,
                        classpathRewriter,
                        sourceSetRewriter,
                        anyScalaModule
                    ).export()
                }
            .map(bloopProjectWriter::write)
            .toSet()
    }

    private fun buildClassPathRewriter(): ClasspathRewriter {
        val localArtifactsBuilder = project.modules.flatMap {
            val moduleOutput = classesOutputForModule(it, bloopRoot)
            artifactsFromLanguageData(it).map { uri ->
                uri to moduleOutput
            }
        }.toMap()
        return ClasspathRewriter(localArtifactsBuilder)
    }

    private fun artifactsFromLanguageData(module: Module): Set<URI> {
        return module.javaModule
            ?.let(JavaModule::allOutputs)
            ?.toSet().orEmpty()
    }

    private fun classesOutputForModule(mod: Module, bloopRoot: Path): URI {
        return bloopRoot.resolve(Naming.compilerOutputNameFor(mod.label)).resolve("classes")
            .toUri()
    }

    companion object {
        private val IGNORED_SOURCES: Set<Path> = hashSetOf(
            Paths.get(
                "tools/src/main/scala/com/twitter/bazel/resources_workaround/placeholder.scala"
            )
        )
    }
}
