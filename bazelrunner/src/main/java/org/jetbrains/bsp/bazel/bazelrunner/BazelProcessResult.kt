package org.jetbrains.bsp.bazel.bazelrunner

import ch.epfl.scala.bsp4j.StatusCode
import org.jetbrains.bsp.bazel.bazelrunner.outputs.OutputCollector
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper

class BazelProcessResult(
    private val stdoutCollector: OutputCollector,
    private val stderrCollector: OutputCollector,
    private val exitCode: Int
) {
  val isNotSuccess: Boolean get() = statusCode != StatusCode.OK
  val statusCode: StatusCode get() = ExitCodeMapper.mapExitCode(exitCode)
  val stdoutLines: List<String> get() = stdoutCollector.lines()
  val stdout: String get() = stdoutCollector.output()
  val stderrLines: List<String> get() = stderrCollector.lines()
  val stderr: String get() = stderrCollector.output()
}
