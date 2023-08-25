package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path


class DotBazelBspDirPathSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): Try<DotBazelBspDirPathSpec> tests")
    inner class MapTest {

        @Test
        fun `should return success with deducted dir path skipping project view`() {
            // given
            val projectView = ProjectView.Builder().build().get()

            // when
            val dotBazelBspDirPathSpecTry = DotBazelBspDirPathSpecMapper.map(projectView)

            // then
            dotBazelBspDirPathSpecTry.isSuccess shouldBe true
            val dotBazelBspDirPathSpec = dotBazelBspDirPathSpecTry.get()

            val expectedDotBazelBspDirPathSpec = DotBazelBspDirPathSpec(Path("").toAbsolutePath().resolve(".bazelbsp"))
            dotBazelBspDirPathSpec shouldBe expectedDotBazelBspDirPathSpec
        }
    }

    @Nested
    @DisplayName("fun default(): Try<DotBazelBspDirPathSpec> tests")
    inner class DefaultTest {

        @Test
        fun `should return success with deducted dir path`() {
            // given
            // when
            val dotBazelBspDirPathSpecTry = DotBazelBspDirPathSpecMapper.default()

            // then
            dotBazelBspDirPathSpecTry.isSuccess shouldBe true
            val dotBazelBspDirPathSpec = dotBazelBspDirPathSpecTry.get()

            val expectedDotBazelBspDirPathSpec = DotBazelBspDirPathSpec(Path("").toAbsolutePath().resolve(".bazelbsp"))
            dotBazelBspDirPathSpec shouldBe expectedDotBazelBspDirPathSpec
        }
    }
}
