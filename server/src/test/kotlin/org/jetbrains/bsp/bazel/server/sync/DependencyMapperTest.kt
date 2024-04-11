package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.MavenDependencyModule
import ch.epfl.scala.bsp4j.MavenDependencyModuleArtifact
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Library
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths

class DependencyMapperTest {

    private val cacheLocation = "file:///home/user/.cache/bazel/_bazel_user/ae7b7b315151086e31e3b97f9ddba009/execroot/monorepo/bazel-out/k8-fastbuild-ST-4a519fd6d3e4"

    @Test
    fun `should translate dependency`() {
        val jarUri = URI.create("$cacheLocation/bin/external/maven/org/scala-lang/scala-library/2.13.11/processed_scala-library-2.13.11.jar")
        val jarSourcesUri = URI.create("$cacheLocation/bin/external/maven/org/scala-lang/scala-library/2.13.11/scala-library-2.13.11-sources.jar")
        val lib1 = Library(
            "@maven//:org_scala_lang_scala_library",
            setOf(jarUri),
            setOf(jarSourcesUri),
            emptyList()
        )
        val expectedMavenArtifact = MavenDependencyModuleArtifact(jarUri.toString())
        val expectedMavenSourcesArtifact = MavenDependencyModuleArtifact(jarSourcesUri.toString())
        expectedMavenSourcesArtifact.classifier = "sources"
        val expectedDependency = MavenDependencyModule("org.scala-lang", "scala-library", "2.13.11", listOf(
            expectedMavenArtifact,
            expectedMavenSourcesArtifact
        ))
        val dependency = DependencyMapper.extractMavenDependencyInfo(lib1)

        dependency shouldBe expectedDependency
    }

    @Test
    fun `should bazelmod translate dependency`() {
        val jarUri = URI.create("$cacheLocation/bin/external/rules_jvm_external~~maven~name/v1/https/repo1.maven.org/maven2/com/google/auto/service/auto-service-annotations/1.1.1/header_auto-service-annotations-1.1.1.jar")
        val jarSourcesUri = URI.create("$cacheLocation/bin/external/rules_jvm_external~~maven~name/v1/https/repo1.maven.org/maven2/com/google/auto/service/auto-service-annotations/1.1.1/header_auto-service-annotations-1.1.1-sources.jar")
        val lib1 = Library(
            "@@rules_jvm_external~override~maven~maven//:com_google_auto_service_auto_service_annotations",
            setOf(jarUri),
            setOf(jarSourcesUri),
            emptyList()
        )
        val expectedMavenArtifact = MavenDependencyModuleArtifact(jarUri.toString())
        val expectedMavenSourcesArtifact = MavenDependencyModuleArtifact(jarSourcesUri.toString())
        expectedMavenSourcesArtifact.classifier = "sources"
        val expectedDependency = MavenDependencyModule("com.google.auto.service", "auto-service-annotations", "1.1.1", listOf(
            expectedMavenArtifact,
            expectedMavenSourcesArtifact
        ))
        val dependency = DependencyMapper.extractMavenDependencyInfo(lib1)

        dependency shouldBe expectedDependency
    }

    @Test
    fun `should not translate non maven dependency`() {
        val lib1 = Library(
            "@//projects/v1:scheduler",
            emptySet(),
            emptySet(),
            emptyList()
        )
        val dependency = DependencyMapper.extractMavenDependencyInfo(lib1)

        dependency shouldBe null
    }

    @Test
    fun `should gather deps transitively`() {
        val jarUri = URI.create("$cacheLocation/bin/external/maven/org/scala-lang/scala-library/2.13.11/processed_scala-library-2.13.11.jar")
        val jarSourcesUri = URI.create("$cacheLocation/bin/external/maven/org/scala-lang/scala-library/2.13.11/scala-library-2.13.11-sources.jar")
        val lib1 = Library(
            "@maven//:org_scala_lang_scala_library",
            setOf(jarUri),
            setOf(jarSourcesUri),
            emptyList()
        )
        val lib2 = Library(
            "@maven//:org_scala_lang_scala_library2",
            emptySet(),
            emptySet(),
            listOf(lib1.label)
        )
        val lib3 = Library(
            "@maven//:org_scala_lang_scala_library3",
            emptySet(),
            emptySet(),
            listOf(lib1.label, lib2.label)
        )
        val lib4 = Library(
            "@maven//:org_scala_lang_scala_library4",
            emptySet(),
            emptySet(),
            listOf(lib3.label, lib2.label)
        )
        val libraries = mapOf(lib1.label to lib1, lib2.label to lib2, lib3.label to lib3, lib4.label to lib4)
        val currentUri = Paths.get(".").toUri()
        val project = Project(currentUri, emptyList(), emptyMap(), libraries, emptyList(), BazelRelease(6))
        val module = Module(
            Label(""),
            true,
            listOf(Label(lib4.label)),
            emptySet(),
            emptySet(),
            currentUri,
            SourceSet(emptySet(), emptySet(), emptySet()),
            emptySet(),
            emptySet(),
            emptySet(),
            null,
            emptyMap()
        )
        val foundLibraries = DependencyMapper.allModuleDependencies(project, module)

        foundLibraries shouldBe setOf(lib1, lib2, lib3, lib4)
    }
}
