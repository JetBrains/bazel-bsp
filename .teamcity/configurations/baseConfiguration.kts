package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureConditions
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.ParametrizedWithType
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

open class BaseBuildType(
    name: String,
    steps: BuildSteps.() -> Unit,
    failureConditions: FailureConditions.() -> Unit,
    artifactRules: String = ""
) : BuildType({
    id(name.toExtId())
    this.name = name
    this.artifactRules = artifactRules
    this.steps(steps)
    this.failureConditions(failureConditions)

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
            -:refs/heads/team*city*local
        """.trimIndent()
})
