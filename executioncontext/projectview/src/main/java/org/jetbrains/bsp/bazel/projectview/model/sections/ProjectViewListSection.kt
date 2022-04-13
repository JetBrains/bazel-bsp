package org.jetbrains.bsp.bazel.projectview.model.sections

import io.vavr.collection.List

sealed class ProjectViewListSection<T> constructor(sectionName: String) : ProjectViewSection(sectionName) {
    abstract val values: List<T>
}

data class ProjectViewBuildFlagsSection(override val values: List<String>) :
    ProjectViewListSection<String>("build_flags")
