package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.*
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult
import org.jetbrains.bsp.bazel.bazelrunner.outputs.OutputCollector
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.*

class JUnit5TestParserTest {

  @Test
  fun `should finish all started tests`() {
    bspClient.startedSuiteStack.empty() shouldBe true
  }

  @Test
  fun `should conduct correct number of tests`() {
    bspClient.conductedTests.size shouldBeExactly 18
  }

  @Test
  fun `should conduct correct number of suites`() {
    bspClient.conductedSuites.size shouldBeExactly 4
  }

  @Test
  fun `should fail one test`() {
    val failed = bspClient.conductedTests.filter { it.status == TestStatus.FAILED }
    failed.size shouldBeExactly 1
    failed.firstOrNull()?.name shouldBe "should return ScalaLanguagePlugin for Scala Language()"
  }

  @Test
  fun `should parse nested tests correctly`() {
    bspClient.getParentByName("should really work()", false) shouldBe "An inner inner test"
    bspClient.getParentByName("An inner inner test", true) shouldBe "Tests for the method shouldGetPlugin"
    bspClient.getParentByName("Tests for the method shouldGetPlugin", true) shouldBe "LanguagePluginServiceTest"
    bspClient.getParentByName("LanguagePluginServiceTest", true) shouldBe null
  }

  @Test
  fun `should distinguish similarly-named tests and suites`() {
    bspClient.conductedTests.count { it.name == "Tests for the method shouldGetPlugin" } shouldBeExactly 1
    bspClient.conductedSuites.count { it.name == "Tests for the method shouldGetPlugin" } shouldBeExactly 1
  }

  @Test
  fun `should parse tests with trimmed name`() {
    bspClient.conductedTests.count {
      it.name == "should return CppLanguagePlugin for Cpp Languahgfdhgusdhgfihdfhgosihdfgoisdfogih..."
    } shouldBeExactly 1
  }

  @Test
  fun `should detect testing duration`() {
    bspClient.duration shouldBeExactly 382
  }

  companion object {
    private val outputString = """
    |Invoking: /opt/homebrew/bin/bazel build --bes_backend=grpc://localhost:60052 --define=ORIGINID=test-a9896735-e16b-49f0-b013-7d337213422e -- //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest
    |Loading: 
    |Loading: 0 packages loaded
    |INFO: Build option --define has changed, discarding analysis cache.
    |Analyzing: target //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest (0 packages loaded, 0 targets configured)
    |INFO: Analyzed target //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest (135 packages loaded, 1984 targets configured).
    |INFO: Found 1 target...
    |[0 / 31] [Prepa] BazelWorkspaceStatusAction stable-status.txt
    |Target //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest up-to-date:
    |  bazel-bin/server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages/LanguagePluginServiceTest.jar
    |  bazel-bin/server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages/LanguagePluginServiceTest.jdeps
    |INFO: Elapsed time: 0.854s, Critical Path: 0.01s
    |INFO: 1 process: 1 internal.
    |INFO: Build completed successfully, 1 total action
    |Command completed in 932ms (exit code 0)
    |Invoking: /opt/homebrew/bin/bazel test --bes_backend=grpc://localhost:60052 --test_output=all --define=ORIGINID=test-a9896735-e16b-49f0-b013-7d337213422e -- //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest
    |Loading: 
    |Loading: 0 packages loaded
    |Analyzing: target //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest (0 packages loaded, 0 targets configured)
    |INFO: Analyzed target //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest (0 packages loaded, 0 targets configured).
    |INFO: Found 1 test target...
    |[0 / 1] [Prepa] BazelWorkspaceStatusAction stable-status.txt
    |Target //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest up-to-date:
    |  bazel-bin/server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages/LanguagePluginServiceTest.jar
    |  bazel-bin/server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages/LanguagePluginServiceTest.jdeps
    |INFO: Elapsed time: 0.149s, Critical Path: 0.01s
    |INFO: 1 process: 1 internal.
    |INFO: Build completed successfully, 1 total action
    |PASSED: //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest (see /private/var/tmp/_bazel_testuser/f98dd469277d467079cd482bf6a8e08f/execroot/bazel_bsp/bazel-out/darwin_arm64-fastbuild/testlogs/server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages/LanguagePluginServiceTest/test.log)
    |INFO: From Testing //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest
    |==================== Test output for //server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest:
    |Thanks for using JUnit! Support its development at https://junit.org/sponsoring
    |╷
    |└─ JUnit Jupiter ✔
    |   └─ LanguagePluginServiceTest ✔
    |      ├─ Tests for the method shouldGetPlugin ✔
    |      │  ├─ should return CppLanguagePlugin for Cpp Languahgfdhgusdhgfihdfhgosihdfgoisdfogih... ✔
    |      │  ├─ should return JavaLanguagePlugin for Kotlin Language() ✔
    |      │  ├─ Tests for the method shouldGetPlugin ✔
    |      │  ├─ should return ScalaLanguagePlugin for Scala Language() ✘
    |      │  ├─ should return ThriftLanguagePlugin for Thrift Language() ✔
    |      │  ├─ should return EmptyLanguagePlugin for no Language() ✔
    |      │  └─ An inner inner test ✔
    |      │     └─ should really work() ✔
    |      └─ Tests for the method shouldGetSourceSet ✔
    |         ├─ should not return tmpRepo for Kotlin Language for empty file() ✔
    |         ├─ should return sourceSet for Java Language() ✔
    |         ├─ should not return tmpRepo for empty file in Java Language() ✔
    |         ├─ should return sourceSet for Kotlin Language() ✔
    |         ├─ should not return tmpRepo for Scala Language from empty package declaration() ✔
    |         ├─ should return sourceSet for Scala Language from multi line package declaration() ✔
    |         ├─ should not return tmpRepo for Java Language with wrong package declaration() ✔
    |         ├─ should not return tmpRepo for Thrift Language() ✔
    |         ├─ should return sourceSet for Scala Language from two line package declaration() ✔
    |         ├─ should return null for no Language() ✔
    |         └─ should return sourceSet for Scala Language from one line package declaration() ✔
    |Test run finished after 382 ms
    |[         5 containers found      ]
    |[         0 containers skipped    ]
    |[         5 containers started    ]
    |[         0 containers aborted    ]
    |[         5 containers successful ]
    |[         0 containers failed     ]
    |[        18 tests found           ]
    |[         0 tests skipped         ]
    |[        18 tests started         ]
    |[         0 tests aborted         ]
    |[        17 tests successful      ]
    |[         1 tests failed          ]
    |================================================================================
    |//server/src/test/java/org/jetbrains/bsp/bazel/server/sync/languages:LanguagePluginServiceTest (cached) PASSED in 1.7s
    |Executed 0 out of 1 test: 1 test passes.
    |Command completed in 236ms (exit code 0)""".trimMargin()

    private lateinit var bazelProcessResult: BazelProcessResult
    private lateinit var jUnit5TestParser: JUnit5TestParser
    private lateinit var bspClient: MockBspClient

    @JvmStatic
    @BeforeAll
    fun init() {
      val collector = OutputCollector()
      outputString.lines().forEach { collector.onNextLine(it) }
      bazelProcessResult = BazelProcessResult(collector, OutputCollector(), 0)
      val testNotifier = BspClientTestNotifier()
      bspClient = MockBspClient()
      testNotifier.initialize(bspClient)
      jUnit5TestParser = JUnit5TestParser(testNotifier)
      jUnit5TestParser.processTestOutput(bazelProcessResult)
    }
  }
}

