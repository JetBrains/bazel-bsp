package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

open class BazelBspBuildBuildType(name: String, moduleLabel: String) : BaseConfiguration.BazelBspBaseBuildType(
    name = "[build] $name",
    steps = {
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
