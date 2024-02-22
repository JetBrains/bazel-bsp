package configurations.bazelBsp

import configurations.BaseBuildType
import configurations.BazelBspVcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

object BuildifierFormat : BaseBuildType(
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
        vcsRoot = BazelBspVcs,
)