package org.jetbrains.bsp.bazel.installationcontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext
import java.nio.file.Path

data class InstallationContext(
    val javaPath: InstallationContextJavaPathEntity,
    val debuggerAddress: InstallationContextDebuggerAddressEntity?,
    val projectViewFilePath: Path,
    val bazelWorkspaceRootDir: Path,
) : ExecutionContext()
