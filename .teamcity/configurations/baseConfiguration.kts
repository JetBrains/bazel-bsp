package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureConditions
import jetbrains.buildServer.configs.kotlin.v2019_2.Requirements
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script


open class BaseBuildType(
    name: String,
    vcsRoot: GitVcsRoot,
    steps: BuildSteps.() -> Unit,
    artifactRules: String = "",
    setupSteps: Boolean = false,
    failureConditions: FailureConditions.() -> Unit = {},
    requirements: (Requirements.() -> Unit)? = null
) : BuildType({

    this.name = name
    this.artifactRules = artifactRules
    this.failureConditions(failureConditions)

    failureConditions {
        executionTimeoutMin = 60
    }

    vcs {
        root(vcsRoot)
    }

    if (vcsRoot.name == "bazel-bsp-github" ) {
        id("GitHub$name".toExtId())
        if (requirements == null) {
            requirements {
                endsWith("cloud.amazon.agent-name-prefix", "Medium")
                equals("container.engine.osType", "linux")
            }
        } else {
            this.requirements(requirements)
        }
    } else {
        id("Space$name".toExtId())
        requirements {
            endsWith("cloud.amazon.agent-name-prefix", "-XLarge")
            equals("container.engine.osType", "linux")
        }
    }

    features {
        perfmon {
        }
        if (vcsRoot.name == "bazel-bsp-github") {
            commitStatusPublisher {
                publisher = github {
                    githubUrl = "https://api.github.com"
                    authType = personalToken {
                        token = "credentialsJSON:5bc345d4-e38f-4428-95e1-b6e4121aadf6"
                    }
                }
                param("github_oauth_user", "hb-man")
            }
            pullRequests {
                vcsRootExtId = "${vcsRoot.id}"
                provider = github {
                    authType = token {
                        token = "credentialsJSON:5bc345d4-e38f-4428-95e1-b6e4121aadf6"
                    }
                    filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
                }
            }
        } else {
            commitStatusPublisher {
                vcsRootExtId = "${vcsRoot.id}"
                publisher = space {
                    authType = connection {
                        connectionId = "PROJECT_EXT_12"
                    }
                    displayName = "BazelTeamCityCloud"
                }
            }
        }
    }

    if (setupSteps) {
        steps {
            script {
                this.name = "Coursier"

                scriptContent = """
                    #!/bin/bash
                    set -euxo pipefail
                                        
                    #install coursier
                    curl -fL "https://github.com/coursier/coursier/releases/download/v2.1.5/cs-x86_64-pc-linux.gz" | gzip -d > cs 
                    sudo mv cs /usr/bin/cs
                    
                    sudo chmod +x "/usr/bin/cs"
            """.trimIndent()
            }
        }
    }
    this.steps(steps)
})

object GitHubVcs : GitVcsRoot({
    name = "bazel-bsp-github"
    url = "https://github.com/JetBrains/bazel-bsp.git"
    branch = "master"
    branchSpec = "+:refs/heads/*"
    authMethod = password {
        userName = "hb-man"
        password = "credentialsJSON:5bc345d4-e38f-4428-95e1-b6e4121aadf6"
    }
    param("oauthProviderId", "tc-cloud-github-connection")
    param("tokenType", "permanent")
})

object SpaceVcs : GitVcsRoot({
    name = "bazel-bsp-space"
    url = "https://git.jetbrains.team/bazel/bazel-bsp.git"
    branch = "master"
    branchSpec = "+:refs/heads/*"
    authMethod = password {
        userName = "x-oauth-basic"
        password = "credentialsJSON:4efcb75d-2f9b-47fd-a63b-fc2969a334f5"
    }
    param("oauthProviderId", "PROJECT_EXT_15")
    param("tokenType", "permanent")
})