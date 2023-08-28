package org.jetbrains.bsp.bazel.install

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class EnvironmentCreatorTest {

    class MockEnvironmentCreator(projectRootDir: Path) : EnvironmentCreator(projectRootDir) {
        override fun create(): Unit = Unit

        fun testCreateDotBazelBsp() = createDotBazelBsp()
    }

    @Nested
    @DisplayName("environmentCreator.create tests")
    inner class CopyAspectsTest {

        private lateinit var tempRoot: Path

        @BeforeEach
        fun beforeEach() {
            tempRoot = createTempDirectory("test-workspace-root")
            tempRoot.toFile().deleteOnExit()
        }

        @Test
        fun `should copy aspects from resources to dot bazelbsp directory`() {
            // when
            val dotBazelBsp = MockEnvironmentCreator(tempRoot).testCreateDotBazelBsp()

            // then
            dotBazelBsp shouldNotBe null
            dotBazelBsp.resolve("aspects/core.bzl").exists() shouldBeEqual true
            dotBazelBsp.resolve("aspects/rules").isDirectory() shouldBeEqual true
            dotBazelBsp.resolve("aspects/utils").isDirectory() shouldBeEqual true
            dotBazelBsp.resolve("aspects/utils/utils.bzl").exists() shouldBeEqual true
            dotBazelBsp.resolve("aspects/utils/java_utils.bzl").exists() shouldBeEqual true
            dotBazelBsp.resolve("aspects/rules/java/java_info.bzl").exists() shouldBeEqual true
            dotBazelBsp.resolve("aspects/rules/kt/kt_info.bzl").exists() shouldBeEqual true
            dotBazelBsp.resolve("aspects/rules/python/python_info.bzl").exists() shouldBeEqual true
            dotBazelBsp.resolve("aspects/rules/scala/scala_info.bzl").exists() shouldBeEqual true
            dotBazelBsp.resolve("aspects/rules/cpp/cpp_info.bzl").exists() shouldBeEqual true
        }
    }
}
