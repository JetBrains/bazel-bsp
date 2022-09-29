package org.jetbrains.bsp.bazel.server.sync
// TODO - rethink the location

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.TaskId
import ch.epfl.scala.bsp4j.TestReport
import ch.epfl.scala.bsp4j.TestStatus
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

private data class TestOutputLine(
  val name: String,
  val passed: Boolean,
  val message: String,
  val indent: Int,
  val taskId: TaskId?
)

private data class StartedTestingTarget(
  val uri: String,
  val taskId: TaskId
)

class JUnitTestParser(
  private val bspClientTestNotifier: BspClientTestNotifier
) {
  fun processTestOutputWithJUnit(testResult: BazelProcessResult) {
    val startedSuites: Stack<TestOutputLine> = Stack()
    var currentTestTarget: StartedTestingTarget? = null
    var previousOutputLine: TestOutputLine? = null

    testResult.stdoutLines.forEach {
      if (currentTestTarget == null) {
        currentTestTarget = beginTesting(it)
      } else {
        val currentLineMatcher = testLinePattern.matcher(it)
        val testingEndedMatcher = testingEndedPattern.matcher(it)
        val currentOutputLine = getCurrentOutputLine(currentLineMatcher)
        if (currentOutputLine != null) {
          processPreviousOutputLine(previousOutputLine, currentOutputLine, startedSuites)
          previousOutputLine = currentOutputLine
        } else if (testingEndedMatcher.find()) {
          processTestingEndingLine(startedSuites, previousOutputLine, testingEndedMatcher, currentTestTarget)
          currentTestTarget = null
        }
      }
    }
  }

  private fun processTestingEndingLine(
    startedSuites: Stack<TestOutputLine>,
    previousOutputLine: TestOutputLine?,
    testingEndedMatcher: Matcher,
    currentTestTarget: StartedTestingTarget?
  ) {
    startAndFinishTest(startedSuites, previousOutputLine!!)
    while (startedSuites.isNotEmpty()) {
      finishTopmostSuite(startedSuites)
    }
    val time = testingEndedMatcher.group("time").toLongOrNull() ?: 0
    endTesting(currentTestTarget!!, time)
  }

  private fun processPreviousOutputLine(
    previousOutputLine: TestOutputLine?,
    currentOutputLine: TestOutputLine,
    startedSuites: Stack<TestOutputLine>
  ) {
    if (previousOutputLine != null) {
      if (currentOutputLine.indent > previousOutputLine.indent) {
        startSuite(startedSuites, previousOutputLine)
      } else {
        startAndFinishTest(startedSuites, previousOutputLine)
        while (startedSuites.isNotEmpty() && startedSuites.peek().indent >= currentOutputLine.indent) {
          finishTopmostSuite(startedSuites)
        }
      }
    }
  }

  private fun getCurrentOutputLine(currentLineMatcher: Matcher) = if (currentLineMatcher.find()) {
    TestOutputLine(
      currentLineMatcher.group("name"),
      currentLineMatcher.group("result") == "✔",
      currentLineMatcher.group("message"),
      currentLineMatcher.start("name"),
      null
    )
  } else {
    null
  }

  private fun beginTesting(line: String): StartedTestingTarget? {
    val testingStartMatcher = testingStartPattern.matcher(line)
    if (testingStartMatcher.find()) {
      val currentTestTarget = StartedTestingTarget(testingStartMatcher.group("target"), TaskId(testUUID()))
      bspClientTestNotifier.beginTestTarget(BuildTargetIdentifier(currentTestTarget.uri), currentTestTarget.taskId)
      return currentTestTarget
    }
    return null
  }

  private fun endTesting(testTarget: StartedTestingTarget, millis: Long) {
    val report = TestReport(BuildTargetIdentifier(testTarget.uri), 0, 0, 0, 0, 0)
    report.time = millis
    bspClientTestNotifier.endTestTarget(report, testTarget.taskId)
  }

  private fun startSuite(startedSuites: Stack<TestOutputLine>, suite: TestOutputLine) {
    val newTaskId = TaskId(testUUID())
    newTaskId.parents = generateParentList(startedSuites)
    val updatedSuite = suite.copy(taskId = newTaskId)
    bspClientTestNotifier.startTest(true, updatedSuite.name, newTaskId)
    startedSuites.push(updatedSuite)
  }

  private fun finishTopmostSuite(startedSuites: Stack<TestOutputLine>) {
    val finishingSuite = startedSuites.pop()
    bspClientTestNotifier.finishTestSuite(
      finishingSuite!!.name,
      finishingSuite.taskId
    )
  }

  private fun startAndFinishTest(startedSuites: Stack<TestOutputLine>, test: TestOutputLine) {
    val newTaskId = TaskId(testUUID())
    newTaskId.parents = generateParentList(startedSuites)
    bspClientTestNotifier.startTest(false, test.name, newTaskId)
    bspClientTestNotifier.finishTest(
      false,
      test.name,
      newTaskId,
      if (test.passed) TestStatus.PASSED else TestStatus.FAILED,
      test.message
    )
  }

  private fun generateParentList(parents: Stack<TestOutputLine>): List<String> =
    parents.toList().mapNotNull { it.taskId?.id }

  private fun testUUID(): String = "test-" + UUID.randomUUID().toString()

  companion object {
    private val testingStartPattern = Pattern.compile("^=+\\hTest\\houtput\\hfor\\h(?<target>[^:]*:[^:]+):")
    private val testLinePattern =
      Pattern.compile("^(?:[\\h└├│]{3})+[└├│]─\\h(?<name>.+)\\h(?<result>[✔✘])\\h?(?<message>.*)\$")
    private val testingEndedPattern = Pattern.compile("^Test\\hrun\\hfinished\\hafter\\h(?<time>\\d+)\\hms")
  }
}
