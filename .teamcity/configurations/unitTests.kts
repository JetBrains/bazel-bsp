package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class UnitTestsBuildType(moduleLabel: String) : BaseConfiguration.BaseBuildType(
    name = "[unit tests] unit tests $moduleLabel",
    steps = {
        script {
            this.name = "testing $moduleLabel"
            this.scriptContent = """bazel test --test_output=errors $moduleLabel"""
            this.dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            this.dockerPull = true
            this.dockerImage = "andrefmrocha/bazelisk"
        }
    }
)

object UnitTests : UnitTestsBuildType(
    moduleLabel = "//...",
)
