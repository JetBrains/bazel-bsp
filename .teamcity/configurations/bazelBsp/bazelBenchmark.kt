package configurations.bazelBsp

import configurations.BaseBuildType
import configurations.BazelBspVcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script



open class BazelBspBenchmarkBuildType(
        projectSize: String,
        ) : BaseBuildType(
        artifactRules = "+:%system.teamcity.build.checkoutDir%/metrics.txt",
        name = "[benchmark] $projectSize targets",
        vcsRoot = BazelBspVcs,
        setupSteps = true,
        steps = {
            bazel {
                name = "generate $projectSize project for benchmark"
                command = "run"
                targets = "//bspcli:generator /tmp/project_$projectSize $projectSize"
                param("toolPath", "/usr/local/bin")
            }
            bazel {
                name = "benchmark $projectSize"
                command = "run"
                targets = "//bspcli:bspcli /tmp/project_$projectSize %system.teamcity.build.checkoutDir%/metrics.txt"
                param("toolPath", "/usr/local/bin")
            }
            script {
                name = "adding project size to benchmark results file"
                scriptContent = """
                echo "Synthetic $projectSize project\n${'$'}(cat %system.teamcity.build.checkoutDir%/metrics.txt)" > temp.txt && mv temp.txt %system.teamcity.build.checkoutDir%/metrics.txt
            """.trimIndent()
            }
        },
        requirements =  {
            contains("cloud.amazon.agent-name-prefix", "Linux-Large")
        }
    )

object RegularBenchmark : BazelBspBenchmarkBuildType(
        projectSize = "1001"
)
