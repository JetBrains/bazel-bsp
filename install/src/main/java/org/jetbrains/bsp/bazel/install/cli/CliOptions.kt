package org.jetbrains.bsp.bazel.install.cli

import com.google.common.net.HostAndPort
import java.nio.file.Path

data class HelpCliOptions internal constructor(
        val isHelpOptionUsed: Boolean,
        val printHelp: () -> Unit,
)

data class ProjectViewCliOptions internal constructor(
        val javaPath: Path?,
        val bazelPath: Path?,
        val debuggerAddress: HostAndPort?,
        val targets: List<String>?,
        val buildFlags: List<String>?,
)

data class CliOptions internal constructor(
        val helpCliOptions: HelpCliOptions,
        val workspaceRootDir: Path,
        val projectViewFilePath: Path?,
        val projectViewCliOptions: ProjectViewCliOptions?,
)
