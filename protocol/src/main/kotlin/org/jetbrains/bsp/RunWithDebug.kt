package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.RunParams

public data class RemoteDebugData(
  val debugType: String,
  val port: Int,
)

public data class RunWithDebugParams(
  val originId: String,
  val runParams: RunParams,
  val debug: RemoteDebugData?,
)
