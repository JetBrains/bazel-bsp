package org.jetbrains.bsp.bazel.server.bloop

import bloop.config.Config
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import io.vavr.collection.HashSet
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList
import org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toList
import scala.jdk.javaapi.CollectionConverters

class ReGlobberTest {
    @Test
    fun `reglobs sources with one level`() {
        val basePath = Paths.get("base/path/")
        val sourceSet = SourceSet(
            HashSet.of(
                basePath.resolve(Paths.get("file1.scala")).toUri(),
                basePath.resolve(Paths.get("file2.scala")).toUri()
            ),
            HashSet.empty()
        )
        val reglobbed = ReGlobber.reGlob(basePath.toUri(), sourceSet)

        CollectionConverters.asJava(reglobbed.sources).shouldBeEmpty()
        reglobbed.globs.get() shouldBe toList(
            Config.SourcesGlobs(
                basePath.toAbsolutePath(),
                scala.Some.apply(1),
                toList("glob:*.scala"),
                emptyList()
            )
        )
    }

    @Test
    fun `reglobs sources with two levels`() {
        val basePath = Paths.get("base/path/")
        val sourceSet = SourceSet(
            HashSet.of(
                basePath.resolve(Paths.get("file1.scala")).toUri(),
                basePath.resolve(Paths.get("child/file2.scala")).toUri()
            ),
            HashSet.empty()
        )
        val reglobbed = ReGlobber.reGlob(basePath.toUri(), sourceSet)

        CollectionConverters.asJava(reglobbed.sources).shouldBeEmpty()
        reglobbed.globs.get() shouldBe toList(
            Config.SourcesGlobs(
                basePath.toAbsolutePath(),
                scala.Option.empty(),
                toList("glob:**.scala"),
                emptyList()
            )
        )
    }
}
