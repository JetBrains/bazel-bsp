package org.jetbrains.bsp.bazel.projectview.generator.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection

abstract class ProjectViewSectionGenerator<in T : ProjectViewSection> {

    fun generatePrettyString(section: T?): String? =
        section?.let(::generatePrettyStringForNonNull)

    protected abstract fun generatePrettyStringForNonNull(section: T): String
}
