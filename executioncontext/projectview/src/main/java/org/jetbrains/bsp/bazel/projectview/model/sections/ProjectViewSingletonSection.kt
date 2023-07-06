package org.jetbrains.bsp.bazel.projectview.model.sections

import java.nio.file.Path

sealed class ProjectViewSingletonSection<T>(sectionName: String) : ProjectViewSection(sectionName) {
    abstract val value: T
}

data class ProjectViewDeriveTargetsFromDirectoriesSection(override val value: Boolean) :
        ProjectViewSingletonSection<Boolean>(SECTION_NAME) {
    companion object {
        const val SECTION_NAME = "derive_targets_from_directories"
    }
}

data class ProjectViewBazelPathSection(override val value: Path) :
    ProjectViewSingletonSection<Path>(SECTION_NAME) {
    companion object {
        const val SECTION_NAME = "bazel_path"
    }
}

data class ProjectViewBuildManualTargetsSection(override val value: Boolean) :
    ProjectViewSingletonSection<Boolean>(SECTION_NAME) {
    companion object {
        const val SECTION_NAME = "build_manual_targets"
    }
}

data class ProjectViewImportDepthSection(override val value: Int) :
    ProjectViewSingletonSection<Int>(SECTION_NAME) {
    companion object {
        const val SECTION_NAME = "import_depth"
    }
}
