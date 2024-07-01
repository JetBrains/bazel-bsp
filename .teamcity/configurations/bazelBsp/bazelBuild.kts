package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

open class Build (
    vcsRoot: GitVcsRoot
): BaseConfiguration.BaseBuildType(
    name = "[build] build bazel-bsp",
    setupSteps = true,
    vcsRoot = vcsRoot,
    steps = {
        bazel {
            name = "build //..."
            command = "build"
            targets = "//..."
            param("toolPath", "/usr/local/bin")
        }
    }
)

object GitHub : Build(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object Space : Build(
    vcsRoot = BaseConfiguration.SpaceVcs
)