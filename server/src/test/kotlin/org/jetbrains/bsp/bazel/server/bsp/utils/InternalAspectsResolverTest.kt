package org.jetbrains.bsp.bazel.server.bsp.utils

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
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
        aspectLabel shouldBe "@bazelbsp_aspect//aspects:core.bzl%get_classpath"
    }

    @Test
    fun `should resolve label for bsp root in subdirectory of workspace`() {
        // given
        val bspProjectRoot = "/Users/user/workspace/test-project/bsp-projects/test-project-bsp"

        // when
        val internalAspectsResolver = createAspectsResolver(bspProjectRoot)
        val aspectLabel = internalAspectsResolver.resolveLabel("get_classpath")

        // then
        aspectLabel shouldBe "@bazelbsp_aspect//aspects:core.bzl%get_classpath"
    }

    @Test
    fun `should resolve label differently for bazel version leq 5 and geq 6`() {
        // given
        val bspProjectRoot = "/Users/user/workspace/test-project/bsp-projects/test-project-bsp"

        // when
        val internalAspectsResolverVer5 = createAspectsResolver(bspProjectRoot, BazelRelease(5))
        val internalAspectsResolverVer6 = createAspectsResolver(bspProjectRoot, BazelRelease(6))
        val aspectLabelVer5 = internalAspectsResolverVer5.resolveLabel("get_classpath")
        val aspectLabelVer6 = internalAspectsResolverVer6.resolveLabel("get_classpath")

        // then
        aspectLabelVer5 shouldBe "@bazelbsp_aspect//aspects:core.bzl%get_classpath"
        aspectLabelVer6 shouldBe "@@bazelbsp_aspect//aspects:core.bzl%get_classpath"
    }

    private fun createAspectsResolver(
            bspProjectRoot: String,
            bazelRelease: BazelRelease = BazelRelease(5)
    ): InternalAspectsResolver =
        InternalAspectsResolver(BspInfo(Paths.get(bspProjectRoot)), bazelRelease)
}
