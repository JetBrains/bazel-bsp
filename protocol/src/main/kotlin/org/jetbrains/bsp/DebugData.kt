package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.RunParams
import ch.epfl.scala.bsp4j.StatusCode

data class AnalysisDebugParams(
  val originId: String,
  val port: Int,
  val targets: List<BuildTargetIdentifier>,
)

data class AnalysisDebugResult(
  val originId: String,
  val statusCode: StatusCode,
)

data class RemoteDebugData(
  val debugType: String,
  val port: Int,
)

data class RunWithDebugParams(
  val originId: String,
  val runParams: RunParams,
  val debug: RemoteDebugData?,
)
