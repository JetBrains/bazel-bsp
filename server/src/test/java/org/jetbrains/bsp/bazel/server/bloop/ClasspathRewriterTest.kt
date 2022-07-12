package org.jetbrains.bsp.bazel.server.bloop

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ClasspathRewriterTest {
    @Test
    fun `rewrites classpath`() {
        val root = Paths.get("root/")
        val localRoot = Paths.get("local-root/")
        val artifacts = mapOf(
            root.resolve("artifact-1").toUri() to localRoot.resolve("local-artifact-1").toUri()
        )

        val rewriter = ClasspathRewriter(artifacts)
        val ret = rewriter.rewrite(
            listOf(
                root.resolve("artifact-1").toUri(),
                root.resolve("artifact-2").toUri()
            )
        )

        ret.shouldContainExactly(
            localRoot.resolve("local-artifact-1").toAbsolutePath(),
            root.resolve("artifact-2").toAbsolutePath()
        )
    }
}
