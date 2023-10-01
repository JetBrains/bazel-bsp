package org.jetbrains.bsp.bazel.server.sync.languages

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

class JVMLanguagePluginParserTest {

    private lateinit var tempRoot: Path

    @BeforeEach
    fun beforeEach() {
        tempRoot = createTempDirectory("test-temp-root")
    }

    @AfterEach
    fun afterEach() {
        tempRoot.toFile().deleteRecursively()
    }

    @Test
    fun `should return source dir for empty package`() {
        // given
        val fileContent = """
            |
            |public class Test {
            |}
            |
        """.trimMargin()

        val sourceRoot = tempRoot.resolve("path/to/source/")
        val sourceDir = Files.createDirectories(sourceRoot.resolve("dir1/dir2/dir3/"))

        val sourceFile = Files.createFile(sourceDir.resolve("File.java"))
        sourceFile.writeText(fileContent)

        // when
        val calculatedSourceRoot = JVMLanguagePluginParser.calculateJVMSourceRoot(sourceFile)

        // then
        calculatedSourceRoot shouldBe sourceDir
    }

    @Test
    fun `should return source root as a sub path for package corresponding to path`() {
        // given
        val packageName = "dir1.dir2.dir3"
        val fileContent = """
            |package $packageName
            |
            |public class Test {
            |}
            |
        """.trimMargin()

        val sourceRoot = tempRoot.resolve("path/to/source/")
        val sourceDir = Files.createDirectories(sourceRoot.resolve("dir1/dir2/dir3/"))

        val sourceFile = Files.createFile(sourceDir.resolve("File.java"))
        sourceFile.writeText(fileContent)

        // when
        val calculatedSourceRoot = JVMLanguagePluginParser.calculateJVMSourceRoot(sourceFile)

        // then
        calculatedSourceRoot shouldBe sourceRoot
    }

    @Test
    fun `should return source dir for package longer than the path`() {
        // given
        val packageName = (1..100).joinToString(".") { "dir$it" }
        val fileContent = """
            |package $packageName
            |
            |public class Test {
            |}
            |
        """.trimMargin()

        val sourceRoot = tempRoot.resolve("path/to/source/")
        val sourceDir = Files.createDirectories(sourceRoot.resolve("dir1/dir2/dir3/"))

        val sourceFile = Files.createFile(sourceDir.resolve("File.java"))
        sourceFile.writeText(fileContent)

        // when
        val calculatedSourceRoot = JVMLanguagePluginParser.calculateJVMSourceRoot(sourceFile)

        // then
        calculatedSourceRoot shouldBe sourceDir
    }

    @Test
    fun `should return source dir for package name not matching the dir`() {
        // given

        val fileContent = """
            |package com.example
            |
            |public class Test {
            |}
            |
        """.trimMargin()

        val sourceRoot = tempRoot.resolve("path/to/source/")
        val sourceDir = Files.createDirectories(sourceRoot.resolve("dir1/dir2/dir3/"))

        val sourceFile = Files.createFile(sourceDir.resolve("File.java"))
        sourceFile.writeText(fileContent)

        // when
        val calculatedSourceRoot = JVMLanguagePluginParser.calculateJVMSourceRoot(sourceFile)

        // then
        calculatedSourceRoot shouldBe sourceDir
    }
}
