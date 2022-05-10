package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.parser.DefaultProjectViewParser
import java.nio.file.Path

interface WorkspaceContextProvider {

    fun currentWorkspaceContext(): WorkspaceContext
}


class DefaultWorkspaceContextProvider(private val projectViewPath: Path?) : WorkspaceContextProvider {

    override fun currentWorkspaceContext(): WorkspaceContext =
        when (projectViewPath) {
            // we rally need to think about exceptions / try
            null -> WorkspaceContextConstructor.constructDefault().get()
            else -> parseProjectViewAndConstructWorkspaceContext(projectViewPath).get()
        }

    private fun parseProjectViewAndConstructWorkspaceContext(projectViewPath: Path): Try<WorkspaceContext> {
        val projectViewParser = DefaultProjectViewParser()
        val projectViewTry = projectViewParser.parse(projectViewPath)

        return WorkspaceContextConstructor.construct(projectViewTry)
    }
}
