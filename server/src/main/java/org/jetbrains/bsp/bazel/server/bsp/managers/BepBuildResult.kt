package org.jetbrains.bsp.bazel.server.bsp.managers

import org.jetbrains.bsp.bazel.bazelrunner.BazelProcessResult
import org.jetbrains.bsp.bazel.server.bep.BepOutput

data class BepBuildResult(val processResult: BazelProcessResult, val bepOutput: BepOutput)
