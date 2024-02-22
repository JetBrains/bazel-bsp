package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.projectview.generator.DefaultProjectViewGenerator
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.parser.DefaultProjectViewParser
import java.nio.file.Path
import kotlin.io.path.notExists

interface WorkspaceContextProvider {

    fun currentWorkspaceContext(): WorkspaceContext
}

class DefaultWorkspaceContextProvider(
    workspaceRoot: Path,
    private val projectViewPath: Path
) : WorkspaceContextProvider {

    private val workspaceContextConstructor = WorkspaceContextConstructor(workspaceRoot)

    override fun currentWorkspaceContext(): WorkspaceContext {
        val projectView = ensureProjectViewExistsAndParse()

        return workspaceContextConstructor.construct(projectView)
    }

    private fun ensureProjectViewExistsAndParse(): ProjectView {
        if (projectViewPath.notExists()) {
            generateEmptyProjectView()
        }
        return DefaultProjectViewParser().parse(projectViewPath)
    }

    private fun generateEmptyProjectView() {
        val emptyProjectView = ProjectView.Builder().build()
        DefaultProjectViewGenerator.generatePrettyStringAndSaveInFile(emptyProjectView, projectViewPath)
    }
}
