package org.jetbrains.bsp.bazel.install.cli

import java.nio.file.Path

data class HelpCliOptions internal constructor(
        val isHelpOptionUsed: Boolean,
        val printHelp: () -> Unit,
)

data class ProjectViewCliOptions internal constructor(
        val javaPath: Path?,
        val bazelPath: Path?,
        val debuggerAddress: String?,
        val targets: List<String>?,
        val excludedTargets: List<String>?,
        val buildFlags: List<String>?,
        val buildManualTargets: Boolean?,
        val directories: List<String>?,
        val excludedDirectories: List<String>?,
        val deriveTargetsFromDirectories: Boolean?,
        val importDepth: Int?,
)

data class BloopCliOptions internal constructor(
        val useBloop: Boolean
)

data class CliOptions internal constructor(
        val helpCliOptions: HelpCliOptions,
        val workspaceRootDir: Path,
        val projectViewFilePath: Path?,
        val projectViewCliOptions: ProjectViewCliOptions?,
        val bloopCliOptions: BloopCliOptions,
        val bazelWorkspaceRootDir: Path,
)
