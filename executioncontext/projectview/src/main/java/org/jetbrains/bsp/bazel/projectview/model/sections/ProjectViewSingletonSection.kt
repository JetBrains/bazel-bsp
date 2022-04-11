package org.jetbrains.bsp.bazel.projectview.model.sections

import com.google.common.net.HostAndPort
import java.nio.file.Path

sealed class ProjectViewSingletonSection<T>(sectionName: String) : ProjectViewSection(sectionName) {
    abstract val value: T
}

data class ProjectViewJavaPathSection(override val value: Path) :
    ProjectViewSingletonSection<Path>("java_path")

data class ProjectViewDebuggerAddressSection(override val value: HostAndPort) :
    ProjectViewSingletonSection<HostAndPort>("debugger_address")

data class ProjectViewBazelPathSection(override val value: Path) :
    ProjectViewSingletonSection<Path>("bazel_path")
