package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class ReleaseBuildType(name: String) : BaseConfiguration.BaseBuildType(
    name = "[release] $name",
    params = {
        password("gpg_secret", "credentialsJSON:bd309ca0-3387-4a6b-a185-95d162209e1e")
    },
    steps = {
        script {
            this.scriptContent = """
                apt-get update
                apt-get gpg
                apt-get install -y python3-pip
                pip3 install lxml
                wget https://github.com/bazelbuild/bazelisk/releases/download/v1.11.0/bazelisk-linux-amd64
                chmod +x bazelisk-linux-amd64
                echo %gpg_secret% | base64 -di | gpg --import
                bazelisk-linux-amd64 run --stamp \
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
