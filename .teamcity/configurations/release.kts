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
                apt-get install -y python3-pip
                pip3 install lxml
                cd "/usr/local/lib/bazel/bin" && curl -fLO https://releases.bazel.build/5.1.0/release/bazel-5.1.0-linux-x86_64 && chmod +x bazel-5.1.0-linux-x86_64 && cd -
                echo %env.GPG_SECRET% | base64 -di | gpg --import
                bazel run --stamp \
                  --define "maven_user=%jetbrains.sonatype.access.token.username%" \
                  --define "maven_password=%jetbrains.sonatype.access.token.password%" \
                  //server/src/main/java/org/jetbrains/bsp/bazel:bsp.publish
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "gcr.io/cloud-marketplace-containers/google/bazel"
        }
    },
    failureConditions = {},
    notifications = {
        notifierSettings = slackNotifier {
            connection = "PROJECT_EXT_486"
            sendTo = "#bazel-build"
            messageFormat = verboseMessageFormat {
                addBranch = true
                addChanges = true
                addStatusText = true
                maximumNumberOfChanges = 10
            }
        }
        branchFilter = "+:<default>"
        buildFailed = true
        buildFinishedSuccessfully = true
    },
)

object Release : ReleaseBuildType(
    name = "new release!",
)
