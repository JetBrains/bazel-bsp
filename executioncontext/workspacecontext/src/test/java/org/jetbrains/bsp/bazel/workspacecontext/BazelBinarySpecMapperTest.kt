package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractorException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class BazelBinarySpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): BazelBinarySpec tests")
    inner class MapTest {

        // TODO https://youtrack.jetbrains.com/issue/BAZEL-619
        @Disabled("for now we don't have a framework to change classpath, i'll fix it later")
        @Test
        fun `should return failure if it isn't possible to deduct bazel path from PATH and bazel path is null`() {
           // given
            val projectView = ProjectView.Builder(bazelBinary = null).build()

           // when
            val exception = shouldThrow<ExecutionContextEntityExtractorException> {
                BazelBinarySpecExtractor.fromProjectView(projectView)
            }

           // then
            exception.message shouldBe "Mapping project view into 'bazel path' failed! Could not find bazel on your PATH"
        }

        @Disabled("for now we don't have a framework to change classpath, i'll fix it later")
        @Test
        fun `should return success with deducted bazel path from PATH if bazel path is null`() {
            // given
            val projectView = ProjectView.Builder(bazelBinary = null).build()

            // when
            val bazelBinarySpec = BazelBinarySpecExtractor.fromProjectView(projectView)

            // then
            val expectedBazelBinarySpec = BazelBinarySpec(Path("/usr/local/bin/bazel"))
            bazelBinarySpec shouldBe expectedBazelBinarySpec
        }

        @Test
        fun `should return success for successful mapping`() {
            // given
            val projectView =
                ProjectView.Builder(
                    bazelBinary = ProjectViewBazelBinarySection(Path("/path/to/bazel"))
                ).build()

            // when
            val bazelBinarySpec = BazelBinarySpecExtractor.fromProjectView(projectView)

            // then
            val expectedBazelBinarySpec = BazelBinarySpec(Path("/path/to/bazel"))
            bazelBinarySpec shouldBe expectedBazelBinarySpec
        }
    }
}
