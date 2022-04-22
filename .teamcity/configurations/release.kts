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
                echo %env.PGP_SECRET% | base64 -di | gpg --import
                ./bazelisk-linux-amd64 run --stamp \
                  --define "maven_user=%jetbrains.sonatype.access.token.username%" \
                  --define "maven_password=%jetbrains.sonatype.access.token.password%" \
                  //server/src/main/java/org/jetbrains/bsp/bazel:bsp.publish
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
