package org.jetbrains.bsp.bazel.server.bsp.utils

import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class InternalAspectsResolverTest {
    
    @Test
    fun `should resolve label for bsp root at workspace root`() {
        // given
        val bspProjectRoot = "/Users/user/workspace/test-project"

        // when
        val internalAspectsResolver = createAspectsResolver(bspProjectRoot)
        val aspectLabel = internalAspectsResolver.resolveLabel("get_classpath")

        // then
        aspectLabel shouldBe "@bazelbsp_aspect//:aspects.bzl%get_classpath"
    }

    @Test
    fun `should resolve label for bsp root in subdirectory of workspace`() {
        // given
        val bspProjectRoot = "/Users/user/workspace/test-project/bsp-projects/test-project-bsp"

        // when
        val internalAspectsResolver = createAspectsResolver(bspProjectRoot)
        val aspectLabel = internalAspectsResolver.resolveLabel("get_classpath")

        // then
        aspectLabel shouldBe "@bazelbsp_aspect//:aspects.bzl%get_classpath"
    }

    private fun createAspectsResolver(bspProjectRoot: String): InternalAspectsResolver =
        InternalAspectsResolver(BspInfo(Paths.get(bspProjectRoot)))
}
