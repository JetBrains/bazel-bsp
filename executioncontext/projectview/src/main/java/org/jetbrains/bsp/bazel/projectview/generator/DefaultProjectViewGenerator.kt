package org.jetbrains.bsp.bazel.projectview.generator

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path

class DefaultProjectViewGenerator : ProjectViewGenerator {

    override fun generatePrettyStringRepresentation(projectView: ProjectView): String {
        TODO("Not yet implemented")
    }

    override fun generatePrettyStringRepresentationAndSaveInFile(projectView: ProjectView, filePath: Path): Try<Void> {
        TODO("Not yet implemented")
    }
}