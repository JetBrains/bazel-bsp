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
                wget https://github.com/bazelbuild/bazelisk/releases/download/v1.11.0/bazelisk-linux-amd64
                chmod +x bazelisk-linux-amd64
                ./bazelisk-linux-amd64 build //...
                
                exit 127
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "ubuntu"
        }
    }
)

object Release : ReleaseBuildType(
    name = "new release!",
)
