package org.jetbrains.bsp.bazel.workspacecontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewImportDepthSection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ImportDepthSpecMapperTest {

    @Nested
    @DisplayName("fun map(projectView): Try<ImportDepthSpec> tests")
    inner class MapTest {

        @Test
        fun `should return success with default import depth for empty project view`() {
            // given
            val projectView = ProjectView.Builder().build().get()

            // when
            val importDepthSpecTry = ImportDepthSpecMapper.map(projectView)

            // then
            importDepthSpecTry.isSuccess shouldBe true
            val importDepthSpec = importDepthSpecTry.get()

            val expectedImportDepthSpec = ImportDepthSpec(0)
            importDepthSpec shouldBe expectedImportDepthSpec
        }

        @Test
        fun `should return success with mapped import depth from project view`() {
            // given
            val projectView = ProjectView.Builder(
                importDepth = ProjectViewImportDepthSection(3),
            ).build().get()
            // when
            val importDepthSpecTry = ImportDepthSpecMapper.map(projectView)
            // then
            importDepthSpecTry.isSuccess shouldBe true
            val importDepthSpec = importDepthSpecTry.get()
            val expectedImportDepthSpec = ImportDepthSpec(3)
            importDepthSpec shouldBe expectedImportDepthSpec
        }
    }

    @Nested
    @DisplayName("fun default(): Try<ImportDepthSpec> tests")
    inner class DefaultTest {

        @Test
        fun `should return success with default import depth`() {
            // given
            // when
            val importDepthSpecTry = ImportDepthSpecMapper.default()

            // then
            importDepthSpecTry.isSuccess shouldBe true
            val importDepthSpec = importDepthSpecTry.get()

            val expectedImportDepthSpec = ImportDepthSpec(0)
            importDepthSpec shouldBe expectedImportDepthSpec
        }
    }
}
