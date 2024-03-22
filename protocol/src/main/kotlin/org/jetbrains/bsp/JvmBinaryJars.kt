package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildTargetIdentifier

public data class JvmBinaryJarsParams(
  val targets: List<BuildTargetIdentifier>,
)

public data class JvmBinaryJarsResult(
  val items: List<JvmBinaryJarsItem>,
)

public data class JvmBinaryJarsItem(
  val target: BuildTargetIdentifier,
  val jars: List<String>,
)
