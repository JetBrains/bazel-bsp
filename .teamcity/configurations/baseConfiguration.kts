package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.ParametrizedWithType
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

open class BaseBuildType(
    name: String,
    steps: BuildSteps.() -> Unit,
    artifactRules: String = ""
) : BuildType({
    id(name.toExtId())
    this.name = name
    this.artifactRules = artifactRules
    this.steps(steps)

    vcs {
        root(BazelBspVcs)
    }
})

object BazelBspVcs : GitVcsRoot({
    name = "bazel-bsp"
    url = "https://github.com/JetBrains/bazel-bsp.git"
    branch = "master"
    branchSpec = "refs/heads/(*)"
})