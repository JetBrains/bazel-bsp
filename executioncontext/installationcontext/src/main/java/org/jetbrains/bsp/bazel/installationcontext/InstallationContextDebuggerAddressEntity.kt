package org.jetbrains.bsp.bazel.installationcontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityNullableMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection

data class InstallationContextDebuggerAddressEntity(override val value: String) :
    ExecutionContextSingletonEntity<String>()


internal object InstallationContextDebuggerAddressEntityMapper :
    ProjectViewToExecutionContextEntityNullableMapper<InstallationContextDebuggerAddressEntity> {

    override fun map(projectView: ProjectView): Try<InstallationContextDebuggerAddressEntity?> {
        val debuggerAddressEntity = projectView.debuggerAddress?.let(::map)

        return Try.success(debuggerAddressEntity)
    }

    private fun map(debuggerAddressSection: ProjectViewDebuggerAddressSection): InstallationContextDebuggerAddressEntity =
        InstallationContextDebuggerAddressEntity(debuggerAddressSection.value)
}
