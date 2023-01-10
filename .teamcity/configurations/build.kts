package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class BuildBuildType(name: String, moduleLabel: String) : BaseConfiguration.BaseBuildType(
    name = "[build] $name",
    steps = {
        script {
            this.name = "building $moduleLabel"
            this.scriptContent = """bazel build $moduleLabel"""
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "cbills/build-runner"
        }
    }
)

object BuildTheProject : BuildBuildType(
    name = "build the project",
    moduleLabel = "//...",
)
