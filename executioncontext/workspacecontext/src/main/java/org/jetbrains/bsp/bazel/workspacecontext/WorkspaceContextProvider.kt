package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.projectview.parser.DefaultProjectViewParser
import java.nio.file.Path

interface WorkspaceContextProvider {

    fun currentWorkspaceContext(): WorkspaceContext
}

class DefaultWorkspaceContextProvider(private val projectViewPath: Path?) : WorkspaceContextProvider {

    override fun currentWorkspaceContext(): WorkspaceContext =
        when (projectViewPath) {
            // we really need to think about exceptions / try
            null -> WorkspaceContextConstructor.constructDefault().getOrThrow()
            else -> parseProjectViewAndConstructWorkspaceContext(projectViewPath).getOrThrow()
        }

    private fun parseProjectViewAndConstructWorkspaceContext(projectViewPath: Path): Result<WorkspaceContext> {
        val projectViewParser = DefaultProjectViewParser()
        val projectViewTry = projectViewParser.parse(projectViewPath)

        return WorkspaceContextConstructor.construct(projectViewTry)
    }
}
