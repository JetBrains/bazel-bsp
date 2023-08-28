package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path


class DotBazelBspDirPathSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): DotBazelBspDirPathSpec tests")
    inner class MapTest {

        @Test
        fun `should return success with deducted dir path skipping project view`() {
            // given
            val projectView = ProjectView.Builder().build()

            // when
            val dotBazelBspDirPathSpec = DotBazelBspDirPathSpecExtractor.fromProjectView(projectView)

            // then
            val expectedDotBazelBspDirPathSpec = DotBazelBspDirPathSpec(Path("").toAbsolutePath().resolve(".bazelbsp"))
            dotBazelBspDirPathSpec shouldBe expectedDotBazelBspDirPathSpec
        }
    }

    @Nested
    @DisplayName("fun default(): DotBazelBspDirPathSpec tests")
    inner class DefaultTest {

        @Test
        fun `should return success with deducted dir path`() {
            // given
            // when
            val dotBazelBspDirPathSpec = DotBazelBspDirPathSpecExtractor.default()

            // then
            val expectedDotBazelBspDirPathSpec = DotBazelBspDirPathSpec(Path("").toAbsolutePath().resolve(".bazelbsp"))
            dotBazelBspDirPathSpec shouldBe expectedDotBazelBspDirPathSpec
        }
    }
}
