package org.jetbrains.bsp.bazel.utils.dope

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class DopeTempTest {

    @Nested
    @DisplayName("DopeTemp.createTempFile(rawPath, writable) tests")
    inner class CreateTempFileTest {

        @Test
        fun `should return path without directories to writable file`() {
            // given
            val rawPath = "file.xd"

            // when
            val path = DopeTemp.createTempFile(rawPath)

            // then
            Files.exists(path) shouldBe true
            Files.isWritable(path) shouldBe true
            path.extension shouldBe "xd"
            path.nameWithoutExtension shouldStartWith "file"
        }

        @Test
        fun `should return path to writable file`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempFile(rawPath)

            // then
            Files.exists(path) shouldBe true
            Files.isWritable(path) shouldBe true
            path.extension shouldBe "xd"
            path.nameWithoutExtension shouldStartWith "file"
            path.parent.name shouldStartWith "to"
            path.parent.parent.name shouldStartWith "path"
        }

        @Test
        fun `should return path to not writable file`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempFile(rawPath, false)

            // then
            Files.exists(path) shouldBe true
            Files.isWritable(path) shouldBe false
            path.extension shouldBe "xd"
            path.nameWithoutExtension shouldStartWith "file"
            path.parent.name shouldStartWith "to"
            path.parent.parent.name shouldStartWith "path"
        }
    }

    @Nested
    @DisplayName("DopeTemp.createTempPath(rawPath) tests")
    inner class CreateTempPathTest {

        @Test
        fun `should return path without directories (file shouldn't exist)`() {
            // given
            val rawPath = "file.xd"

            // when
            val path = DopeTemp.createTempPath(rawPath)

            // then
            Files.exists(path) shouldBe false
            path.extension shouldBe "xd"
            path.nameWithoutExtension shouldStartWith "file"
        }

        @Test
        fun `should return path (file shouldn't exist)`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempPath(rawPath)

            // then
            Files.exists(path) shouldBe false
            path.extension shouldBe "xd"
            path.nameWithoutExtension shouldStartWith "file"
            path.parent.name shouldStartWith "to"
            path.parent.parent.name shouldStartWith "path"
        }
    }
}
