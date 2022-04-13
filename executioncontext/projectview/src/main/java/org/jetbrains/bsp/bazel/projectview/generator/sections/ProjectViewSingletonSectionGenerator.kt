package org.jetbrains.bsp.bazel.projectview.generator.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection

abstract class ProjectViewSingletonSectionGenerator<in T : ProjectViewSingletonSection<*>> :
    ProjectViewSectionGenerator<T>() {

    override fun generatePrettyStringForNonNull(section: T): String =
        "${section.sectionName}: ${section.value}"
}

class ProjectViewJavaPathSectionGenerator : ProjectViewSingletonSectionGenerator<ProjectViewJavaPathSection>()
