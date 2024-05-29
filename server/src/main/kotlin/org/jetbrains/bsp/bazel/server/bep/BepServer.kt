package org.jetbrains.bsp.bazel.server.bep

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.CompileReport
import ch.epfl.scala.bsp4j.CompileTask
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams
import ch.epfl.scala.bsp4j.StatusCode
import ch.epfl.scala.bsp4j.TaskFinishDataKind
import ch.epfl.scala.bsp4j.TaskFinishParams
import ch.epfl.scala.bsp4j.TaskId
import ch.epfl.scala.bsp4j.TaskStartDataKind
import ch.epfl.scala.bsp4j.TaskStartParams
import ch.epfl.scala.bsp4j.TestReport
import ch.epfl.scala.bsp4j.TestStatus
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.PublishBuildEventGrpc
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse
import com.google.devtools.build.v1.PublishLifecycleEventRequest
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.bsp.JoinedBuildClient
import org.jetbrains.bsp.PublishOutputParams
import org.jetbrains.bsp.TestCoverageReport
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.bazel.server.bep.TestXmlParser
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService
import org.jetbrains.bsp.bazel.server.model.Label
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import java.io.IOException
import java.net.URI
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.AbstractMap.SimpleEntry
import java.util.function.Consumer


class BepServer(
  private val bspClient: JoinedBuildClient,
  private val diagnosticsService: DiagnosticsService,
  private val originId: String?,
  private val target: BuildTargetIdentifier?,
  bazelPathsResolver: BazelPathsResolver
) : PublishBuildEventGrpc.PublishBuildEventImplBase() {
  private val bspClientLogger = BspClientLogger(bspClient)
  private val bepLogger = BepLogger(bspClientLogger)

  private val startedEvents: Deque<Map.Entry<TaskId, String?>> = ArrayDeque()
  private val bepOutputBuilder = BepOutputBuilder(bazelPathsResolver)

  override fun publishLifecycleEvent(
    request: PublishLifecycleEventRequest, responseObserver: StreamObserver<Empty>
  ) {
    responseObserver.onNext(Empty.getDefaultInstance())
    responseObserver.onCompleted()
  }

  override fun publishBuildToolEventStream(
    responseObserver: StreamObserver<PublishBuildToolEventStreamResponse>
  ): StreamObserver<PublishBuildToolEventStreamRequest> {
    return BepStreamObserver(this, responseObserver)
  }

  fun handleEvent(buildEvent: BuildEvent) {
    try {
      val event =
        BuildEventStreamProtos.BuildEvent.parseFrom(buildEvent.bazelEvent.value)

      LOGGER.trace("Got event {}", event)

      handleBuildEventStreamProtosEvent(event)
    } catch (e: IOException) {
      LOGGER.error("Error deserializing BEP proto: {}", e.toString())
    }
  }

  fun handleBuildEventStreamProtosEvent(event: BuildEventStreamProtos.BuildEvent) {
    processBuildStartedEvent(event)
    processProgressEvent(event)
    processBuildMetrics(event)
    processFinishedEvent(event)
    processActionCompletedEvent(event)
    fetchNamedSet(event)
    processCompletedEvent(event)
    processAbortedEvent(event)
    processTestResult(event)
    processTestSummary(event)
  }

  private fun processTestResult(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasTestResult()) {
      if (originId == null) {
        return
      }

      val bspClientTestNotifier = BspClientTestNotifier(bspClient, originId)
      val testResult = event.testResult

      val parentId = TaskId(UUID.randomUUID().toString())

      bspClientTestNotifier.beginTestTarget(target, parentId)

      // TODO: this is the place where we could parse the test result and produce individual test events
      // TODO: there's some other interesting data
      //  If testing is requested, a TestResult event is sent for each test attempt,
      //  shard, and run per test. This allows BEP consumers to identify precisely
      //  which test actions failed their tests and identify the test outputs
      //  (such as logs, test.xml files) for each test action.

      val testStatus = when (testResult.status) {
        BuildEventStreamProtos.TestStatus.NO_STATUS -> TestStatus.SKIPPED
        BuildEventStreamProtos.TestStatus.PASSED -> TestStatus.PASSED
        BuildEventStreamProtos.TestStatus.FLAKY -> TestStatus.FAILED
        BuildEventStreamProtos.TestStatus.TIMEOUT -> TestStatus.FAILED
        BuildEventStreamProtos.TestStatus.FAILED -> TestStatus.FAILED
        BuildEventStreamProtos.TestStatus.INCOMPLETE -> TestStatus.SKIPPED
        BuildEventStreamProtos.TestStatus.REMOTE_FAILURE -> TestStatus.IGNORED
        BuildEventStreamProtos.TestStatus.FAILED_TO_BUILD -> TestStatus.CANCELLED
        BuildEventStreamProtos.TestStatus.TOOL_HALTED_BEFORE_TESTING -> TestStatus.SKIPPED
        else -> TestStatus.FAILED
      }

      val coverageReportUri = testResult.testActionOutputList.find { it.name == "test.lcov" }?.uri
      if (coverageReportUri != null) {
        bspClient.onBuildPublishOutput(PublishOutputParams(originId, parentId, target, TestCoverageReport.DATA_KIND, TestCoverageReport(coverageReportUri)))
      }

      val testXmlUri = testResult.testActionOutputList.find { it.name == "test.xml" }?.uri
      if (testXmlUri != null) {
        // Test cases identified and sent to the client by TestXmlParser.
        TestXmlParser(parentId, bspClientTestNotifier).parseAndReport(testXmlUri)
      } else {
        // Send a generic notification if individual tests cannot be processed.
        val childId = TaskId(UUID.randomUUID().toString())
        childId.parents = listOf(parentId.id)
        bspClientTestNotifier.startTest("Test", childId)
        bspClientTestNotifier.finishTest("Test", childId, testStatus, "Test finished")
      }

      val passed = if (testStatus == TestStatus.PASSED) 1 else 0
      val failed = if (testStatus == TestStatus.FAILED) 1 else 0
      val ignored = if (testStatus == TestStatus.IGNORED) 1 else 0
      val cancelled = if (testStatus == TestStatus.CANCELLED) 1 else 0
      val skipped = if (testStatus == TestStatus.SKIPPED) 1 else 0

      val testReport = TestReport(
        target, passed, failed, ignored, cancelled, skipped
      )

      bspClientTestNotifier.endTestTarget(testReport, parentId)
    }
  }

  private fun processTestSummary(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasTestSummary()) {
      // TODO: this is probably only relevant in remote scenarios
    }
  }

  private fun fetchNamedSet(event: BuildEventStreamProtos.BuildEvent) {
    if (event.id.hasNamedSet()) {
      bepOutputBuilder.storeNamedSet(
        event.id.namedSet.id, event.namedSetOfFiles
      )
    }
  }

  private fun processBuildStartedEvent(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasStarted() && event.started.command == Constants.BAZEL_BUILD_COMMAND) {
      consumeBuildStartedEvent(event.started)
    }
  }

  private fun processProgressEvent(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasProgress()) {
      if (target != null) {
        processDiagnosticText(event.progress.stderr, target.uri, true)
      }
      // TODO https://youtrack.jetbrains.com/issue/BAZEL-622
      // bepLogger.onProgress(event.getProgress());
    }
  }

  private fun processBuildMetrics(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasBuildMetrics()) {
      bepLogger.onBuildMetrics(event.buildMetrics)
    }
  }

  private fun consumeBuildStartedEvent(buildStarted: BuildEventStreamProtos.BuildStarted) {
    bepOutputBuilder.clear()
    val taskId = TaskId(buildStarted.uuid)
    val startParams = TaskStartParams(taskId)
    startParams.eventTime = buildStarted.startTimeMillis

    if (target != null) {
      startParams.dataKind = TaskStartDataKind.COMPILE_TASK
      val task = CompileTask(target)
      startParams.data = task
    }
    bspClient.onBuildTaskStart(startParams)
    startedEvents.push(SimpleEntry(taskId, originId))
  }

  private fun processFinishedEvent(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasFinished()) {
      consumeFinishedEvent(event.finished)
    }
  }

  private fun consumeFinishedEvent(buildFinished: BuildEventStreamProtos.BuildFinished) {
    if (startedEvents.isEmpty()) {
      LOGGER.debug("No start event id was found.")
      return
    }

    if (startedEvents.size > 1) {
      LOGGER.debug("More than 1 start event was found")
      return
    }

    val exitCode = ExitCodeMapper.mapExitCode(buildFinished.exitCode.code)
    val finishParams = TaskFinishParams(startedEvents.pop().key, exitCode)
    finishParams.eventTime = buildFinished.finishTimeMillis

    if (target != null) {
      finishParams.dataKind = TaskFinishDataKind.COMPILE_REPORT
      val isSuccess = exitCode.value == 1
      val errors = if (isSuccess) 0 else 1
      val report = CompileReport(target, errors, 0)
      finishParams.data = report
    }
    bspClient.onBuildTaskFinish(finishParams)
  }

  private fun processCompletedEvent(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasCompleted()) {
      consumeCompletedEvent(event)
    }
  }

  private fun processActionCompletedEvent(event: BuildEventStreamProtos.BuildEvent) {
    if (event.id.hasActionCompleted()) {
      consumeActionCompletedEvent(event)
    }
  }

  private fun consumeActionCompletedEvent(event: BuildEventStreamProtos.BuildEvent) {
    val label = event.id.actionCompleted.label
    val actionEvent = event.action
    if (!actionEvent.success) {
      consumeUnsuccessfulActionCompletedEvent(actionEvent, label)
    }
  }

  private fun consumeUnsuccessfulActionCompletedEvent(
    actionEvent: BuildEventStreamProtos.ActionExecuted, label: String
  ) {
    when (actionEvent.stderr.fileCase) {
      BuildEventStreamProtos.File.FileCase.URI -> {
        try {
          val path = Paths.get(URI.create(actionEvent.stderr.uri))
          val stdErrText = Files.readString(path)
          processDiagnosticText(stdErrText, label, false)
        } catch (e: FileSystemNotFoundException) {
          LOGGER.warn(e)
        } catch (e: IOException) {
          LOGGER.warn(e)
        }
      }
      BuildEventStreamProtos.File.FileCase.CONTENTS -> {
        processDiagnosticText(actionEvent.stderr.contents.toStringUtf8(), label, false)
      }
      else -> {
        processDiagnosticText("", label, false)
      }
    }
  }

  private fun processDiagnosticText(stdErrText: String, targetLabel: String, diagnosticsFromProgress: Boolean) {
    if (startedEvents.isNotEmpty() && stdErrText.isNotEmpty()) {
      val events =
        diagnosticsService.extractDiagnostics(
          stdErrText, targetLabel, startedEvents.first.value, diagnosticsFromProgress
        )
      events.forEach(Consumer { publishDiagnosticsParams: PublishDiagnosticsParams? ->
        bspClient.onBuildPublishDiagnostics(
            publishDiagnosticsParams
        )
      })
    }
  }

  private fun consumeCompletedEvent(event: BuildEventStreamProtos.BuildEvent) {
    val eventLabel = event.id.targetCompleted.label
    /* The events never contain @, which will be different than the actual target id. Here we work around that fact,
    * but since we also set up the BEP server to gather info about build targets within certain path (//... etc.), we can't
    * just use target.uri. So this only fixes the diagnostics without breaking the query for targets.
    * */
    val labelText = if (target != null && ("@$eventLabel" == target.uri || "@@$eventLabel" == target.uri) ) target.uri else eventLabel
    val label = Label.parse(labelText)
    val targetComplete = event.completed
    val outputGroups = targetComplete.outputGroupList
    LOGGER.trace("Consuming target completed event {}", targetComplete)
    bepOutputBuilder.storeTargetOutputGroups(label, outputGroups)

    // We should clear diagnostics only on completed successful compilation
    val shouldClearDiagnostics =
      outputGroups.stream().anyMatch { group: BuildEventStreamProtos.OutputGroup -> group.name == "default" }
    if (targetComplete.success && shouldClearDiagnostics) {
      // clear former diagnostics by publishing an empty array of diagnostics
      // why we do this on `target_completed` instead of `action_completed`?
      // because `action_completed` won't be published on build success for a target.
      // https://github.com/bazelbuild/bazel/blob/d43737f95d28789bb2d9ef2d7f62320e9a840ab0/src/main/java/com/google/devtools/build/lib/buildeventstream/proto/build_event_stream.proto#L157-L160
      val events = diagnosticsService.clearFormerDiagnostics(label)
      events.forEach(Consumer { publishDiagnosticsParams: PublishDiagnosticsParams? ->
        bspClient.onBuildPublishDiagnostics(
          publishDiagnosticsParams
        )
      })
    }
  }

  private fun processAbortedEvent(event: BuildEventStreamProtos.BuildEvent) {
    if (event.hasAborted()) {
      consumeAbortedEvent(event.aborted)
    }
  }

  private fun consumeAbortedEvent(aborted: BuildEventStreamProtos.Aborted) {
    if (aborted.reason != BuildEventStreamProtos.Aborted.AbortReason.NO_BUILD) {
      LOGGER.warn(
        "Command aborted with reason {}: {}", aborted.reason, aborted.description
      )
    }
  }

  val bepOutput: BepOutput = bepOutputBuilder.build()

  companion object {
    private val LOGGER: Logger = LogManager.getLogger(BepServer::class.java)
  }
}
