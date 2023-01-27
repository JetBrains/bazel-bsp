package org.jetbrains.bsp.bazel.utils.dope

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.AccessDeniedException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException

class DopeFilesTest {

    @Nested
    @DisplayName("DopeFiles.readText(filePath) tests")
    inner class ReadTextTest {

        @Test
        fun `should return failure for not existing file`() {
            // given
            val filePath = DopeTemp.createTempPath("file/doesnt/exist.xd")

            // when
            val fileContentTry = DopeFiles.readText(filePath)

            // then
            fileContentTry.isFailure shouldBe true
            fileContentTry.cause::class shouldBe NoSuchFileException::class
        }

        @Test
        fun `should parse existing file and return success`() {
            // given
            val filePath = DopeTemp.createTempFile("path/to/file.xd")
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

        @Disabled("It doesn't work on TC, it's deprecated anyway - im about to remove DopeFiles")
        @Test
        fun `should return failure for path without writing permission`() {
            // given
            val filePath = DopeTemp.createTempFile("path/to/file.xd", false)

            // when
            val writeResult = DopeFiles.writeText(filePath, "test content")

            // then
            writeResult.isFailure shouldBe true
            writeResult.cause::class shouldBe AccessDeniedException::class
            writeResult.cause.message shouldBe filePath.toString()
        }

        @Test
        fun `should return success and create the file save text for normal path`() {
            // given
            val filePath = DopeTemp.createTempPath("path/to/file.xd")

            // when
            val writeResult = DopeFiles.writeText(filePath, "test content")

            // then
            writeResult.isSuccess shouldBe true

            Files.readString(filePath) shouldBe "test content"
        }

        @Test
        fun `should return success and override file content`() {
            // given
            val filePath = DopeTemp.createTempFile("path/to/file.xd")
            Files.writeString(filePath, "old content")

            // when
            val writeResult = DopeFiles.writeText(filePath, "test content")

            // then
            writeResult.isSuccess shouldBe true

            Files.readString(filePath) shouldBe "test content"
        }
    }

    @Nested
    @DisplayName("DopeFiles.createDirectories(dir) tests")
    inner class CreateDirectoriesTest {

        @Test
        fun `should return failure if a file exists in the location`() {
            // given
            val dirPath = DopeTemp.createTempFile("existing/file.xd")

            // when
            val createDirectoriesResult = DopeFiles.createDirectories(dirPath)

            // then
            createDirectoriesResult.isFailure shouldBe true
            createDirectoriesResult.cause::class shouldBe FileAlreadyExistsException::class
            createDirectoriesResult.cause.message shouldBe dirPath.toString()
        }

        @Test
        fun `should return success and create 1 dir`() {
            // given
            val dirPath = DopeTemp.createTempPath("dir")

            // when
            val createDirectoriesResult = DopeFiles.createDirectories(dirPath)

            // then
            createDirectoriesResult.isSuccess shouldBe true

            Files.exists(dirPath) shouldBe true
            Files.isDirectory(dirPath) shouldBe true
        }

        @Test
        fun `should return success and create 3 dirs`() {
            // given
            val dirPath = DopeTemp.createTempPath("path/to/dir")

            // when
            val createDirectoriesResult = DopeFiles.createDirectories(dirPath)

            // then
            createDirectoriesResult.isSuccess shouldBe true

            Files.exists(dirPath) shouldBe true
            Files.isDirectory(dirPath) shouldBe true

            Files.exists(dirPath.parent) shouldBe true
            Files.isDirectory(dirPath.parent) shouldBe true

            Files.exists(dirPath.parent.parent) shouldBe true
            Files.isDirectory(dirPath.parent.parent) shouldBe true
        }
    }
}
