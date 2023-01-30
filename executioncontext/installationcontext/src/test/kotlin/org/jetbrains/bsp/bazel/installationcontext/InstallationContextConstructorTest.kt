package org.jetbrains.bsp.bazel.installationcontext

import io.kotest.matchers.shouldBe
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.Path

class InstallationContextConstructorTest {

    private lateinit var installationContextConstructor: InstallationContextConstructor

    @BeforeEach
    fun beforeEach() {
        // given
        this.installationContextConstructor =
            InstallationContextConstructor(Path("/path/to/projectview.bazelproject"), Path("/path/to/bazel/workspace"))
    }

    @Nested
    @DisplayName("fun construct(projectViewTry: Try<ProjectView> ): Try<WorkspaceContext> tests")
    inner class ConstructProjectViewTryTest {

        @Test
        fun `should return failure if project view is failure`() {
            // given
            val projectViewTry = Try.failure<ProjectView>(Exception("exception message"))

            // when
            val installationContextTry = installationContextConstructor.construct(projectViewTry)

            // then
            installationContextTry.isFailure shouldBe true
            installationContextTry.cause::class shouldBe Exception::class
            installationContextTry.cause.message shouldBe "exception message"
        }
    }

    @Nested
    @DisplayName("fun construct(projectView: ProjectView): Try<WorkspaceContext> tests")
    inner class ConstructProjectViewTest {

        @Test
        fun `should return success if project view is valid`() {
            // given
            val projectView =
                ProjectView.Builder(
                    javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java")),
                    debuggerAddress = ProjectViewDebuggerAddressSection("host:8000")
                ).build()

            // when
            val installationContextTry = installationContextConstructor.construct(projectView)

            // then
            installationContextTry.isSuccess shouldBe true
            val installationContext = installationContextTry.get()

            val expectedJavaPath = InstallationContextJavaPathEntity(Paths.get("/path/to/java"))
            installationContext.javaPath shouldBe expectedJavaPath

            val expectedDebuggerAddress = InstallationContextDebuggerAddressEntity("host:8000")
            installationContext.debuggerAddress shouldBe expectedDebuggerAddress

            val expectedProjectViewFilePath = Path("/path/to/projectview.bazelproject")
            installationContext.projectViewFilePath shouldBe expectedProjectViewFilePath
        }
    }

    @Nested
    @DisplayName("fun constructDefault(): Try<InstallationContext> tests")
    inner class ConstructDefaultTest {

        @Test
        fun `should return success and default installation context`() {
            // given
            System.setProperty("java.home", "/path/to/java")

            // when
            val installationContextTry = installationContextConstructor.constructDefault()

            // then
            installationContextTry.isSuccess shouldBe true
            val installationContext = installationContextTry.get()

            val expectedInstallationContext = InstallationContext(
                javaPath = InstallationContextJavaPathEntity(Path("/path/to/java/bin/java")),
                debuggerAddress = null,
                projectViewFilePath = Path("/path/to/projectview.bazelproject"),
                bazelWorkspaceRootDir = Path("/path/to/bazel/workspace")
            )
            installationContext shouldBe expectedInstallationContext
        }
    }
}
