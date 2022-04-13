package org.jetbrains.bsp.bazel.projectview.saver

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

interface ProjectViewSaver {

    /**
     * Saves provided project view file in the file (which could be parsed by the `ProjectViewParser`).
     *
     * @param projectView - project view which should be saved in the file
     * @param filePath - path to file where the project view should be saved
     * @return `Try.Success` if the operation was successful, `Try.Failure` otherwise
     *
     * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
     * @see org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser
     */
    fun saveInFile(projectView: ProjectView, filePath: Path): Try<Void>
}
