package org.jetbrains.bsp.bazel.installationcontext

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class InstallationContextJavaPathEntityMapperTest {

    @Nested
    @DisplayName("fun default(): InstallationContextJavaPathEntity tests")
    inner class DefaultTest {

        @Test
        fun `should return success with java path from system property`() {
            // given
            System.setProperty("java.home", "/path/to/java")

            // when
            val javaPath = InstallationContextJavaPathEntityMapper.default()

            // then
            val expectedJavaPath = InstallationContextJavaPathEntity(Paths.get("/path/to/java/bin/java"))
            javaPath shouldBe expectedJavaPath
        }

        @Test
        fun `should return failure if it is not possible to obtain it from system property`() {
            // given
            System.clearProperty("java.home")

            // when & then
            shouldThrowExactly<IllegalStateException> { InstallationContextJavaPathEntityMapper.default() }
        }
    }
}
