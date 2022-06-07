package org.jetbrains.bsp.bazel.projectview.parser.splitter

data class ProjectViewRawSection(val sectionName: String, val sectionBody: String)

data class ProjectViewRawSections(private val sections: List<ProjectViewRawSection>) {


    fun getLastSectionWithName(sectionName: String): ProjectViewRawSection? =
        sections.lastOrNull { it.sectionName == sectionName }

    fun getAllWithName(sectionName: String): List<ProjectViewRawSection> =
        sections.filter { it.sectionName == sectionName }
}
