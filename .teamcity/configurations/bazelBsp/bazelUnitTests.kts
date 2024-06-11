package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

object UnitTests : BaseConfiguration.BaseBuildType(
        name = "[unit tests] unit tests //...",
        setupSteps = true,
        steps = {
            bazel {
                name = "bazel test //..."
                command = "test"
                targets = "//..."
                arguments = "--test_output=errors"
                param("toolPath", "/usr/local/bin")
            }
        },
        vcsRoot = BaseConfiguration.BazelBspVcs,
        requirements =  {
            endsWith("cloud.amazon.agent-name-prefix", "Medium")
            equals("container.engine.osType", "linux")
        }
)