private data class JUnitTest(
  val status: TestStatus,
  val name: String?,
  val parent: String?
)

private data class JUnitSuite(
  val name: String?,
  val parent: String?
)

private class MockBspClient : BuildClient {
  val startedSuiteStack = Stack<String?>()
  var startedTest: String? = null
  val conductedTests = mutableListOf<JUnitTest>()
  val conductedSuites = mutableListOf<JUnitSuite>()

  val outputLines = mutableListOf<String?>()
  var duration = -1L

  override fun onBuildShowMessage(params: ShowMessageParams?) {
    outputLines.add(params?.message)
  }

  override fun onBuildLogMessage(params: LogMessageParams?) {
    outputLines.add(params?.message)
  }

  override fun onBuildTaskStart(params: TaskStartParams?) {
    when (params?.dataKind) {
      TaskStartDataKind.TEST_START -> {
        val testStart = params.data as? TestStart
        val isSuite = params.message.startsWith("<S>")
        val displayName = testStart?.displayName
        if (isSuite) startedSuiteStack.push(displayName)
        else startedTest = displayName
      }
      TaskStartDataKind.TEST_TASK -> {
        // ignore
      }
    }
  }

  override fun onBuildTaskProgress(params: TaskProgressParams?) {
    // ignore
  }

  override fun onBuildTaskFinish(params: TaskFinishParams?) {
    when (params?.dataKind) {
      TaskFinishDataKind.TEST_FINISH -> {
        val testFinish = params.data as TestFinish
        val isSuite = params.message.startsWith("<S>")
        val displayName = testFinish.displayName
        if (isSuite && displayName == stackPeekOrNull(startedSuiteStack)) {
          stackPopOrNull(startedSuiteStack)
          conductedSuites.add(JUnitSuite(displayName, stackPeekOrNull(startedSuiteStack)))
        } else if (!isSuite && displayName == startedTest) {
          conductedTests.add(JUnitTest(testFinish.status, displayName, stackPeekOrNull(startedSuiteStack)))
          startedTest = null
        }
      }

      TaskFinishDataKind.TEST_REPORT -> {
        val report = params.data as? TestReport
        duration = (report?.time) ?: -1
      }
    }
  }

  override fun onRunPrintStdout(p0: PrintParams?) {
    // ignore
  }

  override fun onRunPrintStderr(p0: PrintParams?) {
    // ignore
  }

  override fun onBuildPublishDiagnostics(params: PublishDiagnosticsParams?) {
    // ignore
  }

  override fun onBuildTargetDidChange(params: DidChangeBuildTarget?) {
    // ignore
  }

  private fun <T> stackPeekOrNull(stack: Stack<T>) =
    if (stack.isNotEmpty()) stack.peek() else null

  private fun <T> stackPopOrNull(stack: Stack<T>) =
    if (stack.isNotEmpty()) stack.pop() else null

  fun getParentByName(name: String, isSuite: Boolean): String? =
    if (isSuite) conductedSuites.find { it.name == name } ?.parent
    else conductedTests.find { it.name == name } ?.parent
}
