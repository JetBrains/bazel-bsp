package org.jetbrains.bsp.bazel.projectview.generator.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection

abstract class ProjectViewSectionGenerator<in T : ProjectViewSection> {

    fun generatePrettyStringRepresentation(section: T?): String? =
        section?.let(::generatePrettyStringRepresentationForNonNull)

    protected abstract fun generatePrettyStringRepresentationForNonNull(section: T): String
}
