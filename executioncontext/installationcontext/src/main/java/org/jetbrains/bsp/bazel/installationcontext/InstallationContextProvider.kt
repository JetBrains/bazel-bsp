package org.jetbrains.bsp.bazel.installationcontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.projectview.parser.DefaultProjectViewParser
import java.nio.file.Path

interface InstallationContextProvider {

    fun currentInstallationContext(): Try<InstallationContext>
}


class DefaultInstallationContextProvider(private val projectViewPath: Path?, private val bazelWorkspaceRootDir:Path) : InstallationContextProvider {

    private val installationContextConstructor = InstallationContextConstructor(projectViewPath, bazelWorkspaceRootDir)

    override fun currentInstallationContext(): Try<InstallationContext> =
        when (projectViewPath) {
            null -> installationContextConstructor.constructDefault()
            else -> parseProjectViewAndConstructWorkspaceContext(projectViewPath)
        }

    private fun parseProjectViewAndConstructWorkspaceContext(projectViewPath: Path): Try<InstallationContext> {
        val projectViewParser = DefaultProjectViewParser()
        val projectViewTry = projectViewParser.parse(projectViewPath)

        return installationContextConstructor.construct(projectViewTry)
    }
}
