package org.jetbrains.bsp.bazel.projectview.model.sections

sealed class ProjectViewListSection<T> constructor(sectionName: String) : ProjectViewSection(sectionName) {
    abstract val values: List<T>
}

data class ProjectViewBuildFlagsSection(override val values: List<String>) :
    ProjectViewListSection<String>(SECTION_NAME) {

    companion object {
        const val SECTION_NAME = "build_flags"
    }
}
