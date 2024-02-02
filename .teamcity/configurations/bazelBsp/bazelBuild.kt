package configurations.bazelBsp

import configurations.BaseBuildType
import configurations.BazelBspVcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

object BuildTheProject : BaseBuildType(
        name = "[build] build bazel-bsp",
        setupSteps = true,
        vcsRoot = BazelBspVcs,
        steps = {
            bazel {
                name = "build //..."
                command = "build"
                targets = "//..."
                param("toolPath", "/usr/local/bin")
            }
        },
    requirements =  {
        contains("cloud.amazon.agent-name-prefix", "Linux-Medium")
    }
)