package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

object BuildTheProject : BaseConfiguration.BaseBuildType(
        name = "[build] build bazel-bsp",
        setupSteps = true,
        vcsRoot = BaseConfiguration.BazelBspVcs,
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