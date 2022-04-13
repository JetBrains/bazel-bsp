package org.jetbrains.bsp.bazel.projectview.model.sections

import com.google.common.net.HostAndPort
import java.nio.file.Path

sealed class ProjectViewSingletonSection<T>(sectionName: String) : ProjectViewSection(sectionName) {
    abstract val value: T
}

data class ProjectViewJavaPathSection(override val value: Path) :
    ProjectViewSingletonSection<Path>(SECTION_NAME) {
    companion object {
        const val SECTION_NAME = "java_path"
    }
}

data class ProjectViewDebuggerAddressSection(override val value: HostAndPort) :
    ProjectViewSingletonSection<HostAndPort>(SECTION_NAME) {
    companion object {
        const val SECTION_NAME = "debugger_address"
    }
}

data class ProjectViewBazelPathSection(override val value: Path) :
    ProjectViewSingletonSection<Path>(SECTION_NAME) {
    companion object {
        const val SECTION_NAME = "bazel_path"
    }
}
