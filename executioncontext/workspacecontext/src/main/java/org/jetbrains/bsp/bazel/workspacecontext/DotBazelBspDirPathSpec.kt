package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextEntityExtractor
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path
import kotlin.io.path.Path

data class DotBazelBspDirPathSpec(
    override val value: Path
) : ExecutionContextSingletonEntity<Path>()

internal object DotBazelBspDirPathSpecExtractor : ExecutionContextEntityExtractor<DotBazelBspDirPathSpec> {

    override fun fromProjectView(projectView: ProjectView): DotBazelBspDirPathSpec = default()

    override fun default(): DotBazelBspDirPathSpec = calculatePathToDorBazelBspDirAndMap()

    private fun calculatePathToDorBazelBspDirAndMap(): DotBazelBspDirPathSpec =
        DotBazelBspDirPathSpec(
            value = calculatePathToDotBazelBspDir()
        )

    private fun calculatePathToDotBazelBspDir(): Path =
        Path("").toAbsolutePath().normalize().resolve(".bazelbsp")
}
