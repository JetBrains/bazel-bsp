package org.jetbrains.bsp

public data class DirectoryItem(
  val uri: String,
)

public data class WorkspaceDirectoriesResult(
  val includedDirectories: List<DirectoryItem>,
  val excludedDirectories: List<DirectoryItem>,
)
