package org.jetbrains.bsp.bazel.install.cli

import com.google.common.net.HostAndPort
import io.vavr.collection.List
import io.vavr.control.Option
import java.nio.file.Path

data class HelpCliOptions internal constructor(
        val isHelpOptionUsed: Boolean,
        val printHelp: () -> Unit,
)

data class ProjectViewCliOptions internal constructor(
        val javaPath: Option<Path>,
        val bazelPath: Option<Path>,
        val debuggerAddress: Option<HostAndPort>,
        val targets: Option<List<String>>,
        val buildFlags: Option<List<String>>,
)

data class CliOptions internal constructor(
        val helpCliOptions: HelpCliOptions,
        val workspaceRootDir: Path,
        val projectViewFilePath: Option<Path>,
        val projectViewCliOptions: ProjectViewCliOptions,
)
