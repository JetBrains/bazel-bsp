package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

open class BazelBspBaseBuildType(name: String, steps: BuildSteps.() -> Unit) : BuildType({
    id(name.toExtId())
    this.name = name

    this.steps(steps)

    vcs {
        root(BazelBspVcs)
    }

    features {
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:3f56fecd-4c69-4c60-85f2-13bc42792558"
                }
            }
        }
    }
})

object BazelBspVcs : GitVcsRoot({
    name = "bazel-bsp"
    url = "https://github.com/JetBrains/bazel-bsp.git"
    branch = "master"
    branchSpec = "refs/heads/(*)"
})