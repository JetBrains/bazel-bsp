package org.jetbrains.bsp.bazel.projectview.generator.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection

abstract class ProjectViewSectionGenerator<in T : ProjectViewSection> {

    /**
     * Should return pretty representation of the section, what pretty means?
     * It depends on the section type.
     * If section is null, should return null
     */
    fun generatePrettyString(section: T?): String? =
        section?.let(::generatePrettyStringForNonNull)

    protected abstract fun generatePrettyStringForNonNull(section: T): String
}
