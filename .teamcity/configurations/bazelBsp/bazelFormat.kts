package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

open class Buildifier (
    vcsRoot: GitVcsRoot,
) : BaseConfiguration.BaseBuildType(
    name = "[format] buildifier",
    steps = {
        script {
            name = "checking formatting with buildifier"
            scriptContent = """
            buildifier -r .
            buildifier --lint=fix -r .
            git diff --exit-code
        """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "andrefmrocha/buildifier"
        }
    },
    vcsRoot = vcsRoot,
)

object GitHub : Buildifier(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object Space : Buildifier(
    vcsRoot = BaseConfiguration.SpaceVcs
)