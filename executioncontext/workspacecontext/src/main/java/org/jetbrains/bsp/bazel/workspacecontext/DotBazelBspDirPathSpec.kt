package org.jetbrains.bsp.bazel.workspacecontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import java.nio.file.Path
import kotlin.io.path.Path

data class DotBazelBspDirPathSpec(
    override val value: Path
) : ExecutionContextSingletonEntity<Path>()

internal object DotBazelBspDirPathSpecMapper : ProjectViewToExecutionContextEntityMapper<DotBazelBspDirPathSpec> {

    override fun map(projectView: ProjectView): Result<DotBazelBspDirPathSpec> = default()

    override fun default(): Result<DotBazelBspDirPathSpec> = Result.success(calculatePathToDorBazelBspDirAndMap())

    private fun calculatePathToDorBazelBspDirAndMap(): DotBazelBspDirPathSpec =
        DotBazelBspDirPathSpec(
            value = calculatePathToDotBazelBspDir()
        )

    private fun calculatePathToDotBazelBspDir(): Path =
        Path("").toAbsolutePath().normalize().resolve(".bazelbsp")
}
