package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

open class ReleaseBuildType(name: String) : BaseConfiguration.BaseBuildType(
    name = "[release] $name",
    setupSteps = true,
    steps = {
        script {
            this.name = "update GPG key"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                echo %env.GPG_SECRET% | base64 -di | gpg --import
            """.trimIndent()
        }
        bazel {
            this.name = "publish $name"
            this.command = "run"
            this.targets = "//server/src/main/java/org/jetbrains/bsp/bazel:bsp.publish"
            logging = BazelStep.Verbosity.Diagnostic
            param("toolPath", "/usr/bin")
            arguments = """
                --stamp --define "maven_user=%jetbrains.sonatype.access.token.username%" --define "maven_password=%jetbrains.sonatype.access.token.password%"
                """.trimIndent()
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
