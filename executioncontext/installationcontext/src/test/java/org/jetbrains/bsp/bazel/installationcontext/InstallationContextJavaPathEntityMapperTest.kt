package org.jetbrains.bsp.bazel.installationcontext

import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class InstallationContextJavaPathEntityMapperTest {

    @Test
    fun `should return success with java path from project view if java path is specified in project view`() {
        // given
        val projectView = ProjectView.Builder(
            javaPath = ProjectViewJavaPathSection(Paths.get("/path/to/java"))
        ).build().get()

        // when
        val javaPathTry = InstallationContextJavaPathEntityMapper.map(projectView)

        // then
        javaPathTry.isSuccess shouldBe true
        val javaPath = javaPathTry.get()

        val expectedJavaPath = InstallationContextJavaPathEntity(Paths.get("/path/to/java"))
        javaPath shouldBe expectedJavaPath
    }

    @Test
    fun `should return success with java path from system property if java path is not specified in project view`() {
        // given
        val projectView = ProjectView.Builder(javaPath = null).build().get()
        System.setProperty("java.home", "/path/to/java")

        // when
        val javaPathTry = InstallationContextJavaPathEntityMapper.map(projectView)

        // then
        javaPathTry.isSuccess shouldBe true
        val javaPath = javaPathTry.get()

        val expectedJavaPath = InstallationContextJavaPathEntity(Paths.get("/path/to/java/bin/java"))
        javaPath shouldBe expectedJavaPath
    }

    @Test
    fun `should return failure if java path is not specified in project view and it is not possible to obtain it from system property`() {
        // given
        val projectView = ProjectView.Builder(javaPath = null).build().get()
        System.clearProperty("java.home")

        // when
        val javaPathTry = InstallationContextJavaPathEntityMapper.map(projectView)

        // then
        javaPathTry.isFailure shouldBe true
        javaPathTry.cause::class shouldBe ProjectViewToExecutionContextEntityMapperException::class
        javaPathTry.cause.message shouldBe "Mapping project view into 'java path' failed! System property 'java.home' is not specified."
    }
}
