package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelBinarySection
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class BazelBinarySpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): Try<BazelBinarySpec> tests")
    inner class MapTest {

        // TODO https://youtrack.jetbrains.com/issue/BAZEL-58 + framework to test envs
        @Disabled("for now we don't have a framework to change classpath, i'll fix it later")
        @Test
        fun `should return failure if it isn't possible to deduct bazel path from PATH and bazel path is null`() {
           // given
           val projectView = ProjectView.Builder(bazelBinary = null).build().get()

           // when
           val bazelBinarySpecTry = BazelBinarySpecMapper.map(projectView)

           // then
           bazelBinarySpecTry.isFailure shouldBe true
           bazelBinarySpecTry.cause::cause shouldBe ProjectViewToExecutionContextEntityMapperException::class
           bazelBinarySpecTry.cause.message shouldBe "Mapping project view into 'bazel path' failed! Could not find bazel on your PATH"
        }

        @Disabled("for now we don't have a framework to change classpath, i'll fix it later")
        @Test
        fun `should return success with deducted bazel path from PATH if bazel path is null`() {
            // given
            val projectView = ProjectView.Builder(bazelBinary = null).build().get()

            // when
            val bazelBinarySpecTry = BazelBinarySpecMapper.map(projectView)

            // then
            bazelBinarySpecTry.isSuccess shouldBe true
            val bazelBinarySpec = bazelBinarySpecTry.get()

            val expectedBazelBinarySpec = BazelBinarySpec(Path("/usr/local/bin/bazel"))
            bazelBinarySpec shouldBe expectedBazelBinarySpec
        }

        @Test
        fun `should return success for successful mapping`() {
            // given
            val projectView =
                ProjectView.Builder(
                    bazelBinary = ProjectViewBazelBinarySection(Path("/path/to/bazel"))
                ).build().get()

            // when
            val bazelBinarySpecTry = BazelBinarySpecMapper.map(projectView)

            // then
            bazelBinarySpecTry.isSuccess shouldBe true
            val bazelBinarySpec = bazelBinarySpecTry.get()

            val expectedBazelBinarySpec = BazelBinarySpec(Path("/path/to/bazel"))
            bazelBinarySpec shouldBe expectedBazelBinarySpec
        }
    }

    @Nested
    @DisplayName("fun default(): Try<BazelBinarySpec> tests")
    inner class DefaultTest {

        @Disabled("for now we don't have a framework to change classpath, i'll fix it later")
        @Test
        fun `should return success with deducted bazel path from PATH`() {
            // given
            // when
            val bazelBinarySpecTry = BazelBinarySpecMapper.default()

            // then
            bazelBinarySpecTry.isSuccess shouldBe true
            val bazelBinarySpec = bazelBinarySpecTry.get()

            val expectedBazelBinarySpec = BazelBinarySpec(Path("/usr/local/bin/bazel"))
            bazelBinarySpec shouldBe expectedBazelBinarySpec
        }
    }
}
