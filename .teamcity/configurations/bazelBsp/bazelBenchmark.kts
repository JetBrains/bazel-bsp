package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot


open class Benchmark(
    vcsRoot: GitVcsRoot
    ) : BaseConfiguration.BaseBuildType(
    artifactRules = "+:%system.teamcity.build.checkoutDir%/metrics.txt",
    name = "[benchmark] 10 targets",
    vcsRoot = vcsRoot,
    setupSteps = true,
    steps = {
        bazel {
            name = "generate 10 project for benchmark"
            id = "generate_project_for_benchmark"
            command = "run"
            targets = "//bspcli:generator /tmp/project_10 10"
            param("toolPath", "/usr/local/bin")
        }
        bazel {
            name = "run benchmark 10 targets"
            id = "run_benchmark"
            command = "run"
            targets = "//bspcli:bspcli /tmp/project_10 %system.teamcity.build.checkoutDir%/metrics.txt"
            param("toolPath", "/usr/local/bin")
        }
    }
)

object GitHub : Benchmark(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object Space : Benchmark(
    vcsRoot = BaseConfiguration.SpaceVcs
)
