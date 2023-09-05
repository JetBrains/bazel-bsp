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
            val workspaceRoot = Path("path/to/workspace")
            val projectView = ProjectView.Builder().build()
            val extractor = DotBazelBspDirPathSpecExtractor(workspaceRoot)

            // when
            val dotBazelBspDirPathSpec = extractor.fromProjectView(projectView)

            // then
            val expectedDotBazelBspDirPathSpec = DotBazelBspDirPathSpec(workspaceRoot.toAbsolutePath().resolve(".bazelbsp"))
            dotBazelBspDirPathSpec shouldBe expectedDotBazelBspDirPathSpec
        }
    }
}
