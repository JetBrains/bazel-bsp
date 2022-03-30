package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class BazelBspBuildBuildType(name: String, moduleLabel: String) : BaseConfiguration.BazelBspBaseBuildType(
    name = "[build] $name",
    steps = {
        script {
            this.scriptContent = """
                wget https://github.com/bazelbuild/bazelisk/releases/download/v1.11.0/bazelisk-linux-amd64 --directory-prefix=%system.agent.persistent.cache%/bazel/
                chmod +x %system.agent.persistent.cache%/bazel/bazelisk-linux-amd64
            """.trimIndent()
        }
        bazel {
            this.name = "building $moduleLabel"
            this.command = "build"
            this.targets = moduleLabel
        }
    }
)

object BuildTheProject : BazelBspBuildBuildType(
    name = "build the project",
    moduleLabel = "//...",
)
