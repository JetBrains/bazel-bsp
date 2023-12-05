package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildTargetIdentifier

public data class LibraryItem(
  val id: BuildTargetIdentifier,
  val dependencies: List<BuildTargetIdentifier>,
  val jars: List<String>,
  val sourceJars: List<String>,
)

public data class WorkspaceLibrariesResult(
  val libraries: List<LibraryItem>,
)
