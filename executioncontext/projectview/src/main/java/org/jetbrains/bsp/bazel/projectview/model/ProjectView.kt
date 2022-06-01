package org.jetbrains.bsp.bazel.projectview.model

import io.vavr.collection.Seq
import io.vavr.control.Try
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.projectview.model.sections.*

/**
 * Representation of the project view file.
 *
 * @link https://ij.bazel.build/docs/project-views.html
 */
data class ProjectView constructor(
    /** targets included and excluded from the project  */
    val targets: ProjectViewTargetsSection?,
    /** bazel path used to invoke bazel from the code  */
    val bazelPath: ProjectViewBazelPathSection?,
    /** debugger address which can be added to the server run command (as a flag to java)  */
    val debuggerAddress: ProjectViewDebuggerAddressSection?,
    /** path to java to run a server  */
    val javaPath: ProjectViewJavaPathSection?,
    /** bazel flags added to all bazel command invocations  */
    val buildFlags: ProjectViewBuildFlagsSection?,
    /** directories included and excluded from the project  */
    val directories: ProjectViewDirectoriesSection?,
    /** if set to true, relevant project targets will be automatically derived from the `directories` */
    val deriveTargetsFromDirectories: ProjectViewDeriveTargetsFromDirectoriesSection?,
) {

    class Builder constructor(
        private val imports: List<Try<ProjectView>> = emptyList(),
        private val targets: ProjectViewTargetsSection? = null,
        private val bazelPath: ProjectViewBazelPathSection? = null,
        private val debuggerAddress: ProjectViewDebuggerAddressSection? = null,
        private val javaPath: ProjectViewJavaPathSection? = null,
        private val buildFlags: ProjectViewBuildFlagsSection? = null,
        private val directories: ProjectViewDirectoriesSection? = null,
        private val deriveTargetsFromDirectories: ProjectViewDeriveTargetsFromDirectoriesSection? = null,
    ) {

        fun build(): Try<ProjectView> {
            log.debug(
                "Building project view with"
                        + " imports: {}"
                        + " and (before combining with imported project views)"
                        + " targets: {},"
                        + " bazel path: {},"
                        + " debugger address: {},"
                        + " java path: {},"
                        + " build flags: {},"
                        + " directories: {},"
                        + " deriveTargetsFromDirectories: {}.",
                imports,
                targets,
                bazelPath,
                debuggerAddress,
                javaPath,
                buildFlags,
                directories,
                deriveTargetsFromDirectories
            )

            return Try.sequence(imports)
                .map(Seq<ProjectView>::toJavaList)
                .map(MutableList<ProjectView>::toList)
                .map(::buildWithImports)
        }

        private fun buildWithImports(importedProjectViews: List<ProjectView>): ProjectView {
            val targets = combineTargetsSection(importedProjectViews)
            val bazelPath = combineBazelPathSection(importedProjectViews)
            val debuggerAddress = combineDebuggerAddressSection(importedProjectViews)
            val javaPath = combineJavaPathSection(importedProjectViews)
            val buildFlags = combineBuildFlagsSection(importedProjectViews)
            val directories = combineDirectoriesSection(importedProjectViews)
            val deriveTargetsFromDirectories = combineDeriveTargetFlagSection(importedProjectViews)
            log.debug(
                "Building project view with combined"
                        + " targets: {},"
                        + " bazel path: {},"
                        + " debugger address: {},"
                        + " java path: {},"
                        + " directories: {},"
                        + " deriveTargetsFlag: {}.",
                targets,
                bazelPath,
                debuggerAddress,
                javaPath,
                directories,
                deriveTargetsFromDirectories
            )
            return ProjectView(targets, bazelPath, debuggerAddress, javaPath, buildFlags, directories, deriveTargetsFromDirectories)
        }

        private fun combineTargetsSection(importedProjectViews: List<ProjectView>): ProjectViewTargetsSection? {
            val includedTargets = combineListValuesWithImported(
                importedProjectViews,
                targets,
                ProjectView::targets,
                ProjectViewTargetsSection::values
            )
            val excludedTargets = combineListValuesWithImported(
                importedProjectViews,
                targets,
                ProjectView::targets,
                ProjectViewTargetsSection::excludedValues
            )
            return createInstanceOfExcludableListSectionOrNull(
                includedTargets,
                excludedTargets,
                ::ProjectViewTargetsSection
            )
        }

        private fun combineBuildFlagsSection(importedProjectViews: List<ProjectView>): ProjectViewBuildFlagsSection? {
            val flags = combineListValuesWithImported(
                importedProjectViews,
                buildFlags,
                ProjectView::buildFlags,
                ProjectViewBuildFlagsSection::values,
            )

            return createInstanceOfListSectionOrNull(flags, ::ProjectViewBuildFlagsSection)
        }

        private fun combineDirectoriesSection(importedProjectViews: List<ProjectView>): ProjectViewDirectoriesSection? {
            val includedTargets = combineListValuesWithImported(
                    importedProjectViews,
                    directories,
                    ProjectView::directories,
                    ProjectViewDirectoriesSection::values
            )
            val excludedTargets = combineListValuesWithImported(
                    importedProjectViews,
                    directories,
                    ProjectView::directories,
                    ProjectViewDirectoriesSection::excludedValues
            )
            return createInstanceOfExcludableListSectionOrNull(
                    includedTargets,
                    excludedTargets,
                    ::ProjectViewDirectoriesSection
            )
        }

        private fun <V, T : ProjectViewListSection<V>> combineListValuesWithImported(
            importedProjectViews: List<ProjectView>,
            section: T?,
            sectionGetter: (ProjectView) -> T?,
            valuesGetter: (T) -> List<V>
        ): List<V> {
            val sectionValues = section
                ?.let(valuesGetter)
                .orEmpty()

            val importedValues = importedProjectViews
                .mapNotNull(sectionGetter)
                .flatMap(valuesGetter)

            return importedValues + sectionValues
        }

        private fun <V, T : ProjectViewExcludableListSection<V>?> createInstanceOfExcludableListSectionOrNull(
            includedElements: List<V>,
            excludedElements: List<V>,
            constructor: (List<V>, List<V>) -> T
        ): T? = if (includedElements.isEmpty() && excludedElements.isEmpty()) null else constructor(
            includedElements,
            excludedElements
        )

        private fun <V, T : ProjectViewListSection<V>?> createInstanceOfListSectionOrNull(
            values: List<V>, constructor: (List<V>) -> T
        ): T? = if (values.isEmpty()) null else constructor(values)

        private fun combineBazelPathSection(importedProjectViews: List<ProjectView>): ProjectViewBazelPathSection? =
            bazelPath ?: getLastImportedSingletonValue(importedProjectViews, ProjectView::bazelPath)

        private fun combineDebuggerAddressSection(importedProjectViews: List<ProjectView>): ProjectViewDebuggerAddressSection? =
            debuggerAddress ?: getLastImportedSingletonValue(importedProjectViews, ProjectView::debuggerAddress)

        private fun combineJavaPathSection(importedProjectViews: List<ProjectView>): ProjectViewJavaPathSection? =
            javaPath ?: getLastImportedSingletonValue(importedProjectViews, ProjectView::javaPath)

        private fun combineDeriveTargetFlagSection(importedProjectViews: List<ProjectView>): ProjectViewDeriveTargetsFromDirectoriesSection? =
            deriveTargetsFromDirectories ?: getLastImportedSingletonValue(importedProjectViews, ProjectView::deriveTargetsFromDirectories)

        private fun <T : ProjectViewSingletonSection<*>> getLastImportedSingletonValue(
            importedProjectViews: List<ProjectView>, sectionGetter: (ProjectView) -> T?
        ): T? = importedProjectViews.mapNotNull(sectionGetter).lastOrNull()
    }


    companion object {
        private val log = LogManager.getLogger(ProjectView::class.java)
    }
}
