package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

open class UnitTests(
    vcsRoot: GitVcsRoot,
): BaseConfiguration.BaseBuildType(
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
    vcsRoot = vcsRoot
)

object GitHub : UnitTests(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object Space : UnitTests(
    vcsRoot = BaseConfiguration.SpaceVcs
)