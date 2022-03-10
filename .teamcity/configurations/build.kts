package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class BazelBspBuildBuildType(name: String, moduleLabel: String) : BaseConfiguration.BazelBspBaseBuildType(
    name = "[build] $name",
    steps = {
        script {
            this.name = "building $moduleLabel"
            this.scriptContent = """bazel build $moduleLabel"""
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "andrefmrocha/bazelisk"
        }
    }
)

object BuildTheProject : BazelBspBuildBuildType(
    name = "build the project",
    moduleLabel = "//...",
)
