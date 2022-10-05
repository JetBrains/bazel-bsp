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

private data class StartedBuildTarget(
  val uri: String,
  val taskId: TaskId
)

class JUnit5TestParser(
  private val bspClientTestNotifier: BspClientTestNotifier
) {
  fun processTestOutput(testResult: BazelProcessResult) {
    val startedSuites: Stack<TestOutputLine> = Stack()
    var startedBuildTarget: StartedBuildTarget? = null
    var previousOutputLine: TestOutputLine? = null

    testResult.stdoutLines.forEach {
      if (startedBuildTarget == null) {
        startedBuildTarget = checkLineForTestingBeginning(it)
      } else {
        val testingEndedMatcher = testingEndedPattern.matcher(it)
        val currentOutputLine = getCurrentOutputLine(it)
        if (currentOutputLine != null) {
          processPreviousOutputLine(previousOutputLine, currentOutputLine, startedSuites)
          previousOutputLine = currentOutputLine
        } else if (testingEndedMatcher.find()) {
          processTestingEndingLine(startedSuites, previousOutputLine, testingEndedMatcher, startedBuildTarget)
          startedBuildTarget = null
        }
      }
    }
  }

  private fun processTestingEndingLine(
    startedSuites: Stack<TestOutputLine>,
    previousOutputLine: TestOutputLine?,
    testingEndedMatcher: Matcher,
    startedBuildTarget: StartedBuildTarget?
  ) {
    startAndFinishTest(startedSuites, previousOutputLine!!)
    while (startedSuites.isNotEmpty()) {
      finishTopmostSuite(startedSuites)
    }
    val time = testingEndedMatcher.group("time").toLongOrNull() ?: 0
    endTesting(startedBuildTarget!!, time)
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
        removeAllFinishedSuites(startedSuites, currentOutputLine)
      }
    }
  }

  private fun removeAllFinishedSuites(
    startedSuites: Stack<TestOutputLine>,
    currentOutputLine: TestOutputLine
  ) {
    while (startedSuites.isNotEmpty() && startedSuites.peek().indent >= currentOutputLine.indent) {
      finishTopmostSuite(startedSuites)
    }
  }

  private fun getCurrentOutputLine(line: String): TestOutputLine? {
    val currentLineMatcher = testLinePattern.matcher(line)
    return if (currentLineMatcher.find()) {
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
  }

  private fun checkLineForTestingBeginning(line: String): StartedBuildTarget? {
    val testingStartMatcher = testingStartPattern.matcher(line)
    return if (testingStartMatcher.find())
      StartedBuildTarget(testingStartMatcher.group("target"), TaskId(testUUID())).also { 
        beginTesting(it)
      }
    else null
  }
  
  private fun beginTesting(startedBuildTarget: StartedBuildTarget) {
    bspClientTestNotifier.beginTestTarget(BuildTargetIdentifier(startedBuildTarget.uri), startedBuildTarget.taskId)
  }

  private fun endTesting(testTarget: StartedBuildTarget, millis: Long) {
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
