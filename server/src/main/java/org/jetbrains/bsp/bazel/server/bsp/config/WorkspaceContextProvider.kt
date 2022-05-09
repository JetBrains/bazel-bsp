package org.jetbrains.bsp.bazel.server.bsp.config

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParserImpl
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextConstructor
import java.nio.file.Path

interface WorkspaceContextProvider {

    fun currentWorkspaceContext(): Try<WorkspaceContext>
}


class DefaultWorkspaceContextProvider(private val projectViewPath: Path?) : WorkspaceContextProvider {

    private val projectViewParser = ProjectViewParserImpl()

    override fun currentWorkspaceContext(): Try<WorkspaceContext> =
        when (projectViewPath) {
            null -> WorkspaceContextConstructor.constructDefault()
            else -> parseProjectViewAndConstructWorkspaceContext(projectViewPath)
        }

    private fun parseProjectViewAndConstructWorkspaceContext(projectViewPath: Path): Try<WorkspaceContext> {
        val projectViewTry = projectViewParser.parse(projectViewPath)

        return WorkspaceContextConstructor.construct(projectViewTry)
    }
}
