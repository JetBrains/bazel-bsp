package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class ReleaseBuildType(name: String) : BaseConfiguration.BaseBuildType(
    name = "[release] $name",
    steps = {
        script {
            this.scriptContent = """echo XD"""
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "ubuntu"
        }
    }
)

object Release : ReleaseBuildType(
    name = "new release!",
)
