package org.jetbrains.bsp.bazel.installationcontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextConstructor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

data class InstallationContext(
    val javaPath: InstallationContextJavaPathEntity,
    val debuggerAddress: InstallationContextDebuggerAddressEntity?,
    val projectViewFilePath: Path?,
    val bazelWorkspaceRootDir: Path,
) : ExecutionContext()


class InstallationContextConstructor(private val projectViewFilePath: Path?, private val bazelWorkspaceRootDir: Path) :
    ExecutionContextConstructor<InstallationContext> {

    override fun construct(projectView: ProjectView): Try<InstallationContext> =
        InstallationContextJavaPathEntityMapper.map(projectView).flatMap { withJavaPath(it, projectView) }

    private fun withJavaPath(
        javaPathEntity: InstallationContextJavaPathEntity,
        projectView: ProjectView
    ): Try<InstallationContext> =
        InstallationContextDebuggerAddressEntityMapper.map(projectView)
            .map { withJavaPathAndDebuggerAddress(javaPathEntity, it) }

    private fun withJavaPathAndDebuggerAddress(
        javaPathEntity: InstallationContextJavaPathEntity,
        debuggerAddressEntity: InstallationContextDebuggerAddressEntity?
    ): InstallationContext = InstallationContext(
        javaPath = javaPathEntity,
        debuggerAddress = debuggerAddressEntity,
        projectViewFilePath = projectViewFilePath,
        bazelWorkspaceRootDir = bazelWorkspaceRootDir,
    )

    override fun constructDefault(): Try<InstallationContext> =
        InstallationContextJavaPathEntityMapper.default().flatMap { javaPathSpec ->
            InstallationContextDebuggerAddressEntityMapper.default().map { debuggerAddressSpec ->
                InstallationContext(
                    javaPath = javaPathSpec,
                    debuggerAddress = debuggerAddressSpec,
                    projectViewFilePath = projectViewFilePath,
                    bazelWorkspaceRootDir= bazelWorkspaceRootDir
                )
            }
        }
}
