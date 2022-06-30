package org.jetbrains.bsp.bazel.server.bloop

import bloop.config.Config
import io.kotest.matchers.shouldBe
import io.vavr.collection.Array
import io.vavr.collection.HashSet
import io.vavr.control.Option
import io.vavr.collection.HashMap
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.Jdk
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaSdk
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toList

class BspModuleExporterTest {
    @Test
    fun `export bsp library module`() {
        val baseDirectory = Paths.get("base/").toAbsolutePath()
        val bloopRoot = baseDirectory.resolve("bloop/").toAbsolutePath()
        val bspModule = org.jetbrains.bsp.bazel.server.sync.model.Module(
            Label("//my/target"),
            false,
            Array.of(Label("dep1")),
            HashSet.of(Language.SCALA),
            HashSet.of(Tag.LIBRARY),
            baseDirectory.toUri(),
            SourceSet(
                HashSet.of(baseDirectory.resolve("file1.scala").toUri()),
                HashSet.of(baseDirectory.toUri())
            ),
            HashSet.empty(),
            HashSet.empty(),
            Option.of(
                ScalaModule(
                    ScalaSdk("scala", "2.12.15", "2.12.15", Array.empty()),
                    Array.empty(),
                    Option.of(
                        JavaModule(
                            Jdk("11", Option.none()),
                            Option.none(),
                            Array.empty(),
                            Array.empty(),
                            baseDirectory.toUri(),
                            Array.empty(),
                            Option.none(),
                            Array.empty(),
                            Array.empty(),
                            Array.of(baseDirectory.resolve("cp1").toUri()),
                            Array.empty(),
                            Array.empty()
                        )
                    )
                )
            )
        )
        val project = Project(
            baseDirectory.toUri(),
            Array.of(bspModule),
            HashMap.empty()
        )
        val classPathRewriter = ClasspathRewriter(HashMap.empty())
        val soureSetRewriter = SourceSetRewriter(HashSet.empty())
        val exporter = BspModuleExporter(
            project,
            bspModule,
            bloopRoot,
            classPathRewriter,
            soureSetRewriter,
            Option.none()
        )
        val ret = exporter.export()

        ret shouldBe Config.Project(
            "my/target",
            baseDirectory,
            scala.Option.apply(baseDirectory),
            emptyList(),
            scala.Option.apply(toList(
                Config.SourcesGlobs(
                    baseDirectory,
                    scala.Option.apply(1),
                    toList("glob:*.scala"),
                    emptyList()
                )
            )),
            scala.Option.apply(toList(Array.of(baseDirectory))),
            emptyList(),
            toList(baseDirectory.resolve("cp1")),
            bloopRoot.resolve("z_73CA8EDE4AC5"),
            bloopRoot.resolve("z_73CA8EDE4AC5/classes"),
            scala.Option.apply(emptyList()),
            scala.Option.apply(Config.Scala(
                "scala",
                "scala-compiler",
                "2.12.15",
                emptyList(),
                emptyList(),
                scala.Option.empty(),
                scala.Option.apply(Config.CompileSetup(Config.`Mixed$`.`MODULE$`, true, false, false, true, true))
            )),
            scala.Option.apply(Config.Java(emptyList())),
            scala.Option.empty(),
            scala.Option.empty(),
            scala.Option.apply(bloop.config.`Config$Platform$Jvm`(
                Config.JvmConfig(
                    scala.Option.empty(),
                    toList("-Duser.dir=${baseDirectory}")
                ),
                scala.Option.empty(),
                scala.Option.apply(Config.JvmConfig(
                    scala.Option.empty(),
                    toList("-Duser.dir=${baseDirectory}")
                )),
                scala.Option.apply(emptyList()),
                scala.Option.empty()
            )),
            scala.Option.apply(Config.Resolution(emptyList())),
            scala.Option.apply(toList("library"))
        )
    }
}
