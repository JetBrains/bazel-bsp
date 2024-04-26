package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, create a buildType with id = 'E2eTestsE2eEnabledRulesTest'
in the root project, and delete the patch script.
*/
create(DslContext.projectId, BuildType({
    id("E2eTestsE2eEnabledRulesTest")
    name = "[e2e tests] //e2e:enabled_rules_test"

    artifactRules = "+:/home/teamcity/.cache/bazel/_bazel_teamcity/*/execroot/_main/bazel-out/k8-fastbuild/testlogs/e2e/** => testlogs.zip"

    vcs {
        root(RelativeId("BazelBspVcs"))
    }

    steps {
        script {
            name = "Coursier"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                                    
                #install coursier
                curl -fL "https://github.com/coursier/coursier/releases/download/v2.1.5/cs-x86_64-pc-linux.gz" | gzip -d > cs 
                sudo mv cs /usr/bin/cs
                
                sudo chmod +x "/usr/bin/cs"
            """.trimIndent()
        }
        bazel {
            name = "test //e2e:sample_repo_test"
            command = "test"
            targets = "//e2e:sample_repo_test"
            arguments = "--sandbox_writable_path=/home/teamcity/.cache"
            logging = BazelStep.Verbosity.Diagnostic
            param("toolPath", "/usr/local/bin")
        }
    }

    failureConditions {
        executionTimeoutMin = 60
    }

    features {
        perfmon {
        }
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
            vcsRootExtId = "Bazel_BazelBsp_BazelBspVcs"
            provider = github {
                authType = token {
                    token = "credentialsJSON:5bc345d4-e38f-4428-95e1-b6e4121aadf6"
                }
                filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
            }
        }
    }

    dependencies {
        snapshot(RelativeId("FormatBuildifier")) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }
}))

