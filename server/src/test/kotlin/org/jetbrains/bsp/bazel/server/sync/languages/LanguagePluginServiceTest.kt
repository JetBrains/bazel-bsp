package org.jetbrains.bsp.bazel.server.sync.languages

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.bazelrunner.orLatestSupported
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.languages.android.AndroidLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.go.GoLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkResolver
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkVersionResolver
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.python.PythonLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.rust.RustLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.workspacecontext.DefaultWorkspaceContextProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory

class LanguagePluginServiceTest {

  private lateinit var languagePluginsService: LanguagePluginsService
  private lateinit var workspaceRoot: Path
  private lateinit var projectViewFile: Path

  @BeforeEach
  fun beforeEach() {
    workspaceRoot = createTempDirectory("workspaceRoot")
    projectViewFile = workspaceRoot.resolve("projectview.bazelproject")
    val bazelInfo = BasicBazelInfo(
      execRoot = "",
      outputBase = Paths.get(""),
      workspaceRoot = Paths.get(""),
      release = BazelRelease.fromReleaseString("release 6.0.0").orLatestSupported(),
      false
    )
    val provider = DefaultWorkspaceContextProvider(Paths.get(""), projectViewFile)
    val bazelPathsResolver = BazelPathsResolver(bazelInfo)
    val jdkResolver = JdkResolver(bazelPathsResolver, JdkVersionResolver())
    val javaLanguagePlugin = JavaLanguagePlugin(provider, bazelPathsResolver, jdkResolver)
    val scalaLanguagePlugin = ScalaLanguagePlugin(javaLanguagePlugin, bazelPathsResolver)
    val cppLanguagePlugin = CppLanguagePlugin(bazelPathsResolver)
    val kotlinLanguagePlugin = KotlinLanguagePlugin(javaLanguagePlugin)
    val thriftLanguagePlugin = ThriftLanguagePlugin(bazelPathsResolver)
    val pythonLanguagePlugin = PythonLanguagePlugin(bazelPathsResolver)
    val rustLanguagePlugin = RustLanguagePlugin(bazelPathsResolver)
    val androidLanguagePlugin = AndroidLanguagePlugin(javaLanguagePlugin, kotlinLanguagePlugin, bazelPathsResolver)
    val goLanguagePlugin = GoLanguagePlugin(bazelPathsResolver)
    languagePluginsService = LanguagePluginsService(
        scalaLanguagePlugin,
        javaLanguagePlugin,
        cppLanguagePlugin,
        kotlinLanguagePlugin,
        thriftLanguagePlugin,
        pythonLanguagePlugin,
        rustLanguagePlugin,
        androidLanguagePlugin,
        goLanguagePlugin
    )
  }

  @Nested
  @DisplayName("Tests for the method shouldGetPlugin")
  inner class ShouldGetPluginTest {

    @Test
    fun `should return JavaLanguagePlugin for Java Language`() {
      // given
      val languages: Set<Language> = hashSetOf(Language.JAVA)

      // when
      val plugin = languagePluginsService.getPlugin(languages) as? JavaLanguagePlugin

      // then
      plugin shouldNotBe null
    }

    @Test
    fun `should return KotlinLanguagePlugin for Kotlin Language`() {
      // given
      val languages: Set<Language> = hashSetOf(Language.KOTLIN)

      // when
      val plugin = languagePluginsService.getPlugin(languages) as? KotlinLanguagePlugin

      // then
      plugin shouldNotBe null
    }

    @Test
    fun `should return ScalaLanguagePlugin for Scala Language`() {
      // given
      val languages: Set<Language> = hashSetOf(Language.SCALA)

      // when
      val plugin = languagePluginsService.getPlugin(languages) as? ScalaLanguagePlugin

      // then
      plugin shouldNotBe null
    }

    @Test
    fun `should return CppLanguagePlugin for Cpp Language`() {
      // given
      val languages: Set<Language> = hashSetOf(Language.CPP)

      // when
      val plugin = languagePluginsService.getPlugin(languages) as? CppLanguagePlugin

      // then
      plugin shouldNotBe null
    }

    @Test
    fun `should return EmptyLanguagePlugin for no Language`() {
      // given
      val languages: Set<Language> = hashSetOf()

      // when
      val plugin = languagePluginsService.getPlugin(languages) as? EmptyLanguagePlugin

      // then
      plugin shouldNotBe null
    }

    @Test
    fun `should return ThriftLanguagePlugin for Thrift Language`() {
      // given
      val languages: Set<Language> = hashSetOf(Language.THRIFT)

      // when
      val plugin = languagePluginsService.getPlugin(languages) as? ThriftLanguagePlugin

      // then
      plugin shouldNotBe null
    }

    @Test
    fun `should return GoLanguagePlugin for Go Language`() {
      // given
      val languages: Set<Language> = hashSetOf(Language.GO)

      // when
      val plugin = languagePluginsService.getPlugin(languages) as? GoLanguagePlugin

      // then
      plugin shouldNotBe null
    }
  }

  @Test
  fun `should return RustLanguagePlugin for Rust Language`() {
    // given
    val languages: Set<Language> = hashSetOf(Language.RUST)

    // when
    val plugin = languagePluginsService.getPlugin(languages) as? RustLanguagePlugin

    // then
    plugin shouldNotBe null
  }

