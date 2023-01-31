package org.jetbrains.bsp.bazel.utils.dope

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files

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
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"
        }

        @Test
        fun `should return path with 1 directory to writable file`() {
            // given
            val rawPath = "path/file.xd"

            // when
            val path = DopeTemp.createTempFile(rawPath)

            // then
            Files.exists(path) shouldBe true
            Files.isWritable(path) shouldBe true
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"

            Files.exists(path.parent) shouldBe true
            path.parent.fileName.toString() shouldStartWith "path"
        }

        @Test
        fun `should return path with 2 directories to writable file`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempFile(rawPath)

            // then
            Files.exists(path) shouldBe true
            Files.isWritable(path) shouldBe true
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"

            Files.exists(path.parent.parent) shouldBe true
            path.parent.fileName.toString() shouldStartWith "to"

            Files.exists(path.parent.parent) shouldBe true
            path.parent.parent.fileName.toString() shouldStartWith "path"
        }

        @Disabled("It doesn't work on TC, it's deprecated anyway - im about to remove DopeTemp")
        @Test
        fun `should return path to not writable file`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempFile(rawPath, false)

            // then
            Files.exists(path) shouldBe true
            Files.isWritable(path) shouldBe false
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"

            Files.exists(path.parent) shouldBe true
            path.parent.fileName.toString() shouldStartWith "to"

            Files.exists(path.parent.parent) shouldBe true
            path.parent.parent.fileName.toString() shouldStartWith "path"
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
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"
        }

        @Test
        fun `should return path with 1 directory (file shouldn't exist)`() {
            // given
            val rawPath = "path/file.xd"

            // when
            val path = DopeTemp.createTempPath(rawPath)

            // then
            Files.exists(path) shouldBe false
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"

            Files.exists(path.parent) shouldBe false
            path.parent.fileName.toString() shouldStartWith "path"
        }

        @Test
        fun `should return path with 2 directories (file shouldn't exist)`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempPath(rawPath)

            // then
            Files.exists(path) shouldBe false
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"

            Files.exists(path.parent) shouldBe false
            path.parent.fileName.toString() shouldStartWith "to"

            Files.exists(path.parent.parent) shouldBe false
            path.parent.parent.fileName.toString() shouldStartWith "path"
        }
    }
}
