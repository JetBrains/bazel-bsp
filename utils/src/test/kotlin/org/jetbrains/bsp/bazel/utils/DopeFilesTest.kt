package org.jetbrains.bsp.bazel.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path

class DopeFilesTest {

    @Nested
    @DisplayName("DopeFiles.readText(filePath) tests")
    inner class ReadTextTest {

        @Test
        fun `should return failure for not existing file`() {
            // given
            val filePath = Path("file/doesnt/exist")

            // when
            val fileContentTry = DopeFiles.readText(filePath)

            // then
            fileContentTry.isFailure shouldBe true
            fileContentTry.cause::class shouldBe NoSuchFileException::class
            fileContentTry.cause.message shouldBe "file/doesnt/exist"
        }

        @Test
        fun `should parse existing file and return success`() {
            // given
            val file = File.createTempFile("test", "file")
            file.deleteOnExit()
            val filePath = file.toPath()
            Files.writeString(filePath, "test content")

            // when
            val fileContentTry = DopeFiles.readText(filePath)

            // then
            fileContentTry.isSuccess shouldBe true
            val fileContent = fileContentTry.get()

            fileContent shouldBe "test content"
        }
    }

    @Nested
    @DisplayName("DopeFiles.writeText(filePath, text) tests")
    inner class WriteTextTest {

        @Test
        fun `should return failure for path without writing permission`() {
            // given
            val file = File.createTempFile("not", "writable")
            file.deleteOnExit()
            file.setWritable(false)
            val filePath = file.toPath()

            // then
            val writeResult = DopeFiles.writeText(filePath, "test content")

            // then
            writeResult.isFailure shouldBe true
            writeResult.cause::class shouldBe AccessDeniedException::class
            writeResult.cause.message shouldBe filePath.toString()
        }

        @Test
        fun `should return success and create the file save text for normal path`() {
            // given
            val file = File.createTempFile("not", "writable")
            file.delete()
            val filePath = file.toPath()

            // then
            val writeResult = DopeFiles.writeText(filePath, "test content")

            // then
            writeResult.isSuccess shouldBe true
            Files.readString(filePath) shouldBe "test content"
        }

        @Test
        fun `should return success and override file content`() {
            // given
            val file = File.createTempFile("not", "writable")
            file.deleteOnExit()
            val filePath = file.toPath()
            Files.writeString(filePath, "old content")

            // then
            val writeResult = DopeFiles.writeText(filePath, "test content")

            // then
            writeResult.isSuccess shouldBe true
            Files.readString(filePath) shouldBe "test content"
        }
    }
}
