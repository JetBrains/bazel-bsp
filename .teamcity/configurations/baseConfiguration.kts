package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureConditions
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.ParametrizedWithType
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.Notifications
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.notifications

open class BaseBuildType(
    name: String,
    steps: BuildSteps.() -> Unit,
    failureConditions: FailureConditions.() -> Unit,
    artifactRules: String = "" ,
    notifications: (Notifications.() -> Unit)? = null
) : BuildType({
    id(name.toExtId())
    this.name = name
    this.artifactRules = artifactRules
    this.steps(steps)
    this.failureConditions(failureConditions)

    if (notifications != null) {
        this.features.notifications(notifications)
    }

    vcs {
        root(BazelBspVcs)
    }
})

object BazelBspVcs : GitVcsRoot({
    name = "bazel-bsp"
    url = "https://github.com/JetBrains/bazel-bsp.git"
    branch = "master"
    branchSpec =  """
            +:refs/heads/*
            -:refs/heads/team*city
        """.trimIndent()
})
