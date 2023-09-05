package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.projectview.parser.DefaultProjectViewParser
import java.nio.file.Path

interface WorkspaceContextProvider {

    fun currentWorkspaceContext(): WorkspaceContext
}

class DefaultWorkspaceContextProvider(
    workspaceRoot: Path,
    private val projectViewPath: Path
) : WorkspaceContextProvider {

    private val workspaceContextConstructor = WorkspaceContextConstructor(workspaceRoot)

    override fun currentWorkspaceContext(): WorkspaceContext {
        val projectViewParser = DefaultProjectViewParser()
        val projectView = projectViewParser.parse(projectViewPath)

        return workspaceContextConstructor.construct(projectView)
    }
}
