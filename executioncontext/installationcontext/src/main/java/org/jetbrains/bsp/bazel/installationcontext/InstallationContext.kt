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
) : ExecutionContext()


class InstallationContextConstructor(private val projectViewFilePath: Path?) :
    ExecutionContextConstructor<InstallationContext> {

    override fun construct(projectView: ProjectView): Try<InstallationContext> =
        javaPathMapper.map(projectView).flatMap { withJavaPath(it, projectView) }

    private fun withJavaPath(
        javaPathEntity: InstallationContextJavaPathEntity,
        projectView: ProjectView
    ): Try<InstallationContext> =
        debuggerAddressMapper.map(projectView).map { withJavaPathAndDebuggerAddress(javaPathEntity, it) }

    private fun withJavaPathAndDebuggerAddress(
        javaPathEntity: InstallationContextJavaPathEntity,
        debuggerAddressEntity: InstallationContextDebuggerAddressEntity?
    ): InstallationContext = InstallationContext(
        javaPath = javaPathEntity,
        debuggerAddress = debuggerAddressEntity,
        projectViewFilePath = projectViewFilePath
    )

    private companion object {
        private val javaPathMapper = InstallationContextJavaPathEntityMapper()
        private val debuggerAddressMapper = InstallationContextDebuggerAddressEntityMapper()
    }
}
