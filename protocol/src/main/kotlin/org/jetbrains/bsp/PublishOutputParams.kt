package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.TaskId

data class PublishOutputParams(
    val originId: String,
    val taskId: TaskId?,
    val buildTarget: BuildTargetIdentifier?,
    val dataKind: String,
    val data: Any
)
