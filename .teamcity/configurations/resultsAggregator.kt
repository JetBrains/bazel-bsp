package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
object BazelBspAggregator : BuildType({
    id("bazel-bsp results".toExtId())

    name = "bazel-bsp results"

    allowExternalStatus = true

    vcs {
        root(BazelBspVcs)
        showDependenciesChanges = false
    }

    features {
        pullRequests {
            vcsRootExtId = "${BazelBspVcs.id}"
            provider = github {
                authType = token {
                    token = "credentialsJSON:5bc345d4-e38f-4428-95e1-b6e4121aadf6"
                }
                filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
            }
        }
    }

    type = Type.COMPOSITE
})