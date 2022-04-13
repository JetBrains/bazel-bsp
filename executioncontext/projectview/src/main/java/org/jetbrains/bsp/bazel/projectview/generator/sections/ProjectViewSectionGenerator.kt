package org.jetbrains.bsp.bazel.projectview.generator.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSection

abstract class ProjectViewSectionGenerator<in T : ProjectViewSection> {

    abstract fun generatePrettyStringRepresentation(section: T?): String?
}
