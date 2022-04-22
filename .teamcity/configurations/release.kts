package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class ReleaseBuildType(name: String) : BaseConfiguration.BaseBuildType(
    name = "[release] $name",
    steps = {
        script {
            this.scriptContent = """
                set -ex
                apt-get update
                apt-get install -y gpg
                apt-get install -y python3-pip
                apt-get install -y wget
                pip3 install lxml
                bazel build //...
                
                exit 127
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "andrefmrocha/bazelisk"
        }
    }
)

object Release : ReleaseBuildType(
    name = "new release!",
)
