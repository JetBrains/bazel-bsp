package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class BazelBspFormatBuildType(name: String, scriptName: String, scriptContent: String, dockerImage: String) :
    BaseConfiguration.BazelBspBaseBuildType(
        name = "[format] $name",
        steps = {
            script {
                this.name = scriptName
                this.scriptContent = scriptContent
                this.dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
                this.dockerPull = true
                this.dockerImage = dockerImage
            }
        },
    )

object JavaFormat : BazelBspFormatBuildType(
    name = "google java format",
    scriptName = "checking formatting with google java format",
    scriptContent = """google-java-format -i --set-exit-if-changed ${'$'}(find . -type f -name "*.java")""",
    dockerImage = "vandmo/google-java-format",
)

object BuildifierFormat : BazelBspFormatBuildType(
    name = "buildifier",
    scriptName = "checking formatting with buildifier",
    scriptContent = """
                buildifier -r .
                buildifier --lint=fix -r .
                git diff --exit-code
            """.trimIndent(),
    dockerImage = "andrefmrocha/buildifier",
)
