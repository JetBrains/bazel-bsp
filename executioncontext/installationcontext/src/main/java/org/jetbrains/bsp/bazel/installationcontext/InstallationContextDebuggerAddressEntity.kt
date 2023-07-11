package org.jetbrains.bsp.bazel.installationcontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity

data class InstallationContextDebuggerAddressEntity(override val value: String) :
    ExecutionContextSingletonEntity<String>()
