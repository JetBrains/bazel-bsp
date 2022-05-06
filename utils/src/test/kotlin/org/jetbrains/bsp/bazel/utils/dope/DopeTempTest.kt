package org.jetbrains.bsp.bazel.utils.dope

import io.kotest.matchers.collections.shouldEndWith
import io.kotest.matchers.collections.shouldStartWith
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
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
        fun `should return path to writable file`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempFile(rawPath)

            // then
            Files.exists(path) shouldBe true
            Files.isWritable(path) shouldBe true
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"
            path.parent.fileName.toString() shouldStartWith "to"
            path.parent.parent.fileName.toString() shouldStartWith "path"
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
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"
            path.parent.fileName.toString() shouldStartWith "to"
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
        fun `should return path (file shouldn't exist)`() {
            // given
            val rawPath = "path/to/file.xd"

            // when
            val path = DopeTemp.createTempPath(rawPath)

            // then
            Files.exists(path) shouldBe false
            path.fileName.toString() shouldStartWith "file"
            path.fileName.toString() shouldEndWith ".xd"
            path.parent.fileName.toString() shouldStartWith "to"
            path.parent.parent.fileName.toString() shouldStartWith "path"
        }
    }
}
