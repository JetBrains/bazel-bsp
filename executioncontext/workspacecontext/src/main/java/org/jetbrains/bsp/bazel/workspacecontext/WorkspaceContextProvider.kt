package org.jetbrains.bsp.bazel.workspacecontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParserImpl
import java.nio.file.Path

interface WorkspaceContextProvider {

    fun currentWorkspaceContext(): WorkspaceContext
}


class DefaultWorkspaceContextProvider(private val projectViewPath: Path?) : WorkspaceContextProvider {

    private val projectViewParser = ProjectViewParserImpl()

    override fun currentWorkspaceContext(): WorkspaceContext =
        when (projectViewPath) {
            // we rally need to think about exceptions / try
            null -> WorkspaceContextConstructor.constructDefault().get()
            else -> parseProjectViewAndConstructWorkspaceContext(projectViewPath).get()
        }

    private fun parseProjectViewAndConstructWorkspaceContext(projectViewPath: Path): Try<WorkspaceContext> {
        val projectViewTry = projectViewParser.parse(projectViewPath)

        return WorkspaceContextConstructor.construct(projectViewTry)
    }
}
