package org.jetbrains.bsp.bazel.projectview.generator.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection

abstract class ProjectViewSingletonSectionGenerator<in T : ProjectViewSingletonSection<*>> :
    ProjectViewSectionGenerator<T>() {

    override fun generatePrettyStringRepresentation(section: T?): String? {
        return section?.let(::generatePrettyStringRepresentationForNonNull)
    }

    private fun generatePrettyStringRepresentationForNonNull(section: T): String {
        return "${section.sectionName}: ${section.value}"
    }
}

class ProjectViewJavaPathSectionGenerator : ProjectViewSingletonSectionGenerator<ProjectViewJavaPathSection>()
