package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot


open class Benchmark(
    vcsRoot: GitVcsRoot
    ) : BaseConfiguration.BaseBuildType(
    artifactRules = "+:%system.teamcity.build.checkoutDir%/metrics.txt",
    name = "[benchmark] 1001 targets",
    vcsRoot = vcsRoot,
    setupSteps = true,
    steps = {
        bazel {
            name = "generate 1001 project for benchmark"
            id = "generate_project_for_benchmark"
            command = "run"
            targets = "//bspcli:generator /tmp/project_1001 1001"
            param("toolPath", "/usr/local/bin")
        }
        bazel {
            name = "run benchmark 1001"
            id = "run_benchmark"
            command = "run"
            targets = "//bspcli:bspcli /tmp/project_1001 %system.teamcity.build.checkoutDir%/metrics.txt"
            param("toolPath", "/usr/local/bin")
        }
    },
    requirements =  {
        endsWith("cloud.amazon.agent-name-prefix", "-Large")
        equals("container.engine.osType", "linux")
    }
)

object GitHub : Benchmark(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object Space : Benchmark(
    vcsRoot = BaseConfiguration.SpaceVcs
)
