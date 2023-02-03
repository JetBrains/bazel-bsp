package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

open class BuildBuildType(command: String, targets: String) : BaseConfiguration.BaseBuildType(
    name = "[build] build the project",
    setupSteps = true,
    steps = {
        bazel {
            this.name = "$command $targets"
            this.command = command
            this.targets = targets
            param("toolPath", "/usr/bin")
        }
    },
    failureConditions = { },
)

object BuildTheProject : BuildBuildType(
    command = "build",
    targets = "//...",
)
