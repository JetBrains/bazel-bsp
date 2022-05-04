package org.jetbrains.bsp.bazel.projectview.generator.sections

import org.jetbrains.bsp.bazel.projectview.model.sections.*

abstract class ProjectViewSingletonSectionGenerator<in T : ProjectViewSingletonSection<*>> :
    ProjectViewSectionGenerator<T>() {

    /**
     * Returns pretty representation of a singleton section, it means that the format looks like that:
     *
     * <section name>: <value>
     */
    override fun generatePrettyStringForNonNull(section: T): String =
        "${section.sectionName}: ${section.value}"
}

class ProjectViewJavaPathSectionGenerator : ProjectViewSingletonSectionGenerator<ProjectViewJavaPathSection>()

class ProjectViewDebuggerAddressSectionGenerator :
    ProjectViewSingletonSectionGenerator<ProjectViewDebuggerAddressSection>()

class ProjectViewBazelPathSectionGenerator : ProjectViewSingletonSectionGenerator<ProjectViewBazelPathSection>()

class ProjectViewManualSectionGenerator : ProjectViewSingletonSectionGenerator<ProjectViewManualSection>()
