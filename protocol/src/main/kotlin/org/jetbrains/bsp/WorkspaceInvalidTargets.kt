@file:Suppress("MatchingDeclarationName")

package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildTargetIdentifier

public data class WorkspaceInvalidTargetsResult(
  val targets: List<BuildTargetIdentifier>,
)
