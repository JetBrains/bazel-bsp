package org.jetbrains.bsp.bazel.projectview.generator

import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

interface ProjectViewGenerator {

    /**
     * Saves provided project view (hopefuly using `generatePrettyStringRepresentation` result)
     * in the file (which could be parsed by the `ProjectViewParser`).
     *
     * @param projectView - project view which should be saved in the file
     * @param filePath - path to file where the project view should be saved
     *
     * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
     * @see org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser
     */
    fun generatePrettyStringAndSaveInFile(projectView: ProjectView, filePath: Path)

    /**
     * Generates pretty (user-friendly) string representation of the provided
     * project view which could be saved in a file and then parsed with the `ProjectViewParser`.
     *
     * @param projectView - project view which pretty string should be generated
     * @return String with pretty representation of the project view file:
     * <section 1 header>: <section 1 single value>
     * <section 2 header>:
     *     <section 2 value 1>
     *     <section 2 value 2>
     *     <section 2 value 3>
     *
     * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
     * @see org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser
     */
    fun generatePrettyString(projectView: ProjectView): String
}