  @Nested
  @DisplayName("Tests for the method shouldGetSourceSet")
  inner class ShouldGetSourceSetTest {
    private lateinit var tmpRepo: Path

    @BeforeEach
    fun beforeEach() {
      tmpRepo = createTempDirectory()
    }

    private fun createFileAndWrite(dirString: String, filename: String, content: String): Path {
      val dirPath = tmpRepo.resolve(dirString)
      Files.createDirectories(dirPath)
      val filePath = dirPath.resolve(filename)
      Files.createFile(filePath)
      File(filePath.toUri()).writeText(content)
      return filePath
    }

    @AfterEach
    fun afterEach() {
      val tmpDir = tmpRepo.toFile()
      tmpDir.deleteRecursively()
    }

    @Test
    fun `should return sourceSet for Java Language`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/java/"
      val filename = "JavaPackageTest.java"
      val content = """
                |package org.jetbrains.bsp.bazel.server.sync.languages.java;
                |
                |            public class JavaPackageTest{
                |              public public static void main(String[] args) {
                |                return;
                |              }
                |            }
                """.trimMargin()
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.JAVA))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldBe tmpRepo
    }

    @Test
    fun `should not return tmpRepo for empty file in Java Language`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/java/"
      val filename = "JavaPackageTest.java"
      val content = ""
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.JAVA))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldNotBe tmpRepo
      result shouldBe tmpRepo.resolve(dirString)
    }

    @Test
    fun `should not return tmpRepo for Java Language with wrong package declaration`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/java/"
      val filename = "JavaPackageTest.java"
      val content = """
                |package org.jetbrains.bsp.bazel.server.sync.languages;
                |package java;
                |public class JavaPackageTest{
                |  public public static void main(String[] args) {
                |    return;
                |  }
                |}
                """.trimMargin()
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.JAVA))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldNotBe tmpRepo
      result shouldBe tmpRepo.resolve(dirString)
    }

    @Test
    fun `should return sourceSet for Scala Language from one line package declaration`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/"
      val filename = "ScalaPackageTest.java"
      val content = """
                |package org.jetbrains.bsp.bazel.server.sync.languages;
                |
                |   class ScalaPackageTest{
                |        def main(){
                |                return null;
                |              }
                |            }
                """.trimMargin()
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.SCALA))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldBe tmpRepo
    }

    @Test
    fun `should return sourceSet for Scala Language from two line package declaration`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/scala/"
      val filename = "ScalaPackageTest.java"
      val content = """
                |package org.jetbrains.bsp.bazel.server.sync.languages;
                |package scala;
                |
                |class ScalaPackageTest{
                |  def main(){
                |    return null;
                |  }
                |}
                """.trimMargin()
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.SCALA))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldBe tmpRepo
    }

    @Test
    fun `should return sourceSet for Scala Language from multi line package declaration`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/scala/"
      val filename = "ScalaPackageTest.java"
      val content = """
                |package org.jetbrains.bsp.bazel;
                |package server.sync.languages;
                |
                |
                |package scala;
                |
                |class ScalaPackageTest{
                |  def main(){
                |    return null;
                |  }
                |}
                """.trimMargin()
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.SCALA))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldBe tmpRepo
    }

    @Test
    fun `should not return tmpRepo for Scala Language from empty package declaration`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/"
      val filename = "ScalaPackageTest.java"
      val content = ""
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.SCALA))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldNotBe tmpRepo
      result shouldBe tmpRepo.resolve(dirString)
    }

    @Test
    fun `should return sourceSet for Kotlin Language`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/kotlin/"
      val filename = "KotlinPackageTest.kt"
      val content = """
                |package org.jetbrains.bsp.bazel.server.sync.languages.kotlin
                |
                |fun main() {
                |    println("Hello, World!")
                |}
                """.trimMargin()
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.KOTLIN))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldBe tmpRepo
    }

    @Test
    fun `should not return tmpRepo for Kotlin Language for empty file`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/kotlin/"
      val filename = "KotlinPackageTest.kt"
      val content = ""
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.KOTLIN))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldNotBe tmpRepo
      result shouldBe tmpRepo.resolve(dirString)
    }

    @Test
    fun `should return null for no Language`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/"
      val filename = "EmptyPackageTest.java"
      val content = """
                |package org.jetbrains.bsp.bazel.server.sync.languages
                |
                """.trimMargin()

      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf())

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldBe null
    }

    @Test
    fun `should not return tmpRepo for Thrift Language`() {
      // given
      val dirString = "org/jetbrains/bsp/bazel/server/sync/languages/"
      val filename = "ThriftPackageTest.java"
      val content = """
                |package org.jetbrains.bsp.bazel.server.sync.languages
                |
                |public class HelloWorldNative
                |{
                |        public static String hello(String name)
                |        {
                |                return "Hello, "+name;
                |        }
                |
                |        public static void main(String args[])
                |        {
                |                long start=System.currentTimeMillis();
                |                for (int i=0;i<100000;i++)
                |                {
                |                        System.out.println(hello("world"+i));
                |                }
                |                long end=System.currentTimeMillis();
                |                System.out.println((end-start)+" ms");
                |        }
                |}
                """.trimMargin()
      val filePath = createFileAndWrite(dirString, filename, content)
      val plugin = languagePluginsService.getPlugin(hashSetOf(Language.THRIFT))

      // when
      val result = plugin.calculateSourceRoot(filePath)

      // then
      result shouldNotBe null
      result shouldNotBe tmpRepo
      result shouldBe tmpRepo.resolve(dirString)
    }
  }
}
