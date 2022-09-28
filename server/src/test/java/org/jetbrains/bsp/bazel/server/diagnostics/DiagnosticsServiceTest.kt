package org.jetbrains.bsp.bazel.server.diagnostics

import ch.epfl.scala.bsp4j.Diagnostic as BspDiagnostic
import ch.epfl.scala.bsp4j.Position as BspPosition
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.DiagnosticSeverity
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.Range
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import java.nio.file.Paths
import org.jetbrains.bsp.bazel.bazelrunner.BasicBazelInfo
import org.junit.jupiter.api.Test

class DiagnosticsServiceTest {

    private val workspacePath = Paths.get("/user/workspace")

    @Test
    fun `should extract diagnostics for error in BUILD file`() {
        val output = """
            Loading:
            Loading: 0 packages loaded
            Analyzing: target //path/to/package:test (0 packages loaded, 0 targets configured)
            INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets configured).
            INFO: Found 1 target...
            [0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt
            ERROR: /user/workspace/path/to/package/BUILD:12:37: in java_test rule //path/to/package:test: target '//path/to/another/package:lib' is not visible from target '//path/to/package:test'. Check the visibility declaration of the former target if you think the dependency is legitimate
            """.trimIndent()

        // when
        val diagnostics = extractDiagnostics(output, "//path/to/package:test")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/path/to/package/BUILD"),
                BuildTargetIdentifier("//path/to/package:test"),
                ErrorDiagnostic(
                    Position(12, 37),
                    "in java_test rule //path/to/package:test: target '//path/to/another/package:lib' is not visible from target '//path/to/package:test'. Check the visibility declaration of the former target if you think the dependency is legitimate"
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `should extract diagnostics for error in one source file`() {
        val output = """
            |Loading:
            |Loading: 0 packages loaded
            |Analyzing: target //path/to/package:test (0 packages loaded, 0 targets configured)
            |INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets configured).
            |INFO: Found 1 target...
            |[0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt
            |ERROR: /user/workspace/path/to/package/BUILD:12:37: scala //path/to/package:test failed: (Exit 1): scalac failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params
            |path/to/package/Test.scala:3: error: type mismatch;
            | found   : String("test")
            | required: Int
            |  val foo: Int = "test"
            |                 ^
            |one error found
            |Build failed
            |java.lang.RuntimeException: Build failed
            |  at io.bazel.rulesscala.scalac.ScalacWorker.compileScalaSources(ScalacWorker.java:280)
            |  at io.bazel.rulesscala.scalac.ScalacWorker.work(ScalacWorker.java:63)
            |  at io.bazel.rulesscala.worker.Worker.persistentWorkerMain(Worker.java:92)
            |  at io.bazel.rulesscala.worker.Worker.workerMain(Worker.java:46)
            |  at io.bazel.rulesscala.scalac.ScalacWorker.main(ScalacWorker.java:26)
            |Target //path/to/package:test failed to build
            |Use --verbose_failures to see the command lines of failed build steps.
            |INFO: Elapsed time: 0.220s, Critical Path: 0.09s
            |INFO: 2 processes: 2 internal.
            |FAILED: Build did NOT complete successfully""".trimMargin()

        // when
        val diagnostics = extractDiagnostics(output, "//path/to/package:test")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/path/to/package/Test.scala"),
                BuildTargetIdentifier("//path/to/package:test"),
                ErrorDiagnostic(
                    Position(3, 18),
                    """type mismatch;
                  | found   : String("test")
                  | required: Int
                  |  val foo: Int = "test"
                  |                 ^""".trimMargin()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `should extract diagnostics for error in 2 source files`() {
        val output = """
            |Loading:
            |Loading: 0 packages loaded
            |Analyzing: target //path/to/package:test (0 packages loaded, 0 targets configured)
            |INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets configured).
            |INFO: Found 1 target...
            |[0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt
            |ERROR: /user/workspace/path/to/package/BUILD:12:37: scala //path/to/package:test failed (Exit 1): scalac failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params
            |path/to/package/Test1.scala:21: error: type mismatch;
            |  found   : Int(42)
            |  required: String
            |    val x: String = 42
            |                    ^
            |path/to/package/Test2.scala:37: error: type mismatch;
            |  found   : String("test")
            |  required: Int
            |    val x: Int = "test"
            |                 ^
            |2 errors
            |Build failed
            |java.lang.RuntimeException: Build failed
            |  at io.bazel.rulesscala.scalac.ScalacWorker.compileScalaSources(ScalacWorker.java:280)
            |  at io.bazel.rulesscala.scalac.ScalacWorker.work(ScalacWorker.java:63)
            |  at io.bazel.rulesscala.worker.Worker.persistentWorkerMain(Worker.java:92)
            |  at io.bazel.rulesscala.worker.Worker.workerMain(Worker.java:46)
            |  at io.bazel.rulesscala.scalac.ScalacWorker.main(ScalacWorker.java:26)
            |Target //path/to/package:test failed to build
            |Use --verbose_failures to see the command lines of failed build steps.
            |INFO: Elapsed time: 0.216s, Critical Path: 0.12s
            |INFO: 2 processes: 2 internal.
            |FAILED: Build did NOT complete successfully
            |FAILED: Build did NOT complete successfully
            |FAILED: Build did NOT complete successfully
            |""".trimMargin()

        // when
        val diagnostics = extractDiagnostics(output, "//path/to/package:test")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/path/to/package/Test1.scala"),
                BuildTargetIdentifier("//path/to/package:test"),
                ErrorDiagnostic(
                    Position(21, 21),
                    """type mismatch;
                  |  found   : Int(42)
                  |  required: String
                  |    val x: String = 42
                  |                    ^""".trimMargin()
                )
            ),
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/path/to/package/Test2.scala"),
                BuildTargetIdentifier("//path/to/package:test"),
                ErrorDiagnostic(
                    Position(37, 18),
                    """type mismatch;
                  |  found   : String("test")
                  |  required: Int
                  |    val x: Int = "test"
                  |                 ^""".trimMargin()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `should extract diagnostics for 2 errors in 1 source file`() {
        val output = """
            |Loading:
            |Loading: 0 packages loaded
            |Analyzing: target //path/to/package:test (0 packages loaded, 0 targets configured)
            |INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets configured).
            |INFO: Found 1 target...
            |[0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt
            |ERROR: /user/workspace/path/to/package/BUILD:12:37: scala //path/to/package:test failed (Exit 1): scalac failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params
            |path/to/package/Test.scala:21: error: type mismatch;
            |  found   : Int(42)
            |  required: String
            |    val x: String = 42
            |                    ^
            |path/to/package/Test.scala:37: error: type mismatch;
            |  found   : String("test")
            |  required: Int
            |    val x: Int = "test"
            |                 ^
            |2 errors
            |Build failed
            |java.lang.RuntimeException: Build failed
            |  at io.bazel.rulesscala.scalac.ScalacWorker.compileScalaSources(ScalacWorker.java:280)
            |  at io.bazel.rulesscala.scalac.ScalacWorker.work(ScalacWorker.java:63)
            |  at io.bazel.rulesscala.worker.Worker.persistentWorkerMain(Worker.java:92)
            |  at io.bazel.rulesscala.worker.Worker.workerMain(Worker.java:46)
            |  at io.bazel.rulesscala.scalac.ScalacWorker.main(ScalacWorker.java:26)
            |Target //path/to/package:test failed to build
            |Use --verbose_failures to see the command lines of failed build steps.
            |INFO: Elapsed time: 0.216s, Critical Path: 0.12s
            |INFO: 2 processes: 2 internal.
            |FAILED: Build did NOT complete successfully
            |FAILED: Build did NOT complete successfully
            |FAILED: Build did NOT complete successfully
            """.trimMargin()
        // when
        val diagnostics = extractDiagnostics(output, "//path/to/package:test")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/path/to/package/Test.scala"),
                BuildTargetIdentifier("//path/to/package:test"),
                ErrorDiagnostic(
                    Position(21, 21),
                    """type mismatch;
                  |  found   : Int(42)
                  |  required: String
                  |    val x: String = 42
                  |                    ^""".trimMargin()
                ),
                ErrorDiagnostic(
                    Position(37, 18),
                    """type mismatch;
                  |  found   : String("test")
                  |  required: Int
                  |    val x: Int = "test"
                  |                 ^""".trimMargin()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `should parse kotlin errors`() {
        // given
        val output = """
      Invoking: /opt/example_mde/bin/bazel build --bes_backend=grpc://localhost:53483 --build_event_publish_all_actions -- //server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics //executioncontext/api:api //executioncontext/workspacecontext:workspacecontext //executioncontext/installationcontext/src/test/java/org/jetbrains/bsp/bazel/installationcontext/entities/mappers:InstallationContextDebuggerAddressEntityMapperTest //server/src/main/java/org/jetbrains/bsp/bazel/server/sync/dependencytree:dependencytree //server/src/main/java/org/jetbrains/bsp/bazel/server/bep:bep //executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser:parser_kotlin //e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV1Test //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/splitter:ProjectViewRawSectionsTest //executioncontext/installationcontext/src/test/java/org/jetbrains/bsp/bazel/installationcontext/entities/mappers:InstallationContextJavaPathEntityMapperTest //commons/src/test/java/org/jetbrains/bsp/bazel/commons:BetterFilesTest //e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV2Test //install/src/main/java/org/jetbrains/bsp/bazel/install:install //e2e:BazelBspActionGraphV1Test //e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava11ProjectTest //e2e:BazelBspCppProjectTest //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/sections:ProjectViewExcludableListSectionParserTest //executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections:sections //executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter:splitter //e2e:BazelBspActionGraphV2Test //server/src/main/java/org/jetbrains/bsp/bazel/server/bsp:bsp //executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries:entries //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/model:ProjectViewTest //e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspCppProjectTest //bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner:bazelrunner //e2e:BazelBspJava11ProjectTest //bazelrunner:bazelrunner //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:ProjectViewParserImplTest //executioncontext/installationcontext/src/test/java/org/jetbrains/bsp/bazel/installationcontext:InstallationContextConstructorTest //executioncontext/workspacecontext/src/test/java/org/jetbrains/bsp/bazel/workspacecontext:WorkspaceContextConstructorImplTest //server/src/main/java/org/jetbrains/bsp/bazel/server:server //executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities/mappers:mappers //executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext/entities:entities //bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/utils:utils //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/sections:ProjectViewListSectionParserTest //commons/src/main/java/org/jetbrains/bsp/bazel/commons:commons //server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/config:config //server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/services:services //bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/outputs:outputs //server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers:managers //server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info:info //e2e:BazelBspSampleRepoTest //bazelrunner:params //install:bsp-install //executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api/entries/mappers:mappers //server/src/test/java/org/jetbrains/bsp/bazel/server/sync:ProjectStorageTest //e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspSampleRepoTest //executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext/entries:entries //e2e:BazelBspEntireRepositoryImportTest //executioncontext/api/src/main/java/org/jetbrains/bsp/bazel/executioncontext/api:api //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:parser_mock_test_impl //executioncontext/projectview:parser //server/src/main/java/org/jetbrains/bsp/bazel:bsp-install //executioncontext/workspacecontext/src/test/java/org/jetbrains/bsp/bazel/workspacecontext:WorkspaceContextTest //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/splitter:ProjectViewRawSectionTest //e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspEntireRepositoryImportTest //e2e:BazelBspJava8ProjectTest //executioncontext/installationcontext/src/test/java/org/jetbrains/bsp/bazel/installationcontext:InstallationContextTest //server/src/main/java/org/jetbrains/bsp/bazel/server:bsp-run //executioncontext/projectview:model //commons:commons //executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext/entries/mappers:mappers //server/src/main/java/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/sections:ProjectViewSingletonSectionParserTest //e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava8ProjectTest //executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections:sections //logger/src/main/java/org/jetbrains/bsp/bazel/logger:logger //server/src/test/java/org/jetbrains/bsp/bazel/server/bsp/utils:InternalAspectsResolverTest //executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/splitter:ProjectViewSectionSplitterTest //executioncontext/workspacecontext/src/main/java/org/jetbrains/bsp/bazel/workspacecontext:workspacecontext //logger:logger //server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics/parser:parser //server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics //executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model:model //install/src/main/java/org/jetbrains/bsp/bazel/install/cli:cli //executioncontext/workspacecontext/src/test/java/org/jetbrains/bsp/bazel/workspacecontext/entries/mappers:WorkspaceContextTargetsEntityMapperTest //server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils:utils //executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext:installationcontext //server/src/test/java/org/jetbrains/bsp/bazel/server/bsp/utils:SourceRootGuesserTest //install:install //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/dependencytree:DependencyTreeTest //server/src/main/java/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_proto //server/src/main/java/org/jetbrains/bsp/bazel/server/sync:sync //executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser:parser //server/src/main/java/org/jetbrains/bsp/bazel:bsp-lib //bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner/params:params //commons/src/test/java/org/jetbrains/bsp/bazel/commons:FormatTest //e2e:all //e2e/src/main/java/org/jetbrains/bsp/bazel/base:base
      Loading:
      Loading: 0 packages loaded
      WARNING: The target pattern '//e2e:all' is ambiguous: ':all' is both a wildcard, and the name of an existing sh_binary rule; using the latter interpretation
      Analyzing: 89 targets (0 packages loaded, 0 targets configured)
      INFO: Analyzed 89 targets (0 packages loaded, 0 targets configured).
      INFO: Found 89 targets...
      [0 / 4] [Prepa] BazelWorkspaceStatusAction stable-status.txt
      [1 / 4] KotlinCompile //server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics { kt: 1, java: 0, srcjars: 0 } for darwin; 1s worker
      ERROR: /Users/uwawrzyk/workspace/bazel-bsp/server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics/BUILD:3:15: KotlinCompile //server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics { kt: 1, java: 0, srcjars: 0 } for darwin failed: (Exit 1): build failed: error executing command bazel-out/host/bin/external/io_bazel_rules_kotlin/src/main/kotlin/build ... (remaining 1 argument skipped)
      server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics/DiagnosticsServiceTest.kt:12:18: error: type mismatch: inferred type is String but Int was expected
      val int: Int = "STRING"
      ^
      server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics/DiagnosticsServiceTest.kt:13:24: error: the integer literal does not conform to the expected type String
      val string: String = 1
      ^
      kwi 13, 2022 12:07:04 PM worker request 0
      SEVERE: Compilation failure: compile phase failed:
      server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics/DiagnosticsServiceTest.kt:12:18: error: type mismatch: inferred type is String but Int was expected
      val int: Int = "STRING"
      ^
      server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics/DiagnosticsServiceTest.kt:13:24: error: the integer literal does not conform to the expected type String
      val string: String = 1
      ^
      INFO: Elapsed time: 2.590s, Critical Path: 1.90s
      INFO: 2 processes: 2 internal.
      FAILED: Build did NOT complete successfully
      Command completed in 2.7s (exit code 1)
    """.trimIndent()

        // when
        val diagnostics =
            extractDiagnostics(output, "//server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics/DiagnosticsServiceTest.kt"),
                BuildTargetIdentifier("//server/src/test/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics"),
                ErrorDiagnostic(
                    Position(12, 18),
                    """type mismatch: inferred type is String but Int was expected
                  |val int: Int = "STRING"
                  |^""".trimMargin()
                ),
                ErrorDiagnostic(
                    Position(13, 24),
                    """the integer literal does not conform to the expected type String
                  |val string: String = 1
                  |^""".trimMargin()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `parse scala errors with warnings`() {
        // given
        val output = """[88 / 93] [Prepa] BazelWorkspaceStatusAction stable-status.txt; 12s ... (3 actions, 2 running)
                   |ERROR: /user/workspace/project/src/main/scala/com/example/project/BUILD:1:14: scala //project/src/main/scala/com/example/project:project failed: (Exit 1): scalac failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac '--jvm_flag=-Xss10m' '--jvm_flag=-Xms512m' '--jvm_flag=-Xmx5G' ... (remaining 1 argument skipped)
                   |project/src/main/scala/com/example/project/File1.scala:11: error: type mismatch;
                   |  found   : String("sd")
                   |  required: Int
                   |    val x: Int = "sd"
                   |                 ^
                   |project/src/main/scala/com/example/project/File1.scala:11: warning: local val x in method promote is never used
                   |  val x: Int = "sd"
                   |      ^
                   |project/src/main/scala/com/example/project/File2.scala:26: warning: private val versionsWriter in object File2 is never used
                   |  private implicit val versionsWriter: ConfigWriter[Versions] = deriveWriter[Versions]
                   |                       ^
                   |project/src/main/scala/com/example/project/File2.scala:28: warning: private val File2ProtocolWriter in object File2 is never used
                   |private implicit val File2ProtocolWriter: ConfigWriter[File2Protocol] =
                   |                     ^
                   |three warnings found
                   |one error found
                   |Build failed""".trimMargin()

        // when
        val diagnostics = extractDiagnostics(output, "//project/src/main/scala/com/example/project:project")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/project/src/main/scala/com/example/project/File1.scala"),
                BuildTargetIdentifier("//project/src/main/scala/com/example/project:project"),
                ErrorDiagnostic(
                    Position(11, 18),
                    """type mismatch;
                  |  found   : String("sd")
                  |  required: Int
                  |    val x: Int = "sd"
                  |                 ^""".trimMargin()
                ),
                WarningDiagnostic(
                    Position(11, 7),
                    """local val x in method promote is never used
                   |  val x: Int = "sd"
                   |      ^""".trimMargin()
                )
            ),
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/project/src/main/scala/com/example/project/File2.scala"),
                BuildTargetIdentifier("//project/src/main/scala/com/example/project:project"),
                WarningDiagnostic(
                    Position(26, 24),
                    """private val versionsWriter in object File2 is never used
                   |  private implicit val versionsWriter: ConfigWriter[Versions] = deriveWriter[Versions]
                   |                       ^""".trimMargin()
                ),
                WarningDiagnostic(
                    Position(28, 22),
                    """private val File2ProtocolWriter in object File2 is never used
                   |private implicit val File2ProtocolWriter: ConfigWriter[File2Protocol] =
                   |                     ^""".trimMargin()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `parse scala warnings`() {
        // given
        val output = """INFO: Invocation ID: 80bcd33a-6782-438c-b212-4f28df8a8433
                   |INFO: Analyzed 12 targets (0 packages loaded, 0 targets configured).
                   |INFO: Found 12 targets...
                   |INFO: From scala //intellij/release-tool/src/main/scala/com/intellij/releasetool:releasetool:
                   |intellij/release-tool/src/main/scala/com/intellij/releasetool/PluginResolver.scala:14: warning: match may not be exhaustive.
                   |It would fail on the following inputs: Bundled(_), BundledCrossVersion(_, _, _), Direct(_), Empty(), FromSources(_, _), Versioned((x: String forSome x not in "com.intellijUpdaterPlugin"), _, _)
                   |    key match {
                   |    ^
                   |intellij/release-tool/src/main/scala/com/intellij/releasetool/Json.scala:29: warning: trait ScalaObjectMapper in package scala is deprecated (since 2.12.1): ScalaObjectMapper is deprecated because Manifests are not supported in Scala3, you might want to use ClassTagExtensions as a replacement
                   |    val m = new ObjectMapper() with ScalaObjectMapper
                   |                                    ^
                   |four warnings found
                   |INFO: Elapsed time: 35,391s, Critical Path: 17,41s
                   |INFO: 2 processes: 1 disk cache hit, 1 internal.
                   |INFO: Build Event Protocol files produced successfully.
                   |INFO: Build completed successfully, 2 total actions""".trimMargin()

        // when
        val diagnostics =
            extractDiagnostics(output, "//intellij/release-tool/src/main/scala/com/intellij/releasetool:releasetool")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/intellij/release-tool/src/main/scala/com/intellij/releasetool/PluginResolver.scala"),
                BuildTargetIdentifier("//intellij/release-tool/src/main/scala/com/intellij/releasetool:releasetool"),
                WarningDiagnostic(
                    Position(14, 5),
                    """match may not be exhaustive.
                   |It would fail on the following inputs: Bundled(_), BundledCrossVersion(_, _, _), Direct(_), Empty(), FromSources(_, _), Versioned((x: String forSome x not in "com.intellijUpdaterPlugin"), _, _)
                   |    key match {
                   |    ^""".trimMargin()
                )
            ),
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/intellij/release-tool/src/main/scala/com/intellij/releasetool/Json.scala"),
                BuildTargetIdentifier("//intellij/release-tool/src/main/scala/com/intellij/releasetool:releasetool"),
                WarningDiagnostic(
                    Position(29, 37),
                    """trait ScalaObjectMapper in package scala is deprecated (since 2.12.1): ScalaObjectMapper is deprecated because Manifests are not supported in Scala3, you might want to use ClassTagExtensions as a replacement
                   |    val m = new ObjectMapper() with ScalaObjectMapper
                   |                                    ^""".trimMargin()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `parse java messy errors`() {
        // given
        val output =
            """ERROR: /Users/uwawrzyk/workspace/bazel-bsp/server/src/main/java/org/jetbrains/bsp/bazel/server/sync/BUILD:3:13: Compiling Java headers server/src/main/java/org/jetbrains/bsp/bazel/server/sync/libsync-hjar.jar (34 source files) failed: (Exit 1): java failed: error executing command external/remotejdk11_macos/bin/java -XX:-CompactStrings '--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED' '--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED' ... (remaining 95 arguments skipped)
                   |
                   |Use --sandbox_debug to see verbose messages from the sandbox
                   |server/src/main/java/org/jetbrains/bsp/bazel/server/sync/ProjectResolver.java:20: error: symbol not found org.jetbrains.bsp.bazel.server.bsp.config.ProjectViewProvider
                   |import org.jetbrains.bsp.bazel.server.bsp.config.ProjectViewProvider;
                   |       ^
                   |server/src/main/java/org/jetbrains/bsp/bazel/server/sync/ProjectResolver.java:37: error: could not resolve ProjectViewProvider
                   |      ProjectViewProvider projectViewProvider,
                   |      ^
                   |ERROR: /Users/uwawrzyk/workspace/bazel-bsp/server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/services/BUILD:3:13 Building server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/services/libservices.jar (1 source file) failed: (Exit 1): java failed: error executing command external/remotejdk11_macos/bin/java -XX:-CompactStrings '--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED' '--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED' ... (remaining 95 arguments skipped)
                   |
                   |Use --sandbox_debug to see verbose messages from the sandbox
                   |INFO: Elapsed time: 2.163s, Critical Path: 1.62s
                   |INFO: 6 processes: 6 internal.
                   |FAILED: Build did NOT complete successfully""".trimMargin()

        // when
        val diagnostics = extractDiagnostics(output, "//project/src/main/scala/com/example/project:project")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/server/src/main/java/org/jetbrains/bsp/bazel/server/sync/ProjectResolver.java"),
                BuildTargetIdentifier("//project/src/main/scala/com/example/project:project"),
                ErrorDiagnostic(
                    Position(20, 8),
                    """symbol not found org.jetbrains.bsp.bazel.server.bsp.config.ProjectViewProvider
                   |import org.jetbrains.bsp.bazel.server.bsp.config.ProjectViewProvider;
                   |       ^""".trimMargin()
                ),
                ErrorDiagnostic(
                    Position(37, 7),
                    """could not resolve ProjectViewProvider
                   |      ProjectViewProvider projectViewProvider,
                   |      ^""".trimMargin()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun `should parse java symbol not found error`() {
        // given
        val output = """
            Loading: 
            Loading: 0 packages loaded
            WARNING: The target pattern '//e2e:all' is ambiguous: ':all' is both a wildcard, and the name of an existing sh_binary rule; using the latter interpretation
            Analyzing: 91 targets (0 packages loaded, 0 targets configured)
            INFO: Analyzed 91 targets (0 packages loaded, 0 targets configured).
            INFO: Found 91 targets...
            [0 / 5] [Prepa] BazelWorkspaceStatusAction stable-status.txt
            INFO: From KotlinCompile //server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics { kt: 9, java: 0, srcjars: 0 } for darwin:
            [2 / 20] [Prepa] JdepsMerge //server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics:diagnostics { jdeps: 2 }
            warning: '-Xuse-experimental' is deprecated and will be removed in a future release
            ERROR: /Users/marcin.abramowicz/dev/ww/bazel-bsp/server/src/main/java/org/jetbrains/bsp/bazel/server/bep/BUILD:3:13: Building server/src/main/java/org/jetbrains/bsp/bazel/server/bep/libbep-class.jar (4 source files) failed: (Exit 1): java failed: error executing command external/remotejdk11_macos/bin/java -XX:-CompactStrings '--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED' '--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED' ... (remaining 17 arguments skipped)
            server/src/main/java/org/jetbrains/bsp/bazel/server/bep/BepServer.java:55: error: cannot find symbol
                return new BepStreamObserver(thi, responseObserver);
                                             ^
              symbol:   variable thi
              location: class BepServer
            INFO: Elapsed time: 2.279s, Critical Path: 2.07s
            INFO: 6 processes: 3 internal, 3 worker.
            FAILED: Build did NOT complete successfully
      """.trimIndent()

        // when
        val diagnostics = extractDiagnostics(output, "//project/src/main/scala/com/example/project:project")

        // then
        val expected = listOf(
            PublishDiagnosticsParams(
                TextDocumentIdentifier("file:///user/workspace/server/src/main/java/org/jetbrains/bsp/bazel/server/bep/BepServer.java"),
                BuildTargetIdentifier("//project/src/main/scala/com/example/project:project"),
                ErrorDiagnostic(
                    Position(55, 34),
                    """
                      cannot find symbol
                          return new BepStreamObserver(thi, responseObserver);
                                                       ^
                        symbol:   variable thi
                        location: class BepServer
                  """.trimIndent()
                )
            )
        )
        diagnostics shouldContainExactlyInAnyOrder expected
    }

    private fun PublishDiagnosticsParams(
        textDocument: TextDocumentIdentifier,
        buildTarget: BuildTargetIdentifier,
        vararg diagnostics: BspDiagnostic
    ): PublishDiagnosticsParams {
        return PublishDiagnosticsParams(
            textDocument,
            buildTarget,
            diagnostics.asList(),
            false
        )
    }

    private fun ErrorDiagnostic(position: Position, message: String): BspDiagnostic =
        createDiagnostic(position, message, DiagnosticSeverity.ERROR)

    private fun WarningDiagnostic(position: Position, message: String): BspDiagnostic =
        createDiagnostic(position, message, DiagnosticSeverity.WARNING)

    private fun createDiagnostic(position: Position, message: String, severity: DiagnosticSeverity): BspDiagnostic {
        val adjustedPosition = BspPosition(position.line - 1, position.character - 1)
        return BspDiagnostic(Range(adjustedPosition, adjustedPosition), message).apply { this.severity = severity }
    }

    private fun extractDiagnostics(output: String, buildTarget: String): List<PublishDiagnosticsParams>? {
        return DiagnosticsService(workspacePath).extractDiagnostics(output, buildTarget, null)
    }
}
