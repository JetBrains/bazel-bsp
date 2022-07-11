package org.jetbrains.bsp.bazel.server.bloop

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths


class SourceSetRewriterTest {
    @Test
    fun `passes through source sets without invalid entries`() {
        val rewriter = SourceSetRewriter(hashSetOf(Paths.get("a/bad/path.scala")))
        val sourceSet = SourceSet(
            hashSetOf(URI.create("file:///some/path/some.file")),
            emptySet()
        )
        val ret = rewriter.rewrite(sourceSet)
        ret shouldBe sourceSet
    }

    @Test
    fun `passes through source sets with invalid entry and valid entry`() {
        val rewriter = SourceSetRewriter(hashSetOf(Paths.get("a/bad/path.scala")))
        val sourceSet = SourceSet(
            hashSetOf(URI.create("file:///some/path/some.file"), URI.create("file:///some/path/a/bad/path.scala")),
            emptySet()
        )
        val ret = rewriter.rewrite(sourceSet)
        ret shouldBe sourceSet
    }

    @Test
    fun `rewrites source sets with invalid entries`() {
        val rewriter = SourceSetRewriter(hashSetOf(Paths.get("a/bad/path.scala")))
        val sourceSet = SourceSet(
            hashSetOf(URI.create("file:///some/path/a/bad/path.scala")),
            emptySet()
        )
        val ret = rewriter.rewrite(sourceSet)
        ret shouldBe SourceSet(emptySet(), emptySet())
    }
}
