package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.*
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule
import org.jetbrains.bsp.bazel.server.sync.languages.java.Jdk
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaSdk
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import java.net.URI

class ProjectStorageTest {
    @Test
    @Throws(IOException::class)
    fun `should store and load project`() {
        val file = File.createTempFile("project-cache-test", ".json")
        file.delete()
        file.deleteOnExit()
        val path = file.toPath()
        val mockBuildClient = object : BuildClient {
            override fun onBuildShowMessage(p0: ShowMessageParams?) {
            }

            override fun onBuildLogMessage(p0: LogMessageParams?) {
            }

            override fun onBuildPublishDiagnostics(p0: PublishDiagnosticsParams?) {
            }

            override fun onBuildTargetDidChange(p0: DidChangeBuildTarget?) {
            }

            override fun onBuildTaskStart(p0: TaskStartParams?) {
            }

            override fun onBuildTaskProgress(p0: TaskProgressParams?) {
            }

            override fun onBuildTaskFinish(p0: TaskFinishParams?) {
            }

            override fun onRunPrintStdout(p0: PrintParams?) {
            }

            override fun onRunPrintStderr(p0: PrintParams?) {
            }

        }
        val storage = FileProjectStorage(path, BspClientLogger(mockBuildClient))
        val empty = storage.load()
        empty shouldBe null
        val scalaModule = ScalaModule(
            ScalaSdk("org.scala", "2.12.3", "2.12", emptyList()), emptyList(), JavaModule(
                Jdk("8", null),
                null,
                emptyList(),
                emptyList(),
                URI.create("file:///tmp/out"),
                emptyList(),
                null,
                emptyList(),
            )
        )
        val project = Project(
            URI.create("file:///root"),
            listOf(
                Module(
                    Label("//project:project"),
                    false,
                    listOf(Label("//project:dep")),
                    hashSetOf(Language.JAVA),
                    hashSetOf(Tag.LIBRARY),
                    URI.create("file:///root/project"),
                    SourceSet(
                        hashSetOf(URI.create("file:///root/project/Lib.java")),
                        emptySet(),
                        hashSetOf(URI.create("file:///root/project/"))
                    ),
                    emptySet(),
                    emptySet(),
                    emptySet(),
                    scalaModule,
                    hashMapOf()
                )
            ),
            mapOf(URI.create("file:///root/project/Lib.java") to Label("file:///root")),
            emptyMap(),
            emptyList(),
            BazelRelease(7),
        )
        storage.store(project)
        val loaded = storage.load()
        loaded shouldBe project
    }
}
