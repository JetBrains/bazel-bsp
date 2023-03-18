package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

open class ReleaseBuildType(
        name: String,
        nightly: Boolean = false,
        buildFinishedSuccessfully: Boolean = false
) : BaseConfiguration.BaseBuildType(
        name = "[publish] $name",
        setupSteps = true,
        steps = {
        if (nightly) {
            script {
                this.name = "setup"
                scriptContent = """
                #!/bin/bash
                set -euxo pipefail

                #get hash
                git_hash=${'$'}(git rev-parse --short HEAD)

                #get current version
                current_version=${'$'}(awk -F '"' '/maven_coordinates =/{print ${'$'}2; exit}' server/src/main/java/org/jetbrains/bsp/bazel/BUILD)

                #generate nigthly version and add it to file
                current_date=${'$'}(date +\%Y\%m\%d)
                new_version="${'$'}{current_version}-${'$'}{current_date}-${'$'}{git_hash}-NIGHTLY"
                sed -i "s/${'$'}current_version/${'$'}new_version/" server/src/main/java/org/jetbrains/bsp/bazel/BUILD
            """.trimIndent()
            }
        }
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
            this.buildFinishedSuccessfully = buildFinishedSuccessfully
        },
)

object Release : ReleaseBuildType(
        name = "new release!",
        buildFinishedSuccessfully = true
)

object Nightly : ReleaseBuildType(
        name = "nightly",
        nightly = true,

)