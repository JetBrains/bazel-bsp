package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path
import kotlin.io.path.Path

data class DotBazelBspDirPathSpec(
    override val value: Path
) : ExecutionContextSingletonEntity<Path>()

internal class DotBazelBspDirPathSpecExtractor(
    private val workspaceRoot: Path,
) : ExecutionContextEntityExtractor<DotBazelBspDirPathSpec> {

    override fun fromProjectView(projectView: ProjectView): DotBazelBspDirPathSpec =
        DotBazelBspDirPathSpec(
            value = calculatePathToDotBazelBspDir()
        )

    private fun calculatePathToDotBazelBspDir(): Path =
        workspaceRoot.toAbsolutePath().normalize().resolve(".bazelbsp")
}
