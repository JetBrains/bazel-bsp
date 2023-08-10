package org.jetbrains.bsp.bazel.logger

import com.jetbrains.bsp.bsp4kt.BuildClient
import com.jetbrains.bsp.bsp4kt.BuildTargetIdentifier
import com.jetbrains.bsp.bsp4kt.StatusCode
import com.jetbrains.bsp.bsp4kt.TaskFinishDataKind
import com.jetbrains.bsp.bsp4kt.TaskFinishParams
import com.jetbrains.bsp.bsp4kt.TaskId
import com.jetbrains.bsp.bsp4kt.TaskStartDataKind
import com.jetbrains.bsp.bsp4kt.TaskStartParams
import com.jetbrains.bsp.bsp4kt.TestFinish
import com.jetbrains.bsp.bsp4kt.TestReport
import com.jetbrains.bsp.bsp4kt.TestStart
import com.jetbrains.bsp.bsp4kt.TestStatus
import com.jetbrains.bsp.bsp4kt.TestTask
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

class BspClientTestNotifier {
  private lateinit var bspClient: BuildClient
  private var originId: String? = null

  fun withOriginId(originId: String?): BspClientTestNotifier {
    val bspClientTestNotifier = BspClientTestNotifier()
    bspClientTestNotifier.originId = originId
    bspClientTestNotifier.bspClient = bspClient
    return bspClientTestNotifier
  }

  /**
   * Notifies the client about starting a single test or a test suite
   *
   * @param isSuite     `true` if a test suite has been started, `false` if it was a single test instead
   * @param displayName display name of the started test / test suite
   * @param taskId      TaskId of the started test / test suite
   */
  fun startTest(isSuite: Boolean, displayName: String, taskId: TaskId) {
    val testStart = Json.encodeToJsonElement(TestStart(displayName))
    val message = if (isSuite) SUITE_TAG else TEST_TAG
    val taskStartParams =
      TaskStartParams(taskId, message = message, dataKind = TaskStartDataKind.TestStart, data = testStart)

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
  fun finishTestSuite(displayName: String, taskId: TaskId) {
    finishTest(true, displayName, taskId, TestStatus.Passed, "")
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
  fun finishTest(isSuite: Boolean, displayName: String, taskId: TaskId, status: TestStatus, message: String?) {
    val testFinish = Json.encodeToJsonElement(TestFinish(displayName, status = status, message = message))
    val message = if (isSuite) SUITE_TAG else TEST_TAG
    val taskFinishParams = TaskFinishParams(
      taskId,
      status = StatusCode.Ok,
      message = message,
      dataKind = TaskFinishDataKind.TestFinish,
      data = testFinish
    )
    bspClient.onBuildTaskFinish(taskFinishParams)
  }

  /**
   * Notifies the client about beginning the testing procedure
   *
   * @param targetIdentifier identifier of the testing target being executed
   * @param taskId           TaskId of the testing target execution
   */
  fun beginTestTarget(targetIdentifier: BuildTargetIdentifier, taskId: TaskId) {
    val testTask = Json.encodeToJsonElement(TestTask(targetIdentifier))
    val taskStartParams =
      TaskStartParams(taskId, message = "Testing", dataKind = TaskStartDataKind.TestTask, data = testTask)
    bspClient.onBuildTaskStart(taskStartParams)
  }

  /**
   * Notifies the client about ending the testing procedure
   *
   * @param testReport report concerning conducted tests
   * @param taskId     TaskId of the testing target execution
   */
  fun endTestTarget(testReport: TestReport, taskId: TaskId) {
    val report = Json.encodeToJsonElement(testReport)
    val taskFinishParams = TaskFinishParams(
      taskId,
      status = StatusCode.Ok,
      message = "Testing",
      dataKind = TaskFinishDataKind.TestReport,
      data = report
    )
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
