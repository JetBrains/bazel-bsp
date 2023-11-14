package org.jetbrains.bsp.bazel.logger

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.StatusCode
import ch.epfl.scala.bsp4j.TaskFinishDataKind
import ch.epfl.scala.bsp4j.TaskFinishParams
import ch.epfl.scala.bsp4j.TaskId
import ch.epfl.scala.bsp4j.TaskStartDataKind
import ch.epfl.scala.bsp4j.TaskStartParams
import ch.epfl.scala.bsp4j.TestFinish
import ch.epfl.scala.bsp4j.TestReport
import ch.epfl.scala.bsp4j.TestStart
import ch.epfl.scala.bsp4j.TestStatus
import ch.epfl.scala.bsp4j.TestTask

class BspClientTestNotifier {
  private lateinit var bspClient: BuildClient

  /**
   * Notifies the client about starting a single test or a test suite
   *
   * @param isSuite     `true` if a test suite has been started, `false` if it was a single test instead
   * @param displayName display name of the started test / test suite
   * @param taskId      TaskId of the started test / test suite
   */
  fun startTest(isSuite: Boolean, displayName: String?, taskId: TaskId?) {
    val testStart = TestStart(displayName)
    val taskStartParams = TaskStartParams(taskId)
    taskStartParams.dataKind = TaskStartDataKind.TEST_START
    taskStartParams.data = testStart
    taskStartParams.message = if (isSuite) SUITE_TAG else TEST_TAG
    bspClient.onBuildTaskStart(taskStartParams)
  }

  /**
   * Notifies the client about finishing a test suite. Synonymous to:
   * ```
   * finishTest(true, displayName, taskId, TestStatus.PASSED, "")
   * ```
   *
   * @param displayName display name of the finished test suite
   * @param taskId      TaskId if the finished test suite
   */
  fun finishTestSuite(displayName: String?, taskId: TaskId?) {
    finishTest(true, displayName, taskId, TestStatus.PASSED, "")
  }

  /**
   * Notifies the client about finishing a single test or a test suite
   *
   * @param isSuite     `true` if a test suite has been finished, `false` if it was a single
   * test instead. **For test suites, using `finishTestSuite` is recommended**
   * @param displayName display name of the finished test / test suite
   * @param taskId      TaskId of the finished test / test suite
   * @param status      status of the performed test (does not matter for test suites)
   * @param message     additional message concerning the test execution
   */
  fun finishTest(isSuite: Boolean, displayName: String?, taskId: TaskId?, status: TestStatus?, message: String?) {
    val testFinish = TestFinish(displayName, status)
    testFinish.message = message
    val taskFinishParams = TaskFinishParams(taskId, StatusCode.OK)
    taskFinishParams.dataKind = TaskFinishDataKind.TEST_FINISH
    taskFinishParams.data = testFinish
    taskFinishParams.message = if (isSuite) SUITE_TAG else TEST_TAG
    bspClient.onBuildTaskFinish(taskFinishParams)
  }

  /**
   * Notifies the client about beginning the testing procedure
   *
   * @param targetIdentifier identifier of the testing target being executed
   * @param taskId           TaskId of the testing target execution
   */
  fun beginTestTarget(targetIdentifier: BuildTargetIdentifier?, taskId: TaskId?) {
    val testingBegin = TestTask(targetIdentifier)
    val taskStartParams = TaskStartParams(taskId)
    taskStartParams.dataKind = TaskStartDataKind.TEST_TASK
    taskStartParams.data = testingBegin
    bspClient.onBuildTaskStart(taskStartParams)
  }

  /**
   * Notifies the client about ending the testing procedure
   *
   * @param testReport report concerning conducted tests
   * @param taskId     TaskId of the testing target execution
   */
  fun endTestTarget(testReport: TestReport?, taskId: TaskId?) {
    val taskFinishParams = TaskFinishParams(taskId, StatusCode.OK)
    taskFinishParams.dataKind = TaskFinishDataKind.TEST_REPORT
    taskFinishParams.data = testReport
    bspClient.onBuildTaskFinish(taskFinishParams)
  }

  fun initialize(buildClient: BuildClient) {
    bspClient = buildClient
  }

  companion object {
    private const val SUITE_TAG = "<S>"
    private const val TEST_TAG = "<T>"
  }
}
