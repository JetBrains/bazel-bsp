package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureConditions
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.ParametrizedWithType
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.Notifications
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep


open class BaseBuildType(
    name: String,
    steps: BuildSteps.() -> Unit,
    failureConditions: FailureConditions.() -> Unit,
    artifactRules: String = "",
    notifications: (Notifications.() -> Unit)? = null,
    setupSteps: Boolean = false
) : BuildType({
    id(name.toExtId())
    this.name = name
    this.artifactRules = artifactRules
    this.failureConditions(failureConditions)

    if (notifications != null) {
        this.features.notifications(notifications)
    }

    vcs {
        root(BazelBspVcs)
    }

    if (setupSteps == true) {
        steps {
            script {
                this.name = "Install necessary pakcages via dockerized Ubuntu"

                scriptContent = """
                    #!/bin/bash
                    set -euxo pipefail
                    
                    apt-get update -q
                    apt-get install -y build-essential
                    
                    # check installation
                    gcc --version ||:
                    find / -name 'cc1plus' 2>/dev/null ||:
                    
                    #install bazelisk
                    curl -L https://github.com/bazelbuild/bazelisk/releases/download/v1.15.0/bazelisk-linux-amd64 -o  \
                    "/usr/bin/bazel"
    
                    chmod +x "/usr/bin/bazel"
                    
                    #check bazel working
                    bazel version ||:
            """.trimIndent()

                dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
                dockerPull = true
                dockerImage = "ubuntu:focal"
                dockerRunParameters = """
                    -v /usr/:/usr/
                    -v /etc/:/etc/
                """.trimIndent()
            }
        }
    }
    this.steps(steps)
})

object BazelBspVcs : GitVcsRoot({
    name = "bazel-bsp-space-repo"
    url = "https://git.jetbrains.team/bsp/bazel-bsp.git"
    branch = "master"
    branchSpec = """
        +:refs/heads/*
        -:refs/heads/team*city
    """.trimIndent()
    authMethod = password {
        userName = "x-oauth-basic"
        password = "credentialsJSON:a53a34e8-7404-4fdd-bb1d-5e4a44ff20c1"
    }
    param("oauthProviderId", "PROJECT_EXT_2845")
    param("tokenType", "permanent")
})
