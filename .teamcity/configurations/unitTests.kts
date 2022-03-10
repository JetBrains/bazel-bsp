package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class BazelBspUnitTestsBuildType(moduleLabel: String) : BaseConfiguration.BazelBspBaseBuildType(
    name = "[unit tests] $moduleLabel tests",
    steps = {
        script {
            this.name = "testing $moduleLabel module"
            this.scriptContent = """bazel test $moduleLabel/..."""
            this.dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            this.dockerPull = true
            this.dockerImage = "andrefmrocha/bazelisk"
        }
    }
)

object BazelRunnerUnitTests : BazelBspUnitTestsBuildType(
    moduleLabel = "//bazelrunner",
)

object CommonsUnitTests : BazelBspUnitTestsBuildType(
    moduleLabel = "//commons",
)

object ExecutionContextUnitTests : BazelBspUnitTestsBuildType(
    moduleLabel = "//executioncontext",
)

object ServerUnitTests : BazelBspUnitTestsBuildType(
    moduleLabel = "//server",
)
