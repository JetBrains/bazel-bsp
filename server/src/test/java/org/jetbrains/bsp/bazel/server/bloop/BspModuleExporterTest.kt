package org.jetbrains.bsp.bazel.server.bloop

import bloop.config.Config
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyOption
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.scalaListOf
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toOption
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toScalaList
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.Jdk
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaSdk
import org.jetbrains.bsp.bazel.server.sync.model.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList as emptyScalaList

class BspModuleExporterTest {
    @Test
    fun `export bsp library module`() {
        val baseDirectory = Paths.get("base/").toAbsolutePath()
        val bloopRoot = baseDirectory.resolve("bloop/").toAbsolutePath()
        val moduleLabel = Label("//my/target")

        val bspModule = Module(
            moduleLabel,
            false,
            listOf(Label("dep1")),
            hashSetOf(Language.SCALA),
            hashSetOf(Tag.LIBRARY),
            baseDirectory.toUri(),
            SourceSet(
                hashSetOf(baseDirectory.resolve("file1.scala").toUri()),
                hashSetOf(baseDirectory.toUri())
            ),
            emptySet(),
            emptySet(),
            emptySet(),
            ScalaModule(
                ScalaSdk("scala", "2.12.15", "2.12.15", emptyList()), emptyList(), JavaModule(
                    Jdk("11", null),
                    null,
                    emptyList(),
                    emptyList(),
                    baseDirectory.toUri(),
                    emptyList(),
                    null,
                    emptyList(),
                    emptyList(),
                    listOf(baseDirectory.resolve("cp1").toUri()),
                    emptyList(),
                    emptyList(),
                )
            ),
            hashMapOf()
        )
        val project = Project(
            baseDirectory.toUri(), listOf(bspModule), emptyMap()
        )
        val classPathRewriter = ClasspathRewriter(emptyMap())
        val soureSetRewriter = SourceSetRewriter(emptySet())
        val exporter = BspModuleExporter(
            project, bspModule, bloopRoot, classPathRewriter, soureSetRewriter, null
        )
        val ret = exporter.export()
        val out = bloopRoot.resolve(Naming.compilerOutputNameFor(moduleLabel))
        ret shouldBe Config.Project(
            "my/target",
            baseDirectory,
            scala.Option.apply(baseDirectory),
            emptyScalaList(),
            scalaListOf(
                Config.SourcesGlobs(
                    baseDirectory,
                    scala.Option.apply(1),
                    scalaListOf("glob:*.scala"),
                    emptyScalaList()
                )
            ).toOption(),
            baseDirectory.toScalaList().toOption(),
            emptyScalaList(),
            scalaListOf(baseDirectory.resolve("cp1")),
            out,
            out.resolve("classes"),
            emptyScalaList<Path>().toOption(),
            Config.Scala(
                "scala",
                "scala-compiler",
                "2.12.15",
                emptyScalaList(),
                emptyScalaList(),
                emptyOption(),
                Config.CompileSetup(
                    Config.`Mixed$`.`MODULE$`, true, false, false, true, true
                ).toOption()
            ).toOption(),
            Config.Java(emptyScalaList()).toOption(),
            emptyOption(),
            emptyOption(),
            bloop.config.`Config$Platform$Jvm`(
                Config.JvmConfig(
                    emptyOption(), scalaListOf("-Duser.dir=${baseDirectory}")
                ), emptyOption(), scala.Option.apply(
                    Config.JvmConfig(
                        emptyOption(), scalaListOf("-Duser.dir=${baseDirectory}")
                    )
                ), emptyScalaList<Path>().toOption(), emptyOption()
            ).toOption(),
            scala.Option.apply(Config.Resolution(emptyScalaList())),
            scala.Option.apply(scalaListOf("library"))
        )
    }
}
