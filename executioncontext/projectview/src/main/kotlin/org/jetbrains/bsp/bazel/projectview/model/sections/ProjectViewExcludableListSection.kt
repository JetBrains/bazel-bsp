package org.jetbrains.bsp.bazel.projectview.model.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import java.nio.file.Path

sealed class ProjectViewExcludableListSection<T> constructor(sectionName: String) :
    ProjectViewListSection<T>(sectionName) {
    abstract val excludedValues: List<T>
}

data class ProjectViewTargetsSection(
    override val values: List<BuildTargetIdentifier>,
    override val excludedValues: List<BuildTargetIdentifier>,
) : ProjectViewExcludableListSection<BuildTargetIdentifier>(SECTION_NAME) {

    companion object {
        const val SECTION_NAME = "targets"
    }
}

data class ProjectViewDirectoriesSection(
        override val values: List<Path>,
        override val excludedValues: List<Path>,
) : ProjectViewExcludableListSection<Path>(SECTION_NAME) {

    companion object {
        const val SECTION_NAME = "directories"
    }
}
