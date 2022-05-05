package org.jetbrains.bsp.bazel.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path
import kotlin.io.path.createTempFile

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
            val filePath = createTempFile("test", "file")
            Files.writeString(filePath, "test content")

            // when
            val fileContentTry = DopeFiles.readText(filePath)

            // then
            fileContentTry.isSuccess shouldBe true
            val fileContent = fileContentTry.get()

            fileContent shouldBe "test content"
        }
    }
}
