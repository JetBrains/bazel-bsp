package org.jetbrains.bsp.bazel.projectview.model.sections

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.collection.List

sealed class ProjectViewExcludableListSection<T> constructor(sectionName: String) :
    ProjectViewListSection<T>(sectionName) {
    abstract val excludedValues: List<T>
}

data class ProjectViewTargetsSection(
    override val values: List<BuildTargetIdentifier>,
    override val excludedValues: List<BuildTargetIdentifier>
) : ProjectViewExcludableListSection<BuildTargetIdentifier>("targets")
