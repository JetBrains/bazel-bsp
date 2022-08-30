package org.jetbrains.bsp.bazel.server.bloop

import bloop.config.Config
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.scalaListOf
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.junit.jupiter.api.Test
import scala.jdk.javaapi.CollectionConverters
import java.nio.file.Paths

class ReGlobberTest {
    @Test
    fun `reglobs sources with one level`() {
        val basePath = Paths.get("base/path/")
        val sourceSet = SourceSet(
            hashSetOf(
                basePath.resolve(Paths.get("file1.scala")).toUri(),
                basePath.resolve(Paths.get("file2.scala")).toUri()
            ),
            emptySet()
        )
        val reglobbed = ReGlobber.reGlob(basePath.toUri(), sourceSet)

        CollectionConverters.asJava(reglobbed.sources).shouldBeEmpty()
        reglobbed.globs.get() shouldBe scalaListOf(
            Config.SourcesGlobs(
                basePath.toAbsolutePath(),
                scala.Some.apply(1),
                scalaListOf("glob:*.scala"),
                emptyList()
            )
        )
    }

    @Test
    fun `reglobs sources with two levels`() {
        val basePath = Paths.get("base/path/")
        val sourceSet = SourceSet(
            hashSetOf(
                basePath.resolve(Paths.get("file1.scala")).toUri(),
                basePath.resolve(Paths.get("child/file2.scala")).toUri()
            ),
            emptySet()
        )
        val reglobbed = ReGlobber.reGlob(basePath.toUri(), sourceSet)

        CollectionConverters.asJava(reglobbed.sources).shouldBeEmpty()
        reglobbed.globs.get() shouldBe scalaListOf(
            Config.SourcesGlobs(
                basePath.toAbsolutePath(),
                scala.Option.empty(),
                scalaListOf("glob:**.scala"),
                emptyList()
            )
        )
    }

    @Test
    fun `reglobs sources with one level and base path`() {
        val basePath = Paths.get("base/path/")
        val sourceSet = SourceSet(
            hashSetOf(
                basePath.resolve(Paths.get("src/main/child/file1.scala")).toUri(),
                basePath.resolve(Paths.get("src/main/child/file2.scala")).toUri()
            ),
            emptySet()
        )
        val reglobbed = ReGlobber.reGlob(basePath.toUri(), sourceSet)

        CollectionConverters.asJava(reglobbed.sources).shouldBeEmpty()
        reglobbed.globs.get() shouldBe scalaListOf(
            Config.SourcesGlobs(
                basePath.resolve("src/main/child").toAbsolutePath(),
                scala.Option.apply(1),
                scalaListOf("glob:*.scala"),
                emptyList()
            )
        )
    }

    @Test
    fun `reglobs sources with two levels and base path`() {
        val basePath = Paths.get("base/path/")
        val sourceSet = SourceSet(
            hashSetOf(
                basePath.resolve(Paths.get("src/main/child/dir1/file1.scala")).toUri(),
                basePath.resolve(Paths.get("src/main/child/file2.scala")).toUri()
            ),
            emptySet()
        )
        val reglobbed = ReGlobber.reGlob(basePath.toUri(), sourceSet)

        CollectionConverters.asJava(reglobbed.sources).shouldBeEmpty()
        reglobbed.globs.get() shouldBe scalaListOf(
            Config.SourcesGlobs(
                basePath.resolve("src/main/child").toAbsolutePath(),
                scala.Option.empty(),
                scalaListOf("glob:**.scala"),
                emptyList()
            )
        )
    }
}
